package com.example.within.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.within.R;
import com.example.within.helpers.NavigationManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/** This object is the activity for the user profile page, it extends the AppCompatActivity so it can implement some of its methods.
 * */
public class ProfileFragment extends Fragment {
    /* Fields for the image buttons in the nav bar*/
    private ImageButton watch_ad_btn, setting_btn, addCreditBtn, credit_other_btn;
    private Context appContext;
    private SettingNavigator settingNavigator;
    private FirebaseUser user;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*Create a new user before loading the view*/ user = FirebaseAuth.getInstance().getCurrentUser();
        
        View view = inflater.inflate(R.layout.fragment_profile_page, container, false);
        appContext = view
                .getContext()
                .getApplicationContext(); //-- Get the application context
        
        /*Create the reference of the settingNavigator class*/
        settingNavigator = new SettingNavigator(
                container,
                inflater, 
                user);
        
        



        /* Initialise the views in the profile page and set */
        watch_ad_btn = view.findViewById(R.id.watch_ads);
        setting_btn = view.findViewById(R.id.settings);
        addCreditBtn = view.findViewById(R.id.add_credit_btn);
        credit_other_btn = view.findViewById(R.id.credit_others);


        /*
         * Listens to the buttons in the profile page
         * */
        setting_btn.setOnClickListener(v -> {
            settingNavigator.initializeSettingView(v);
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

class SettingNavigator{


    private NavigationView settingsView;
    private View popupView;
    private PopupWindow settingsWindow;
    private ViewGroup root;
    private LayoutInflater inflater;
    private FirebaseUser user;

    SettingNavigator(ViewGroup root, LayoutInflater inflater, FirebaseUser user){
        this.root = root;
        this.inflater = inflater;
        this.user = user;
    }

    void initializeSettingView(View view){
        popupView = inflater.inflate(R.layout.row_settings_navigation, root);
        //-- Open a new window in the popup
        settingsWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true //--Stay focused until closed
        );

        settingsView = popupView.findViewById(R.id.settings_navigation);
        settingsView.setNavigationItemSelectedListener(onNavigationItemSelectedListener());
    }
    
    
    final NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener(){
        return new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();/* Get the item id of the menu items used in the navigation view */
                /* Checks for id validity then perform tasks based on id*/

                //--Todo ensure click is only around the icon
                if (itemId == R.id.settings_toolbar){
                    int id = R.drawable.ic_cancel;

                    if (settingsWindow.isFocusable()) {
                        settingsWindow.setFocusable(false);/*Close the popup window when X icon is clicked*/
                    }
                }


                return false;
            }
        };
        
    }
    
    

}

