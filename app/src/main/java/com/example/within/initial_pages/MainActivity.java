package com.example.within.initial_pages;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.within.R;
import com.example.within.calllog_activities.app_recent.RecentFragment;
import com.example.within.contacts.ContactFragment;
import com.example.within.helpers.Constants;
import com.example.within.calls.DialpadFragment;
import com.example.within.home.HomepageFragment;
import com.example.within.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int READ_CONTACTS_PERMISSION_REQUEST = 1;
    private static final int READ_CALL_LOG_PERMISSION = 1;
    private Intent intent;
    private final String userName = null;

    private Fragment selectedFragment = null;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent(); // Get the incoming intent
        // Ask the users permission to contacts, call log,
        // if the permissions are not already granted ask for the user to grant them
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},READ_CONTACTS_PERMISSION_REQUEST);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, READ_CALL_LOG_PERMISSION);
        }
        setContentView(R.layout.fragment_navigation);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        selectedFragment = new HomepageFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragments, selectedFragment)
                .commit();


        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home_navigation) {
                selectedFragment = new HomepageFragment();
            } else if (itemId == R.id.recent_navigation) {
                selectedFragment = new RecentFragment();
            } else if (itemId == R.id.dail_pad_nav) {
                selectedFragment = new DialpadFragment(intent);
            } else if (itemId == R.id.contacts_navigation) {
                selectedFragment = new ContactFragment();
            } else if (itemId == R.id.profile_navigation) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragments, selectedFragment)
                        .commit();
            }

            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When the page resumes get the intent sent to it.,, this code is put on this method because
        // no intent is being sent to this object until the user request a task from within the app
        // after the oncreate has been call
        intent = getIntent();
        if (intent != null){
            String action = intent.getAction(); // get the action flag sent
            if (Objects.equals(action, Constants.EDIT_BEFORE_CALL)){
                selectedFragment = new DialpadFragment(intent);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragments, selectedFragment)
                        .commit();
            }
        }else {
            selectedFragment = new HomepageFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragments, selectedFragment)
                    .commit();
        }
    }
}
