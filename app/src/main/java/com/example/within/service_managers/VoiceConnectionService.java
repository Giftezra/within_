package com.example.within.service_managers;

import static android.telecom.TelecomManager.PRESENTATION_ALLOWED;
import static com.example.within.calls.CallTaskActivity.ACTION_DISCONNECT_CALL;
import static com.example.within.calls.CallTaskActivity.ACTION_DTMF_SEND;
import static com.example.within.calls.CallTaskActivity.DTMF;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.within.helpers.Constants;

public class VoiceConnectionService extends ConnectionService {
    private static final String TAG = "VoiceConnectionService";
    private static Connection activeConnection;

    public static Connection getConnection() {
        return activeConnection;
    }

    public static void releaseConnection() {
        if (null != activeConnection) {
            activeConnection.destroy();
            activeConnection = null;
        }
    }


    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Connection incomingCallConnection = createConnection(request);
        incomingCallConnection.setRinging();
        return incomingCallConnection;
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount,
                                                 ConnectionRequest request) {
        Connection outgoingCallConnection = createConnection(request);
        outgoingCallConnection.setDialing();
        return outgoingCallConnection;
    }

    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request);
        Uri callee = request.getAddress(); //-- Get the callee number from the connection request when call fails to connect
        Toast.makeText(getApplicationContext(), "Unable to place to " + callee, Toast.LENGTH_SHORT).show();
    }

    private Connection createConnection(ConnectionRequest request) {
        activeConnection = new Connection() {

            @Override
            public void onShowIncomingCallUi() {
                super.onShowIncomingCallUi();
            }

            @Override
            public void onHold() {
                super.onHold();
                activeConnection.setOnHold();
            }

            @Override
            public void onUnhold() {
                super.onUnhold();
                activeConnection.onUnhold();
            }

            @Override
            public void onStateChanged(int state) {
                if (state == Connection.STATE_DIALING) {
                    final Handler handler = new Handler();
                    handler.post(() -> sendCallRequestToActivity(Constants.ACTION_OUTGOING_CALL));
                }
            }

            @Override
            public void onCallAudioStateChanged(CallAudioState state) {
                Log.d(TAG, "onCallAudioStateChanged called, current state is " + state);
            }

            @Override
            public void onPlayDtmfTone(char c) {
                Log.d(TAG, "onPlayDtmfTone called with DTMF " + c);
                Bundle extras = new Bundle();
                extras.putString(DTMF, Character.toString(c));
                activeConnection.setExtras(extras);
                final Handler handler = new Handler();
                handler.post(() -> sendCallRequestToActivity(ACTION_DTMF_SEND));
            }

            @Override
            public void onDisconnect() {
                super.onDisconnect();
                activeConnection.setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
                releaseConnection();
                final Handler handler = new Handler();
                handler.post(() -> sendCallRequestToActivity(ACTION_DISCONNECT_CALL));
            }

            @Override
            public void onSeparate() {
                super.onSeparate();
            }

            @Override
            public void onAbort() {
                super.onAbort();
                activeConnection.setDisconnected(new DisconnectCause(DisconnectCause.CANCELED));
                releaseConnection();
            }

            @Override
            public void onAnswer() {
                super.onAnswer();
                activeConnection.setActive();
            }

            @Override
            public void onReject() {
                super.onReject();
                activeConnection.setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
                activeConnection.destroy();
            }

            @Override
            public void onPostDialContinue(boolean proceed) {
                super.onPostDialContinue(true);
            }
        };
        // setup the origin of the caller
        String callee = request.getExtras().getString(Constants.OUTGOING_CALL_RECIPIENT);
        String calleeName = request.getExtras().getString(Constants.RECIPIENT_NAME);
        Log.d("Voice connection service", "Outgoing call request " + callee);
        final Uri recipient = Uri.parse(callee);
        Log.d("Voice connection service", "Outgoing call recipient " + recipient);
        if (recipient != null) {
            activeConnection.setAddress(recipient, PRESENTATION_ALLOWED);
            // self managed isn't available before version O
            activeConnection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
            // set mute capability (for DTMF support?)
            activeConnection.setConnectionCapabilities(Connection.CAPABILITY_MUTE);
            activeConnection.setConnectionCapabilities(Connection.CAPABILITY_MANAGE_CONFERENCE);
            activeConnection.setConnectionCapabilities(Connection.CAPABILITY_HOLD);
            activeConnection.setConnectionCapabilities(Connection.CAPABILITY_SUPPORT_HOLD);
            activeConnection.setConnectionCapabilities(Connection.CAPABILITY_MERGE_CONFERENCE);
            activeConnection.setConnectionCapabilities(Connection.CAPABILITY_RESPOND_VIA_TEXT);
            activeConnection.setConnectionCapabilities(Connection.CAPABILITY_ADD_PARTICIPANT);
            activeConnection.setCallerDisplayName(calleeName, PRESENTATION_ALLOWED);

        } else {
            activeConnection.setAddress(request.getAddress(), PRESENTATION_ALLOWED);
        }

        return activeConnection;
    }

    /*
     * Send call request to the VoiceConnectionServiceActivity
     */
    private void sendCallRequestToActivity(String action) {
        Log.d("Voice connection service", "Outgoing call action " + action);
        Intent intent = new Intent(action);
        Bundle extras = new Bundle();
        if (activeConnection != null && activeConnection.getExtras() != null) {
            switch (action) {
                case Constants.ACTION_OUTGOING_CALL:
                    Uri address = activeConnection.getAddress();
                    extras.putParcelable(Constants.OUTGOING_CALL_RECIPIENT, address);
                    Log.d("Voice connection service", "Address address: " + address);
                    Log.d("Voice connection service", "Address extra: " + extras);

                    intent.putExtras(extras);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    break;
                case ACTION_DISCONNECT_CALL:
                    extras.putInt("Reason", DisconnectCause.LOCAL);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Log.d("Voice connection service", "Disconnect reason: " + extras);
                    intent.putExtras(extras);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    break;
                case ACTION_DTMF_SEND:
                    String d = activeConnection.getExtras().getString(DTMF);
                    extras.putString(DTMF, activeConnection.getExtras().getString(DTMF));
                    intent.putExtras(extras);
                    Log.d("Voice connection service", "Active dtmf: " + extras);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    break;
                default:
                    break;

            }
        }
    }


}

