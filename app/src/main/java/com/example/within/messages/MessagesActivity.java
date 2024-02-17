package com.example.within.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.within.R;
import com.google.android.material.navigation.NavigationView;

public class MessagesActivity extends AppCompatActivity {
    private NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.row_message_item); // this is just for debug  use the messages.xml

        navigationView = findViewById(R.id.send_message_view);
        View handler = navigationView.getHeaderView(0);
        View navHeader = LayoutInflater.from(this).inflate(R.layout.send_message, navigationView, false);
        navigationView.addHeaderView(navHeader);
    }
}
