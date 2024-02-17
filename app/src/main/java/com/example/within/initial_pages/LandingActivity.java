package com.example.within.initial_pages;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.within.R;
import com.example.within.helpers.NavigationManager;
import com.example.within.user.LoginActivity;
import com.example.within.user.RegistrationActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LandingActivity extends AppCompatActivity {
    private final String TAG = "Landing Activity";
    private FirebaseUser user;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int RECORD_AUDIO_PERMISSION= 1;

    Button login_button, sign_up_with_phone, emailSignUp, googleSignUp;

    String currencyCode = null;
    // Method called when the permission is requested .
    // uses a switch to handle algorithms for when the location has been granted or denied
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                login_button.setEnabled(true);
                sign_up_with_phone.setEnabled(true);
            } else {
                showInformationDialog();
            }
        }
    }

    // Display a dialog to explain to the user why they have to allow location permission
    // this dialog will contain a link to the terms and conditions page hosted on the server side
    // the click of the link would send a post request to the server side code
    private void showInformationDialog (){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Permission")
                .setMessage("This app requires user permission to work \nYou can read our terms and conditions \nhere")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() { // Listens to the ok button for when the
                    // user agrees to the permission
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Retry the permission if the user agrees
                        ActivityCompat.requestPermissions(LandingActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                })
                .setIcon(R.drawable.ic_info)
                .setNegativeButton("Refuse", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // When the click is triggered the activity is closed
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }


    private void setListeners (){
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationManager.navigateToPage(LandingActivity.this, LoginActivity.class);
            }
        });
        // Listener listens to the
        sign_up_with_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationManager.navigateToPage(LandingActivity.this, RegistrationActivity.class);
            }
        });
    }
    // Initialize the views in the activity when called.
    // This is for all the views in the activity including the adViews
    private void intializeViews (){
        login_button = findViewById(R.id.loginButton);
        sign_up_with_phone = findViewById(R.id.phone_signup);
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        setContentView(R.layout.activity_landing_page);
        // Check if the user has location permission granted and request it if they do not
        // because the location would be used to determine the currency which the users account
        // would work with and their
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        // Check if the user has granted the device permission to record audio, which will enable them communicate with
        // their call recipient, and request it if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION);
        }
        intializeViews();// Initialize the views on the ui
        // Disable the buttons until the user has granted location permissons
        login_button.setEnabled(true);
        sign_up_with_phone.setEnabled(true);
        setListeners(); // Set the listeners to the views
    }

    // Enable the buttons when the onResume method is invoked

    @Override
    protected void onResume() {
        super.onResume();
        if (user != null){
            login_button.setEnabled(true);
            sign_up_with_phone.setEnabled(true);
            NavigationManager.navigateToPage(LandingActivity.this, WelcomeActivity.class);
        }
    }
}
