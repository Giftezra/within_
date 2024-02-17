package com.example.within.initial_pages;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.within.R;
import com.example.within.helpers.NavigationManager;
import com.example.within.user.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executor;


public class WelcomeActivity extends AppCompatActivity {
    private static final int PERMISSIONS_ALL = 1;
    private static DatabaseReference userRef;
    private static FirebaseUser user;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo; // Biometric prompt_info reference
    private static String userName;

    private TextView welcomeText;
    private Button signIn, signOut;
    private ImageView logoImageView;


    public static void getUserData(DatabaseReference userRef) {
        if (user != null) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //--if the user exist get their first name
                    if (snapshot.exists()) userName = snapshot.child("first_name").getValue(String.class);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("UserDataAsyncTask", "Database error: " + error.getMessage());
                }
            });
        }
    }

    public static String getUserName() {
        return userName;
    }

    private void initiateBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this); // Initialize the executor object

        biometricPrompt = new BiometricPrompt(WelcomeActivity.this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // Takes the user to the login activity when they chose to login with password instead
                        NavigationManager.navigateToPage(WelcomeActivity.this, LoginActivity.class);
                        finish();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Intent mainActivityIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                        mainActivityIntent.setAction("UPDATE_USER_NAME"); // Set the action
                        mainActivityIntent.putExtra("user_name",userName); // Set the action
                        startActivity(mainActivityIntent);
                        finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(), "Authentication failed",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        //-- Create a prompt passing the title and the userName
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Welcome back " )
                .setDescription("Sign in using fingerprint")
                .setNegativeButtonText("Use password instead")
                .build();
    }

    /*
    * Helper listenr to sign the user out if signed.
    * This method is only fired when the user clicks the signout button on the welcome page
    * and it takes the user back to the login activity*/
    private View.OnClickListener signOut(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null){
                    FirebaseAuth.getInstance().signOut();
                    NavigationManager.navigateToPage(WelcomeActivity.this, LoginActivity.class);
                }
            }
        };
    }

    static private Map<String, String> providePermissionsMesageMap() {
        return new HashMap<String, String>() {{
            put(Manifest.permission.ACCESS_FINE_LOCATION,
                    "Access fine location permission needed. Please allow in your application settings.");
            put(Manifest.permission.ACCESS_COARSE_LOCATION,
                    "Access coarse location permission needed. Please allow in your application settings.");
            put(Manifest.permission.READ_CONTACTS,
                    "Manage Contacts permission needed. Please allow in your application read contact.");
            put(Manifest.permission.READ_CALL_LOG,
                    "Without read call permission app will fail to use call log.");
            put(Manifest.permission.WRITE_CALL_LOG,
                    "Without call log permission app will fail to use call log.");
        }};
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        final Map<String, String> permissionsMessageMap = providePermissionsMesageMap();
//        for (String permission : providePermissions()) {
//            if (hasPermissions(this, permission)) {
//                /*
//                 * Due to bluetooth permissions being requested at the same time as mic
//                 * permissions, AudioSwitch should be started after providing the user the option
//                 * to grant the necessary permissions for bluetooth.
//                 */
//                if (!permission.equals(Manifest.permission.BLUETOOTH_CONNECT)) {
//                    Toast.makeText(this,
//                            Objects.requireNonNull(permissionsMessageMap.get(permission)),
//                            Toast.LENGTH_LONG).show();
//                } else {
//                    //startAudioSwitch();
//                    registerForCallInvites();
//                }
//            }
//        }
//    }


    private void startLogoRotationAnimation() {
        // Create an ObjectAnimator to animate the rotation property of the logoImageView
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(logoImageView, "rotation", 0f, 360f);
        // Set the duration of the rotation animation in milliseconds
        rotationAnimator.setDuration(2000); // 1// seconds
        // Start the rotation animation
        rotationAnimator.start();
    }

    /*
     * Helper method is used to provide client with the required permissions to make a phone call
     * because the call to the callee would be interrupted or not go through if these permissions
     * are not granted.
     *
     * The method uses a list of type string to create a new vector of type string*/
    static private String[] providePermissions() {
        List<String> permissionsList = new Vector<String>() {{
            add(Manifest.permission.READ_CALL_LOG);
            add(Manifest.permission.ACCESS_FINE_LOCATION); // <- Add for different behavior
            add(Manifest.permission.ACCESS_COARSE_LOCATION);
            add(Manifest.permission.WRITE_CALL_LOG);
            add(Manifest.permission.READ_CONTACTS);
        }};
        String[] list = new String[permissionsList.size()];
        return permissionsList.toArray(list);
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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            String userId = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child("user").child(userId);
        }
        getUserData(userRef);

        setContentView(R.layout.activity_welcome_back);

        // Check for the user
        welcomeText = findViewById(R.id.welcome_text);
        signIn = findViewById(R.id.login_using_bio);
        signOut = findViewById(R.id.log_out);
        logoImageView = findViewById(R.id.welcome_logo);

        String[] permissionsList = providePermissions();
        if (hasPermissions(this, permissionsList)) {
            ActivityCompat.requestPermissions(this, permissionsList, PERMISSIONS_ALL);
        }else{
            signIn.setOnClickListener(v -> {
                initiateBiometricPrompt();
                biometricPrompt.authenticate(promptInfo);
            });
        }

        runOnUiThread(()->welcomeText.setText(R.string.welcome)); //--write a welcome message to the user
        // Code is fired when the user clicks the sign in button.
        // verifies the users biometrics before signing the user in
        // taking them to the main activity is biometrics is valid
        signIn.setOnClickListener(v -> {
            initiateBiometricPrompt();
            biometricPrompt.authenticate(promptInfo);
        });
        signOut.setOnClickListener(signOut());
        startLogoRotationAnimation();
    }


    // Always asks the user to authenticate using their biometrics if the activity was has resumed
    // and in cases where the user is already null, get the current user and reassign.
    @Override
    protected void onResume() {
        if (user == null){
            user = FirebaseAuth.getInstance().getCurrentUser();
        }
        super.onResume();
        if (user != null){
            initiateBiometricPrompt();
        }else{
            Toast.makeText(getApplicationContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    // While the activity is paused check if the user is null and reAuthenticate the user
    @Override
    protected void onPause() {
        super.onPause();
        if (user == null){
            user = FirebaseAuth.getInstance().getCurrentUser();
        }
    }
}
