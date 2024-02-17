package com.example.within.profile.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.within.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    DatabaseReference userRef;
    FirebaseDatabase database;
    /* Fields for the image buttons in the nav bar*/
    ImageButton contact_btn, dialpad_btn, home_btn, profile_btn, recent_btn;
    ToggleButton notificationToggleButton;

    // Change password view, about view change number view
    TextView change_number_button, about_button, change_password_button, userEmail, userPhone;

    /*
    * Initialize the views on the page. this was only done to add clarity
    * to the codes
    * */
    private void initializeViews(){

        userEmail = findViewById(R.id.email);
        userPhone = findViewById(R.id.phone);

    }



    // Uses the NotificationCompact builder to build a notification object
    private void showNotification (String Title, String message){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
    }

    /*
    * Update the user email using the firebase user to get the currently signed
    * in users email.
    * run a thread on the ui to initialize the email view
    * */
    @SuppressLint("SetTextI18n")
    private void setEmailInSettings (){
        // Check if the user is null before
        if (firebaseUser != null){
            String email = firebaseUser.getEmail();
            runOnUiThread(() ->{
                userEmail.setText("Email: " +email);
            });
        }
    }


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setEmailInSettings();

    }
}
