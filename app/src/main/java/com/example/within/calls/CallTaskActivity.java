package com.example.within.calls;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.DisconnectCause;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.within.R;
import com.example.within.calllog_activities.app_recent.RecentModel;
import com.example.within.helpers.*;
import com.example.within.interfaces.AppRecentCallLogListener;
import com.example.within.service_managers.*;
import com.example.within.workers.*;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.twilio.Twilio;
import com.twilio.audioswitch.AudioDevice;
import com.twilio.audioswitch.AudioSwitch;
import com.twilio.rest.microvisor.v1.App;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.RegistrationException;
import com.twilio.voice.RegistrationListener;
import com.twilio.voice.Voice;

import org.java_websocket.client.WebSocketClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;


public class CallTaskActivity extends AppCompatActivity {
    private AppRecentCallLogListener recentCallLogListener;
    private static  Call phoneCall = null;
    private static final String TAG = "CallTaskActivity";
    public static final String OUTGOING_CALL_ADDRESS = "OUTGOING_CALL_ADDRESS";
    public static final String ACTION_DISCONNECT_CALL = "ACTION_DISCONNECT_CALL";
    public static final String ACTION_DTMF_SEND = "ACTION_DTMF_SEND";
    public static final String DTMF = "DTMF";
    private static final int PERMISSIONS_ALL = 1;

    private NotificationManager notificationManager;
    private Dialog incomingCallDialog;
    private CallInvite activeCallInvite;
    private int savedVolumeControlStream;
    private String accessToken = "";
    private Call activeCall;
    private int activeCallNotificationId;

    private boolean isReceiverRegistered = false;
    private VoiceBroadcastReceiver voiceBroadcastReceiver;
    private AudioSwitch audioSwitch;
    private MenuItem audioDeviceMenuItem;
    private WebSocketClient webSocketClient;
    private PricingModel pricingModel;
    private HandleBottomSheet callActionSheet; // instantiate the bottom sheet
    private CallTask callTask;
    private FirebaseUser firebaseUser;
    private Handler callHandler;
    private long callStartTime;
    private boolean isCallActive = false;
    private String USER_ID;
    private Intent intent;
    private TextView recipientPhone, recipientName, duration;
    private ImageButton end_call;
    private RelativeLayout mainLayout;
    private Chronometer chronometer;
    private final HashMap<String, String> params = new HashMap<>();
    private SharedPreferences preferences;
    private AppRecentCallLogListener appRecentCallLogListener;


    public CallTaskActivity (){
    }

    public void setAppRecentCallListener(AppRecentCallLogListener appRecentCallLogListener){
        this.appRecentCallLogListener = appRecentCallLogListener;
    }

    private void answer() {
        SoundPoolManager.getInstance(this).stopRinging();
        activeCallInvite.accept(this, callListener());
        notificationManager.cancel(activeCallNotificationId);
        stopService(new Intent(getApplicationContext(), IncomingCallNotificationService.class));
        if (incomingCallDialog != null && incomingCallDialog.isShowing()) {
            incomingCallDialog.dismiss();
        }
    }


    private Call.Listener callListener() {
        return new Call.Listener() {
            /*
             * This callback is emitted once before the Call.Listener.onConnected() callback when
             * the callee is being alerted of a Call. The behavior of this callback is determined by
             * the answerOnBridge flag provided in the Dial verb of your TwiML application
             * associated with this client. If the answerOnBridge flag is false, which is the
             * default, the Call.Listener.onConnected() callback will be emitted immediately after
             * Call.Listener.onRinging(). If the answerOnBridge flag is true, this will cause the
             * call to emit the onConnected callback only after the call is answered.
             * See answeronbridge for more details on how to use it with the Dial TwiML verb. If the
             * twiML response contains a Say verb, then the call will emit the
             * Call.Listener.onConnected callback immediately after Call.Listener.onRinging() is
             * raised, irrespective of the value of answerOnBridge being set to true or false
             */
            @Override
            public void onRinging(@NonNull Call call) {
                phoneCall = call;
                Log.d(TAG, "Ringing");
                /*
                 * When [answerOnBridge](https://www.twilio.com/docs/voice/twiml/dial#answeronbridge)
                 * is enabled in the <Dial> TwiML verb, the caller will not hear the ringback while
                 * the call is ringing and awaiting to be accepted on the callee's side. The application
                 * can use the `SoundPoolManager` to play custom audio files between the
                 * `Call.Listener.onRinging()` and the `Call.Listener.onConnected()` callbacks.
                 */

                SoundPoolManager.getInstance(CallTaskActivity.this).playRinging();
            }


            @Override
            public void onConnectFailure(@NonNull Call call, @NonNull CallException error) {
                Log.d(TAG, "Connect failure");
                audioSwitch.deactivate();
                SoundPoolManager.getInstance(CallTaskActivity.this).stopRinging();

                resetConnectionService();
                String message = String.format(
                        Locale.US,
                        "Call Error: %d, %s",
                        error.getErrorCode(),
                        error.getMessage());
                Log.e(TAG, message);
                Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onConnected(@NonNull Call call) {
                audioSwitch.activate();

                SoundPoolManager.getInstance(CallTaskActivity.this).stopRinging();

                Log.d(TAG, "Connected");
                activeCall = call;
            }

            @Override
            public void onReconnecting(@NonNull Call call, @NonNull CallException callException) {

                Log.d(TAG, "onReconnecting");
            }

            @Override
            public void onReconnected(@NonNull Call call) {
                Log.d(TAG, "onReconnected");
            }

            @Override
            public void onDisconnected(@NonNull Call call, CallException error) {
                Log.d(TAG, "Disconnected");
                audioSwitch.deactivate();
                SoundPoolManager.getInstance(CallTaskActivity.this).stopRinging();

                VoiceConnectionService.getConnection().setDisconnected(
                        new DisconnectCause(DisconnectCause.UNKNOWN));
                resetConnectionService();
                if (error != null) {
                    String message = String.format(
                            Locale.US,
                            "Call Error: %d, %s",
                            error.getErrorCode(),
                            error.getMessage());
                    Log.e(TAG, message);
                    Snackbar.make(mainLayout, message, Snackbar.LENGTH_SHORT).show();
                }
            }

            /*
             * currentWarnings: existing quality warnings that have not been cleared yet
             * previousWarnings: last set of warnings prior to receiving this callback
             *
             * Example:
             *   - currentWarnings: { A, B }
             *   - previousWarnings: { B, C }
             *
             * Newly raised warnings = currentWarnings - intersection = { A }
             * Newly cleared warnings = previousWarnings - intersection = { C }
             */
            public void onCallQualityWarningsChanged(@NonNull Call call,
                                                     @NonNull Set<Call.CallQualityWarning> currentWarnings,
                                                     @NonNull Set<Call.CallQualityWarning> previousWarnings) {

                if (previousWarnings.size() > 1) {
                    Set<Call.CallQualityWarning> intersection = new HashSet<>(currentWarnings);
                    currentWarnings.removeAll(previousWarnings);
                    intersection.retainAll(previousWarnings);
                    previousWarnings.removeAll(intersection);
                }

                String message = String.format(
                        Locale.US,
                        "Newly raised warnings: " + currentWarnings + " Clear warnings " + previousWarnings);
                Log.e(TAG, message);
                Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).show();
            }
        };
    }


    private void handleIncomingCallIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            activeCallInvite = intent.getParcelableExtra(Constants.INCOMING_CALL_INVITE);
            activeCallNotificationId = intent.getIntExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, 0);

            // Get the callee name and number from the activeCallInvite and pass the values
            // to the handleIncoming call method
            String name = Objects.requireNonNull(activeCallInvite.
                    getCallerInfo().isVerified()).toString();

            String number = activeCallInvite.getFrom();


            switch (action) {
                case Constants.ACTION_INCOMING_CALL:
                    handleIncomingCall(name, number);
//                   // GenericCallAndContactWorker.addRecentCalls(new RecentModel(name,
//                           number, "" , false, "incoming"));
                    break;
                case Constants.ACTION_INCOMING_CALL_NOTIFICATION:
                    //TODO INCOMING CALL NOTIFICATION
                    break;
                case Constants.ACTION_CANCEL_CALL:
                    handleCancel();
                    break;
                case Constants.ACTION_FCM_TOKEN:
                    registerForCallInvites();
                    break;
                case Constants.ACTION_ACCEPT:
                    answer();
                    break;
                default:
                    break;
            }
        }
    }

    private void handleIncomingCall(String calleeName, String calleeNo) {
        if (isAppVisible()) {
            showIncomingCallDialog(calleeName, calleeNo);
        }
    }

    private void handleCancel() {
        if (incomingCallDialog != null && incomingCallDialog.isShowing()) {
            SoundPoolManager.getInstance(this).stopRinging();
            incomingCallDialog.cancel();
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }


    /*
    * This is a helper method that checks for the call action sheet display
    * to dismiss the sheet when it is not null and valid or to create and
    * show a new modalBottomSheet which contains the call action buttons like
    * hold, mute, endCall, add call */
    private void handleCallSheet() {
        if (callActionSheet != null && callActionSheet.isVisible()) {
            callActionSheet.dismiss();
        }else{
            callActionSheet = new HandleBottomSheet();
            callActionSheet.show(getSupportFragmentManager(), "ModalBottomSheet");
        }
    }


    /**
     * Method will handle the clients call request by getting the sent in intent from either the dial-pad,
     * contacts, and recent pages. checks if the intent is valid before getting the extras in them
     * which will be used to build a connections options using a map for the key value pairs,
     * the connection options will take the access-token generated by the server to target every individual call
     * monitor and listen.
     * finally i will use the Voice.connect method of the twilio api to connect the call and initiate a call
     * also passing the recipients phone number and the intended action from the user before starting the
     * service*/
    final void handleCallRequest(Intent intent) {
        if (intent != null && intent.getAction() != null) { //--Check for null values in the intent
            String action = intent.getAction(); //--Get the action set to the activity

            if (action.equals(Constants.ACTION_OUTGOING_CALL)) { //--If the action matches outgoing call
                final Bundle extras = intent.getExtras(); // Get the extra data from the intent

                if (extras != null) { //--check null values in the data
                    //-- get the contact number sent the intent using the final keyword
                    String to = extras.getString(Constants.OUTGOING_CALL_RECIPIENT);
                    String calleeName = extras.getString(Constants.RECIPIENT_NAME);
                    Log.d(TAG, "Recent model updated from the call");
                    updateCallLog(calleeName, to);

                    if (to != null) {
                       // GenericCallAndContactWorker.addRecentCalls(new RecentModel(calleeName, to, "", false,"dialed"));
                        //-- check for recipients null value before puting it into the map
                        params.put("to", to);
                        //--Initiate a call by calling this method which will send an action
                        //--to the call service to start the call service
                        initiateCalls(to, intent); //--Pass the received number from the intent
                        // updateCallLog(calleeName, to);
                        Log.d(TAG, "Call initiated using twilio voice.connect method " + activeCall);
                    }
                } else {
                    Snackbar.make(this, mainLayout, "Invalid phone number entered", Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    final void handleCallWorker(String recipient){
        //-- Creates a new data to pass the recipient number to the server
        Data inputData = new Data.Builder()
                .putString("to", recipient)  // Example parameter
                .build();

        // Sets the constraint for the worker to a network type connected which means the
        // worker would run only on the ground there is a valid internet connection
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        //--Create a onetime work request to run the callTask class which sends a call
        //--request to the server to place a call and the worker will return a status
        //--code if successful
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(CallTask.class)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build();

        // Enque the work request to the manager
        WorkManager.getInstance(getApplicationContext()).enqueue(request);
        //--Monitor the work flow of the request using is request id to target it.
        //--manage the workflow creating the connect options and the call only when the
        //--work sate returns a success.
        //--this ensures proper work flow for the user
        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(request.getId())
                .observe(this, workInfo -> {
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        //--Create a connection options which will be pas.sed the access-token to
                        //--tag the user to a call and ensure the user is validated on the system to make calls
                        //--also pass the params which contains the phone number in the incoming intent
                        ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken)
                                .params(params)
                                .build();
                        activeCall = Voice.connect(getApplicationContext(), connectOptions, callListener());

                    } else if (workInfo.getState() == WorkInfo.State.FAILED){
                        int reason = workInfo.getStopReason();
                        Toast.makeText(getApplicationContext(), "Error with the call worker with reason " + reason , Toast.LENGTH_SHORT).show();
                    }
                });
    }



    /*
       These code are algorithms to check the intent sent to the CallActivityTask from other activities
       or fragments to place a call. the algorithm gets the phone number sent and compares
       it against our pricing model, display an alert dialog to the user on what country they are calling and
       how much they will be charged for the calls. if the user agrees, the call is started and so is the timer
        else the activity is closed
       */
    final void handlePricingDialog(){

        if (firebaseUser != null) { // Checks if the caller is a logged in user before placing a call
            USER_ID = firebaseUser.getUid();

            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction(); //- get the intent action and the extra data
                Bundle extra = intent.getExtras();

                //-- Check the action type
                if (action.equals(Constants.ACTION_OUTGOING_CALL) && extra != null){
                    String name = extra.getString(Constants.RECIPIENT_NAME);
                    String phone = extra.getString(Constants.OUTGOING_CALL_RECIPIENT);

                /*Check for valid pointer in the phone number like if it starts with the country
                    code or if it is longer than 3 as twilio does not support emergency calls
                    also check if is does not contain any string literals just numbers*/
                    if (phone != null && phone.startsWith("+")
                            || phone.length() > 3
                            && !phone.matches(".*[a-zA-Z].*")) {

                        showPricingDialog(phone); //---display pricing dialog
                        runOnUiThread(()-> recipientPhone.setText(phone)); //-- Update the ui with the phone number
                    }else if (name != null){
                        runOnUiThread(()->{ // -- check for null values in the name then update accordingly if none
                            recipientPhone.setText(phone);
                            recipientName.setText(name);
                        });
                    }else {
                        Toast.makeText(this, "Make sure this is a valid number ", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

            }
        }
    }


    /*
     * Helper method gets the ongoing call anc checks for a null value
     * then it takes also checks if the activeCall is not on-hold before
     * it calls the hold method of the activeCall and passes the boolean
     * returned from the call check to hold or un-hold the call if hold
     **/
    protected void hold() {
        if (activeCall != null) {
            boolean hold = !activeCall.isOnHold();
            activeCall.hold(hold);

        }
    }


    private boolean isAppVisible() {
        return ProcessLifecycleOwner
                .get()
                .getLifecycle()
                .getCurrentState()
                .isAtLeast(Lifecycle.State.STARTED);
    }



    /*
    * Helper method used to initialize the views on the ongoing call layout
    * */
    private void initializeViews() {
        recipientPhone = findViewById(R.id.recipient_phone);
        recipientName = findViewById(R.id.recipient_name);
        mainLayout = findViewById(R.id.main_call_layout);
        chronometer = findViewById(R.id.call_timer);
    }


    /*
    * Helper method creates an intent parsing the to number as params
    * to the receiving activity which will be used to display notification
    * and also connect to the users telecom manager
    *  */
    private void initiateCalls(String to, Intent intent){
        String action = intent.getAction();
        if (action != null && action.equals(Constants.ACTION_OUTGOING_CALL)){
            /* Call the call worker to start the work request to initiate a call on the server */
            handleCallWorker(to);
            /*Create a new intent to open the incomingNotificationService which
             handles the calls notification service and connect to the telecom manager*/
            Intent callIntent = new Intent(getApplicationContext(), IncomingCallNotificationService.class);
            callIntent.putExtra(Constants.OUTGOING_CALL_RECIPIENT, to);
            callIntent.setAction(Constants.ACTION_OUTGOING_CALL);
            getApplicationContext().startService(callIntent);
        }
    }

    /*
    * Helper method is used to provide client with the required permissions to make a phone call
    * because the call to the callee would be interrupted or not go through if these permissions
    * are not granted.
    *
    * The method uses a list of type string to create a new vector of type string*/
    static private String[] providePermissions() {
        List<String> permissionsList = new Vector<String>() {{
            add(Manifest.permission.RECORD_AUDIO);
            //add(Manifest.permission.CALL_PHONE); // <- Add for different behavior
            add(Manifest.permission.BLUETOOTH_CONNECT);
        }};
        String[] list = new String[permissionsList.size()];
        return permissionsList.toArray(list);
    }


    static private Map<String, String> providePermissionsMesageMap() {
        return new HashMap<String, String>() {{
            put(Manifest.permission.RECORD_AUDIO,
                    "Audio recording permission needed. Please allow in your application settings.");
            put(Manifest.permission.CALL_PHONE,
                    "Call phone permission needed. Please allow in your application settings.");
            put(Manifest.permission.MANAGE_OWN_CALLS,
                    "Manage Own Calls permission needed. Please allow in your application settings.");
            put(Manifest.permission.BLUETOOTH_CONNECT,
                    "Without bluetooth permission app will fail to use bluetooth.");
        }};
    }



    private void registerReceiver() {
        if (!isReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_DISCONNECT_CALL);
            intentFilter.addAction(ACTION_DTMF_SEND);
            intentFilter.addAction(Constants.ACTION_INCOMING_CALL);
            intentFilter.addAction(Constants.ACTION_OUTGOING_CALL);
            intentFilter.addAction(Constants.ACTION_CANCEL_CALL);
            intentFilter.addAction(Constants.ACTION_FCM_TOKEN);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    voiceBroadcastReceiver, intentFilter);
            isReceiverRegistered = true;
        }
    }

    private RegistrationListener registrationListener() {
        return new RegistrationListener() {
            @Override
            public void onRegistered(@NonNull String accessToken, @NonNull String fcmToken) {
                Log.d(TAG, "Successfully registered FCM " + fcmToken);
            }

            @Override
            public void onError(@NonNull RegistrationException error,
                                @NonNull String accessToken,
                                @NonNull String fcmToken) {
                String message = String.format(
                        Locale.US,
                        "Registration Error: %d, %s",
                        error.getErrorCode(),
                        error.getMessage());
                Log.e(TAG, message);
                Toast.makeText(getApplicationContext() , message, Toast.LENGTH_LONG).show();
            }
        };
    }

    /*
     * Register your FCM token with Twilio to receive incoming call invites
     */
    private void registerForCallInvites() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && accessToken != null) {
                        String fcmToken = Objects.requireNonNull(task.getResult());
                        Log.i(TAG, "Registering with FCM");
                        Voice.register(accessToken, Voice.RegistrationChannel.FCM, fcmToken, registrationListener());
                    }else {
                        Log.d(TAG, "Error generating fcmtoken.. check the firebase messaging service");
                    }
                });
    }

    /**
     * This helper method is used to fire the interface which listens for calls to update the app
     * recent call log list.
     * @param name is the name of the callee
     * @param number is the phone number of the callee which is passed
     * as argument to the recentModel
     */
    void updateCallLog(String name, String number) {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String dateString = date.toString();
        Log.d(TAG, "update call log date " + dateString);

        if (recentCallLogListener != null) {
            recentCallLogListener.onItemAdded(
                    new RecentModel(name,
                            number,
                            R.drawable.ic_person,
                            null,
                            false,
                            null,
                            dateString
                    ));
            Log.d(TAG, "Recent call interface triggered");
        }else{
            Log.d(TAG, "Listener is " + null);
        }
    }


//    /**
//     * This method sets the recent call log listener by taking
//     * @params recentListener which will be used by the calling activity
//     * that implements the listener to*/
//    public void setRecentCallLogListeners (AppRecentCallLogListener recentCallLogListeners){
//        this.recentCallLogListener = recentCallLogListeners;
//    }


    /**
     * Method is used to build an alert dialog that takes params (phone)
     * and display an alert dialog using material alert dialog builder to display to the user
     * their call destination and the cost per minute
     *  @param phone */
    private void showPricingDialog(String phone) {
        String country = ""; // Initialize a new string to hold the key value (country)
        Double price = 0.0; // Initialize a new double to hold the key value (price)
        // Create a new pricing model object and invoke its methods
        // Create new map to hold the return value for the pricing models checkPricing method
        Map<String, Double> countryAndPricing = pricingModel.checkPricing(phone);

        if (countryAndPricing != null && !countryAndPricing.isEmpty()) {
            country = countryAndPricing.keySet().iterator().next();
            price = countryAndPricing.get(country);

            // Create a new material alert dialog that would display the message and buttons to the user
            // to make their choices
            MaterialAlertDialogBuilder callDialog = new MaterialAlertDialogBuilder(this);
            callDialog.setTitle("Call Rates")
                    .setMessage("Calls to " + country + " cost " + price + " do you still want to continue?")
                    .setIcon(R.drawable.ic_phone)
                    .setPositiveButton("Place call", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // When the user chooses to continue with their call, set the flag for call active so the
                            // call counter would be fired.
                            isCallActive = true;
                            // Display the bottom sheet handling the users ability to interact with the calls
                            // like end call or add another caller
                            handleCallSheet();
                            handleCallRequest(intent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish(); // Close the activity
                        }
                    });
            callDialog.show();
        } else {
            // Display toast to the user indicating that pricing information is not available for the country
            Toast.makeText(this, "Pricing information not available for this country", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * This inner member class extends the android os BroadcastReciever to send broadcasts to the
     * mobile phones broadcast services indicating when there is an outgoing call from the device
     * calling helper methods to perform tasks based on the action received .
     * */
    private class VoiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case Constants.ACTION_OUTGOING_CALL:
                            handleCallRequest(intent);
//                            GenericCallAndContactWorker.addRecentCalls(new RecentModel(intent.getStringExtra(Constants.RECIPIENT_NAME),
//                                    intent.getStringExtra(Constants.OUTGOING_CALL_RECIPIENT),
//                                    null, false, "dialed"));
                            Log.d(TAG, "Voice broadcast outgoing call action: " + action);
                            break;
                        case ACTION_DISCONNECT_CALL:
                            if (activeCall != null) {
                                activeCall.disconnect();
                            }
                            break;
                        case ACTION_DTMF_SEND:
                            if (activeCall != null) {
                                activeCall.sendDigits(Objects.requireNonNull(intent.getStringExtra(DTMF)));
                            }
                            break;
                    }
                }
            }
        }
    }




//    private void startAudioSwitch() {
//        /*
//         * Start the audio device selector after the menu is created and update the icon when the
//         * selected audio device changes.
//         */
//        audioSwitch.start((audioDevices, audioDevice) -> {
//            Log.d(TAG, "Updating AudioDeviceIcon");
//            updateAudioDeviceIcon(audioDevice);
//            return Unit.INSTANCE;
//        });
//    }

    private void resetConnectionService() {
        if (null != VoiceConnectionService.getConnection()) {
            VoiceConnectionService.releaseConnection();
        }
    }


    private void unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(voiceBroadcastReceiver);
            isReceiverRegistered = false;
        }
    }


    /*
    * This method is used to display an incoming call dialog that displays over the main screen when the user has an
    * incoming call. this is done using the Dialog object to expand the incoming call layout which contains a floating
    * action button and the incoming callers name or number
    *  */
    final void showIncomingCallDialog (String name, String phone){
        // Create a new dialog and set the content view to the required layout
        incomingCallDialog = new Dialog(getApplicationContext());
        incomingCallDialog.setContentView(R.layout.fragment_incoming_call);

        // Initialize the views in the layout here which for now is a floating action button
        // which will be dragged eother up or down to receive a call.
        // TODO for later version i will add the ability for users to send a quick message to the caller when not available
        FloatingActionButton recieveCall = incomingCallDialog.findViewById(R.id.accept_call);
        TextView phoneNumber = incomingCallDialog.findViewById(R.id.incoming_phone);
        TextView calleeName = incomingCallDialog.findViewById(R.id.incoming_name);
        final String[] incomingPhone = new String[1];

        if (phoneNumber != null && calleeName != null){
            runOnUiThread(()->{
                // Use twilio call invite to get the incoming callers name and the callers info if verified
                final String call = "Calling " + activeCallInvite.getCallerInfo().isVerified(); // This will return the callee name if verified
                incomingPhone[0] = activeCallInvite.getFrom(); // This will return the callee phone number and
                phoneNumber.setText(incomingPhone[0]);
                calleeName.setText(call);
            });
        }
        // Add a null pointer to check if the dialog window is null before turning the screen on
        if (incomingCallDialog.getWindow() != null) {
            Objects.requireNonNull(incomingCallDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// Turn the screen on
        }
        // Set the dismiss listener to trigger a notification when dismissed
        // Set the on drag listener to pick the call when it rings
        incomingCallDialog.setOnDismissListener(onDismissListener(incomingPhone[0]));
        recieveCall.setOnDragListener(onDragListener(incomingCallDialog));
        incomingCallDialog.show();
    }

    /*
    * This listener will return the user back to the main screen and hide the dialog when the user dismisses
    * the incoming call screen. the service would trigger a call notification until the user is ready to take
    * the call or terminate the call*/
    final DialogInterface.OnDismissListener onDismissListener(String number){
        return new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Intent cancelIntent = new Intent(CallTaskActivity.this, IncomingCallNotificationService.class);
                cancelIntent.setAction(Constants.ACTION_HIDE_INCOMING_CALL);
                // Display a long toast to keep the client aware of an incoming call in notifications
                Toast.makeText(getApplicationContext(), number + " calling...", Toast.LENGTH_LONG).show();
                startService(cancelIntent);
                finish();
            }
        };
    }

    /*
    * Helper method used to monitor the drag event for the floating action button which
    * in the incoming call layout which requires users to drag up to recieve a call or
    *  drag down to terminate the call.
    * the code below first checks the start point and the current location of the FAB
    * and if the drag has gone up it creates an intent and passes a set of constants
    * to the incoming call service else, it sends another constant to the same service
    * before terminating the dialog.
    *
    * The params dialog is used to dismiss the dialog after an action is performed*/
    final View.OnDragListener onDragListener(Dialog dialog){

        return new View.OnDragListener() {
            private float startY, currentY;
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        startY = v.getY(); // Get the x location when drag begins
                        break;

                    case DragEvent.ACTION_DRAG_LOCATION:
                        currentY = v.getX();
                        // Check if the current location is lesser than the last location
                        // or greater. if it is higher it means the user dragged up to pick the call
                        // so the return  value would be true. but if the current location is lower than
                        // the last location that means the use ended the call
                        if (currentY > startY){
                            SoundPoolManager.getInstance(CallTaskActivity.this).stopRinging();
                            if (activeCallInvite != null) {
                                Intent intent = new Intent(CallTaskActivity.this, IncomingCallNotificationService.class);
                                intent.setAction(Constants.ACTION_REJECT);
                                intent.putExtra(Constants.INCOMING_CALL_INVITE, activeCallInvite);
                                Log.d("CallTaskActivity", "Incoming call" + activeCallInvite);// Debug message
                                startService(intent);
                            }
                            dialog.dismiss(); // Dismiss the dialog when the client terminates the call

                        } else if (currentY < startY) {
                            Intent callIntent = new Intent(CallTaskActivity.this, IncomingCallNotificationService.class);
                            callIntent.setAction(Constants.ACTION_ACCEPT);
                            Toast.makeText(getApplicationContext(), "Call; accepted", Toast.LENGTH_SHORT)
                                    // Send a toast to the user .. also used to debug but should work fine for production
                                    .show();
                            startService(intent);
                            dialog.dismiss(); // Dismiss the dialog when the user picks up the call
                        }
                }
                return false;
            }
        };
    }



    /*
     * Disconnect from Call
     */
    protected void disconnect() {
        if (activeCall != null) {
            activeCall.disconnect();
            activeCall = null;
        }
    }


    /*
    * Helper method gets the ongoing call anc checks for a null value
    * then it takes also checks if the activeCall is not muted before
    * it calls the mute method of the activeCall and passes the boolean
    * returned from the call check
    * */
    public final  void mute() {
        if (activeCall != null) {
            boolean mute = !activeCall.isMuted();
            activeCall.mute(mute);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        final Map<String, String> permissionsMessageMap = providePermissionsMesageMap();
        for (String permission : providePermissions()) {
            if (hasPermissions(this, permission)) {
                /*
                 * Due to bluetooth permissions being requested at the same time as mic
                 * permissions, AudioSwitch should be started after providing the user the option
                 * to grant the necessary permissions for bluetooth.
                 */
                if (!permission.equals(Manifest.permission.BLUETOOTH_CONNECT)) {
                    Snackbar.make(mainLayout,
                            Objects.requireNonNull(permissionsMessageMap.get(permission)),
                            Snackbar.LENGTH_LONG).show();
                } else {
                    //startAudioSwitch();
                    registerForCallInvites();
                }
            }
        }
    }


    /*
     * Show the current available audio devices.
     */
    private void showAudioDevices() {
        AudioDevice selectedDevice = audioSwitch.getSelectedAudioDevice();
        List<AudioDevice> availableAudioDevices = audioSwitch.getAvailableAudioDevices();

        if (selectedDevice != null) {
            int selectedDeviceIndex = availableAudioDevices.indexOf(selectedDevice);

            ArrayList<String> audioDeviceNames = new ArrayList<>();
            for (AudioDevice a : availableAudioDevices) {
                audioDeviceNames.add(a.getName());
            }

            new AlertDialog.Builder(this)
                    .setTitle(R.string.select_device)
                    .setSingleChoiceItems(
                            audioDeviceNames.toArray(new CharSequence[0]),
                            selectedDeviceIndex,
                            (dialog, index) -> {
                                dialog.dismiss();
                                AudioDevice selectedAudioDevice = availableAudioDevices.get(index);
                                //updateAudioDeviceIcon(selectedAudioDevice);
                                audioSwitch.selectDevice(selectedAudioDevice);
                            }).create().show();
        }
    }






    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_call);
        pricingModel = new PricingModel(); // Initializes the pricing model object
        callHandler = new Handler(); // Used to handle threads on the UI
        intent = getIntent(); // Get the intent sent from other activities or fragments

        // These flags ensure that the activity can be launched when the screen is locked.
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize the views
        initializeViews();

        // Set the chronometer to start counting when the call is started
        // but first set the base of the timer to hh:mm:ss
        chronometer.setBase(System.currentTimeMillis());
        chronometer.setFormat("%02d:%02d:%02d");

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        /*
         * Setup the broadcast receiver to be notified of FCM Token updates
         * or incoming call invite in this Activity.
         */

        callStartTime = System.currentTimeMillis(); // Get the current system time

        // To handle the users call interactions without ending the main thread while the call is ongoing,
        // the listeners would check if the bottom sheet is displaying before hiding it
        mainLayout.setOnClickListener(v -> {
            handleCallSheet();
        });

        handlePricingDialog();

        String[] permissionsList = providePermissions();
        if (hasPermissions(this, permissionsList)) {
            ActivityCompat.requestPermissions(this, permissionsList, PERMISSIONS_ALL);
        } else {
            registerForCallInvites();
        }

        /*
         * Setup audio device management and set the volume control stream
         */
        registerReceiver();
        audioSwitch = new AudioSwitch(getApplicationContext());
        savedVolumeControlStream = getVolumeControlStream();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Get the access token from the share preference as soon as the view is created to ensure
        // the token used across the activity has been validated by the server
        preferences = getSharedPreferences("within_preferences", Context.MODE_PRIVATE);
        accessToken = preferences.getString("access_token", null);
        Log.d("Shared preference access token", "access token " + accessToken);
        registerReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (intent == null){
            intent = getIntent();
        }
        //startAudioSwitch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
         * Tear down audio device management and restore previous volume stream
         */
        audioSwitch.stop();
        setVolumeControlStream(savedVolumeControlStream);
        SoundPoolManager.getInstance(getApplicationContext()).release();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingCallIntent(intent);
    }




    /**
     * This fragment handles the call bottom sheet which is displayed when the user opens the activity
     * the xml includes image buttons for (speaker phone, mute, add call, end call),
     * handles the listeners attached to this buttons implementing the methods fired when pressed.
     * some of the methods includes the ability for user to add another user to an ongoing call,
     * terminate an ongoing call, mute an ongoing call and put the call on speaker.
     * <p>
     * <p>
     * IN LATER VERSIONS OF WITHIN THIS FRAGMENT WOULD IMPLEMENT THE CODE TO ENABLE THE USERS RECORD A CALL
     * AND SAVE IT AS AN MP4 FILE IN THE USERS CHOSEN DIRECTORY
     */

    public static class HandleBottomSheet extends BottomSheetDialogFragment {
        //CallTaskActivity callTaskActivity = new CallTaskActivity();


        // Create an empty constructor
        public HandleBottomSheet() {
        }

        ImageButton endCall, onMute, speaker, addCall;

        /*
         * Initialize the views for the bottom sheet here*/
        private void initializeViews(View view) {
            endCall = view.findViewById(R.id.end_call);
            onMute = view.findViewById(R.id.mute_call);
            speaker = view.findViewById(R.id.speaker_phone);
            addCall = view.findViewById(R.id.add_call);
        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_call_bottom_sheet, container, false);
            initializeViews(view);
            return view;
        }


        // Handles the algorithm to set the style of the bottomSheet when called
        @Override
        public void onStart() {
            super.onStart();
            // Set the desired peek height (adjust this value as needed)
            Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow())
                    .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
//
//        final View.OnClickListener callControlListener (){
//            return v -> {
//
//                int id = v.getId();
//                if (id == R.id.mute_call){
//                    callTaskActivity.mute();
//                } else if (id == R.id.end_call) {
//                    callTaskActivity.disconnect();
//                } // Todo add more call control functionalities
//
//            };
//        }
    }


}


