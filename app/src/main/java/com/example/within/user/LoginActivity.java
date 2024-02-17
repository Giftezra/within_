package com.example.within.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.within.R;
import com.example.within.helpers.NavigationManager;
import com.example.within.initial_pages.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executor;

/**
 * This is the user lgo
 */
public class LoginActivity extends AppCompatActivity {
    FirebaseUser user;
    private FirebaseAuth mAuth; // Reference to the firebase auth
    private DatabaseReference userRef;
    private ProgressBar loginProgress;
    private String name;
    private EditText password, email;
    private Button login_button, biometric_login_button;
    private TextView forgot_password;
    // Initialize the objects for the biometric authenticator
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo; // Biometric prompt_info reference


    /**
     * Create a dialog to show the user they are not on the system and the dialog would
     * have a button to redirects the user to register
     * @param context
     * @param title
     * @param message
     */
    private void showInvalidAccountAlertDialog(Context context, String title, String message) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle(title)
                .setMessage(message) // Message for the user
                .setPositiveButton("CREATE ACCOUNT", new DialogInterface.OnClickListener() {
                    // Navigate to the registration button if the positive button is activated
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // Redirect the user to the registration page
                        NavigationManager.navigateToPage(context, RegistrationActivity.class);
                    }
                })
                .setNegativeButton("TRY AGAIN", new DialogInterface.OnClickListener() {
                    // dismiss the dialog if the dismiss is activated and take the user back to the
                    // login page to try again
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("EXIT", new DialogInterface.OnClickListener() {
                    // The button closes the app and takes the user to the landing page
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the activity
                        ((Activity) context).finishAffinity();
                        // Terminate the app
                        System.exit(0);
                    }
                })
                .setIcon(R.drawable.ic_info)
                .show();
    }
    /*
    * Method displays a dialog that inflates the forgotten password xml which is called when the user
    * clicks the forgotten password  button.*/
    private void showForgottenPasswordDialog(){
        runOnUiThread(()-> {
            // Create and instance of the alert dialog and inflate the
            // forgotten password xml using the view class getLayoutInflater method
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.fragment_forgotten_password, null);
            dialogBuilder.setView(dialogView);
            // Get the users email address using the dialog view
            EditText email_field = dialogView.findViewById(R.id.forgotten_email);
            dialogBuilder.setTitle("Forgotten password");
            dialogBuilder.setMessage("Enter the correct registered email to reset your password");
            dialogBuilder.setIcon(R.drawable.ic_info);
            // Set a listener to the positive button to listen when clicked.
            // use firebase to send the password reset link to the user
            dialogBuilder.setPositiveButton("Reset password", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String email = email_field.getText().toString();// Get the user email address
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){ // Check if the email was sent
                                Toast.makeText(getApplicationContext(), "Password reset email sent successfully", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                }
            });
            dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                // The negative button closes the dialog when clicked
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            // Initialized with the dialogbuilder create method to create a dialog
            // which will be displayed
            dialogBuilder.show();
        });
    }

    /*
    * Sets the listeners to the views on the login page so they fire when clicked.
    * this method will*/
    @SuppressLint("ClickableViewAccessibility")
    private void setListeners(){

        // Prompt dialog appears when the user clicks the login button
        login_button.setOnClickListener(v -> {
            userLogin();
        });
        // Listens to the forgotten password click before calling the dialog
        // that will redirect the user
        forgot_password.setOnClickListener(v -> {
            showForgottenPasswordDialog();
        });

        // Add a click listener to the icon
        password.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable iconDrawable = ContextCompat.getDrawable(LoginActivity.this, R.drawable.ic_eye);
                // Check if the touch event is within the bounds of the icon
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    assert iconDrawable != null;
                    if (event.getRawX() >= (password.getRight() - iconDrawable.getBounds().width())) {
                        // Icon clicked, handle the event here
                        // For example, show a Toast message
                        togglePasswordVisibility();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void togglePasswordVisibility() {
        int inputType = password.getInputType();
        if (inputType == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // If it's a password field, show the password
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            // If it's visible, make it a password field again
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        // Move the cursor to the end of the input to ensure correct display
        password.setSelection(password.getText().length());
    }
    /**
     * Method verifies the users data against the data from the system and logs the user in if valid,
     * otherwise it displays a dialog for the user to create a new account
     */
    private void userLogin() {
        // Check if the fields are empty
        if (TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(password.getText())) {
            Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the values of the user phone and password
        String user_email = email.getText().toString();
        String user_password = password.getText().toString();
        mAuth.signInWithEmailAndPassword(user_email, user_password).addOnCompleteListener(this, task -> {
            // Check if the user logged in success then open the homepage activity
            if (task.isSuccessful()) {
                NavigationManager.navigateToPage(LoginActivity.this, MainActivity.class);
                finish(); // Ensure the user does not go back to the login page
            } else {
                Toast.makeText(this, "Unauthorized user create account", Toast.LENGTH_SHORT).show();
                showInvalidAccountAlertDialog(this, "Unauthorized user", "Could not find the user with the provided email address on our systems. \nTry again with" +
                        "    new details or click the registration button to create an account.");
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this); // Initialize the firebase object
        user = FirebaseAuth.getInstance().getCurrentUser(); // get the current user
        mAuth = FirebaseAuth.getInstance(); // Initialize the firebase authentication


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        password = findViewById(R.id.login_password);
        email = findViewById(R.id.email_field);
        login_button = findViewById(R.id.login_button);
        biometric_login_button = findViewById(R.id.biometric_login);
        forgot_password = findViewById(R.id.forgotten_password);
        loginProgress = findViewById(R.id.login_progress);


        runOnUiThread( ()-> {
            Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_eye);
            icon.setBounds(0,0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            password.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        });


//        // Initiate the users biometric prompt builder when clicked
//        // using the biometric authenticator and biometric builder with an executor
//        // to run the thread
//        executor = ContextCompat.getMainExecutor(this); // Initialize the executor object
//        biometricPrompt = new androidx.biometric.BiometricPrompt(LoginActivity.this,
//                executor,
//                new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
//                    @Override
//                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
//                        super.onAuthenticationError(errorCode, errString);
//                        Toast.makeText(getApplicationContext(), "Authentication error" + errString,
//                                        Toast.LENGTH_SHORT)
//                                .show();
//                    }
//                    // Overridden method handles the action performed when biometric authentication is a success
//                    // Taking the user to the hompage activity and terminated the activity from returning to
//                    // the login page when the user goes back
//                    @Override
//                    public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
//                        super.onAuthenticationSucceeded(result);
//                        NavigationManager.navigateToPage(LoginActivity.this, MainActivity.class);
//
//                    }
//
//                    // Overridden method of biometric objecgt checks if the authentication failed state and returns
//                    // a toast message to the user
//                    @Override
//                    public void onAuthenticationFailed() {
//                        super.onAuthenticationFailed();
//                        Toast.makeText(getApplicationContext(), "Authentication failed",
//                                        Toast.LENGTH_SHORT)
//                                .show();
//                    }
//                });
//        // Make a prompt that will display different texts and views on
//        // the biometric dialog
//        promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("Welcome back")
//                .setDescription("sign in using fingerprint")
//                .setNegativeButtonText("Use password instead")
//                .build();
//        // Get the current user using the firebaseUser object
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseUser != null) {
//            // If the user is already signed in use the biometric scanner to verify their
//            // identity and take them to the homepage
//            biometricPrompt.authenticate(promptInfo);
//        }

        setListeners();

    }



    @Override
    protected void onResume() {
        super.onResume();
        if (user != null){
            String userId = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child("user").child(userId);
        }

    }
}
