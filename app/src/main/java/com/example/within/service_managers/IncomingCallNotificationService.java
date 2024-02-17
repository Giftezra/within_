package com.example.within.service_managers;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.within.R;
import com.example.within.helpers.*;
import com.example.within.notifications.NotificationProxyActivity;
import com.twilio.voice.CallInvite;

public class IncomingCallNotificationService extends Service {
    private static final String TAG = IncomingCallNotificationService.class.getSimpleName();

    private PhoneAccountHandle phoneAccountHandle;
    private TelecomManager telecomManager;


    @Override
    public void onCreate() {
        super.onCreate();
        // register telecom account info
        Context appContext = this.getApplicationContext();
        String appName = this.getString(R.string.app_name);
        phoneAccountHandle =
                new PhoneAccountHandle(new ComponentName(appContext, VoiceConnectionService.class),
                        appName);
        telecomManager = (TelecomManager) appContext.getSystemService(TELECOM_SERVICE);
        PhoneAccount phoneAccount = null;
        phoneAccount = new PhoneAccount.Builder(phoneAccountHandle, appName)
                .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
                .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                .build();
        telecomManager.registerPhoneAccount(phoneAccount);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action != null) {
            CallInvite callInvite = intent.getParcelableExtra(Constants.INCOMING_CALL_INVITE);
            int notificationId = intent.getIntExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, 0);
            switch (action) {
                case Constants.ACTION_INCOMING_CALL:
                    handleIncomingCall(callInvite, notificationId);
                    break;
                case Constants.ACTION_OUTGOING_CALL:
                    handleOutgoingCall(intent);
                    break;
                case Constants.ACTION_ACCEPT:
                    // Handle the case when the user accepts the call
                    assert callInvite != null;
                    accept(callInvite, notificationId);
                    break;
                case Constants.ACTION_REJECT:
                    assert callInvite != null;
                    reject(callInvite);
                    break;
                case Constants.ACTION_CANCEL_CALL:
                    handleCancelledCall(intent);
                    break;
                default:
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification(CallInvite callInvite, int notificationId, int channelImportance) {
        Intent intent = new Intent(this, NotificationProxyActivity.class);
        intent.setAction(Constants.ACTION_INCOMING_CALL_NOTIFICATION);
        intent.putExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, notificationId);
        intent.putExtra(Constants.INCOMING_CALL_INVITE, callInvite);
        intent.putExtra(Constants.ACTION_HIDE_INCOMING_CALL, callInvite); // Set the flag for when the call is cancelled or dialog closed
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_IMMUTABLE);
        /*
         * Pass the notification id and call sid to use as an identifier to cancel the
         * notification later
         */
        Bundle extras = new Bundle();
        extras.putString(Constants.CALL_SID_KEY, callInvite.getCallSid());

        return buildNotification(callInvite.getFrom() + " is calling.",
                pendingIntent,
                extras,
                callInvite,
                notificationId,
                createChannel(channelImportance));
    }

    /**
     * Build a notification.
     *
     * @param text          the text of the notification
     * @param pendingIntent the body, pending intent for the notification
     * @param extras        extras passed with the notification
     * @return the builder
     */
    private Notification buildNotification(String text, PendingIntent pendingIntent, Bundle extras,
                                           final CallInvite callInvite,
                                           int notificationId,
                                           String channelId) {
        Intent rejectIntent = new Intent(getApplicationContext(), IncomingCallNotificationService.class);
        rejectIntent.setAction(Constants.ACTION_REJECT);
        rejectIntent.putExtra(Constants.INCOMING_CALL_INVITE, callInvite);
        rejectIntent.putExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, notificationId);
        PendingIntent piRejectIntent = PendingIntent.getService(getApplicationContext(), notificationId, rejectIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent acceptIntent = new Intent(getApplicationContext(), NotificationProxyActivity.class);
        acceptIntent.setAction(Constants.ACTION_ACCEPT);
        acceptIntent.putExtra(Constants.INCOMING_CALL_INVITE, callInvite);
        acceptIntent.putExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, notificationId);
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent piAcceptIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, acceptIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder =
                new Notification.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_call_end)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(text)
                        .setCategory(Notification.CATEGORY_CALL)
                        .setExtras(extras)
                        .setAutoCancel(true)
                        .addAction(android.R.drawable.ic_menu_delete, getString(R.string.decline), piRejectIntent)
                        .addAction(android.R.drawable.ic_menu_call, getString(R.string.answer), piAcceptIntent)
                        .setFullScreenIntent(pendingIntent, true);

        return builder.build();
    }

    private String createChannel(int channelImportance) {
        NotificationChannel callInviteChannel = null;
        callInviteChannel = new NotificationChannel(Constants.VOICE_CHANNEL_HIGH_IMPORTANCE,
                "Primary Voice Channel", NotificationManager.IMPORTANCE_HIGH);
        String channelId = Constants.VOICE_CHANNEL_HIGH_IMPORTANCE;

        if (channelImportance == NotificationManager.IMPORTANCE_LOW) {
            callInviteChannel = new NotificationChannel(Constants.VOICE_CHANNEL_LOW_IMPORTANCE,
                    "Primary Voice Channel", NotificationManager.IMPORTANCE_LOW);
            channelId = Constants.VOICE_CHANNEL_LOW_IMPORTANCE;
        }
        //-- Parse the users ringtone to the uri
        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        //--Set the notification sound based on the users ringtone
        callInviteChannel.setSound(ringtone, new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        callInviteChannel.setLightColor(Color.GREEN);
        callInviteChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(callInviteChannel);
        return channelId;
    }

    private void accept(CallInvite callInvite, int notificationId) {
        endForeground();
        // notify telephony service of call approval
        VoiceConnectionService.getConnection().setActive();
        // callInvite.accept(getApplicationContext(),)
    }

    private void reject(CallInvite callInvite) {
        endForeground();
        callInvite.reject(getApplicationContext());
        // notify telephony service of call rejection
        Connection cxn = VoiceConnectionService.getConnection();
        if (null != cxn) {
            cxn.setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
            VoiceConnectionService.releaseConnection();
        }
    }

    private void handleCancelledCall(Intent intent) {
        endForeground();
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleIncomingCall(CallInvite callInvite, int notificationId) {
        setCallInProgressNotification(callInvite, notificationId);
        // register new call with telecom subsystem
        // using the inviteBundle to get get the CallInvite object
        Bundle inviteBundle = new Bundle(CallInvite.class.getClassLoader());
        inviteBundle.putParcelable(Constants.INCOMING_CALL_INVITE, callInvite);
        Log.d(TAG, "Incoming call from " + callInvite);
        // Create a  bundle and intent to parse the data
        Bundle callInfo = new Bundle();
        // The uri is used to match the incoming phone number to the phone contacts
        // the bundle will be used to package the data to be sent to the telecom-manager
        Uri uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, callInvite.getFrom(), null); // Match the callee number to the phone contacts
        callInfo.putBundle(Constants.INCOMING_CALL_INVITE, inviteBundle);
        callInfo.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri);
        callInfo.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
        callInfo.putInt(TelecomManager.EXTRA_INCOMING_VIDEO_STATE, VideoProfile.STATE_AUDIO_ONLY);
        telecomManager.addNewIncomingCall(phoneAccountHandle, callInfo);
    }

    /**
     * This helper method uses the received intent phone number to grant permissions
     * to the telecom manager which checks for the phone number in the phoneAccount handle
     *
     * EXTRA_OUTGOING_CALL_EXTRAS: adds the received number from the intent to the callinfo
     * EXTRA_PHONE_ACCOUNT_HANDLE
     * create a bundle  a new
     * */
    private void handleOutgoingCall(Intent intent) {
        // place a call with the telecom subsystem
        final Bundle extra = intent.getExtras();
        if (null != extra) {
            Bundle callInfo = new Bundle();
            String calleePhone = extra.getString(Constants.OUTGOING_CALL_RECIPIENT);
            Log.d(TAG, "Callee number " + calleePhone);
            final Uri recipient = Uri.parse("tel:"+calleePhone);
            final int permissionsState =
                    ActivityCompat.checkSelfPermission(this,
                                                        Manifest.permission.MANAGE_OWN_CALLS);
            callInfo.putParcelable(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extra);
            callInfo.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
            callInfo.putInt(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_AUDIO_ONLY);
            if (permissionsState == PackageManager.PERMISSION_GRANTED) {
                telecomManager.placeCall(recipient, callInfo);
                Log.d(TAG, "Call placed using the telecom.place call");
            }
        }
    }


    private void endForeground() {
        stopForeground(true);
    }

    private void setCallInProgressNotification(CallInvite callInvite, int notificationId) {
        if (isAppVisible()) {
            Log.i(TAG, "setCallInProgressNotification - app is visible.");
            startForeground(notificationId, createNotification(callInvite, notificationId, NotificationManager.IMPORTANCE_LOW));
        } else {
            Log.i(TAG, "setCallInProgressNotification - app is NOT visible.");
            startForeground(notificationId, createNotification(callInvite, notificationId, NotificationManager.IMPORTANCE_HIGH));
        }
    }


    private boolean isAppVisible() {
        return ProcessLifecycleOwner
                .get()
                .getLifecycle()
                .getCurrentState()
                .isAtLeast(Lifecycle.State.STARTED);
    }
}
