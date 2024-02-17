package com.example.within.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.within.R;
import com.example.within.helpers.NavigationManager;
import com.example.within.profile.settings.SettingsActivity;

/** This object is the activity for the user profile page, it extends the AppCompatActivity so it can implement some of its methods.
 * */
public class ProfileFragment extends Fragment {
    /* Fields for the image buttons in the nav bar*/
    Button watch_ad_btn, setting_btn, addCreditBtn, credit_other_btn;
    Context appContext;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_page, container, false);
        appContext = view.getContext().getApplicationContext(); //-- Get the application context

        /* Initialise the views in the profile page and set */
        watch_ad_btn = view.findViewById(R.id.watch_ads);
        setting_btn = view.findViewById(R.id.settings);
        addCreditBtn = view.findViewById(R.id.add_credit_btn);
        credit_other_btn = view.findViewById(R.id.credit_others);


        /*
         * Listens to the buttons in the profile page
         * */
        setting_btn.setOnClickListener(v -> {
            NavigationManager.navigateToPage(appContext,SettingsActivity.class);
        });

        // Set the listener to the add credit activity so when this button is clicked
        // the user is taken to the add credit activity
        addCreditBtn.setOnClickListener(v -> {
            if (addCreditBtn != null){
                NavigationManager.navigateToPage(appContext, AddCreditActivity.class);
            }
        });

        return view;
    }


}
