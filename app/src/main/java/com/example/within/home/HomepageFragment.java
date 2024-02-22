package com.example.within.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.within.R;
import com.example.within.helpers.UserDataUpdateTask;
import com.example.within.initial_pages.WelcomeActivity;
import com.example.within.user.User;
import com.example.within.workers.AccessTokenWorker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class HomepageFragment extends Fragment implements LocationListener {
    private Context appContext;
    private static final String TAG = HomepageFragment.class.getSimpleName();
    public RelativeLayout mainHomeLayout;
    private ProfileUpdateSheet profileUpdateSheet;
    public static FirebaseUser customer;
    private String USER_ID;
    private static DatabaseReference userRef;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1; // location permission code
    private final int MANAGE_OWN_CALLS = 1;
    private EditText confirmPasswordField, passwordField, phoneField;
    private Handler handler;
    private NavigationView navigationView;
    private DrawerLayout mainDrawer;
    private Toolbar homeBar;
    private ActionBarDrawerToggle toggle;
    // Create an instance of the textview for the
    // preference nav and edit profile nav
    private TextView user, logoutText, userCurrency, userBalance, userNameText;
    private final Stack<Integer> menustack = new Stack<>();
    private String userName = ""; // Initialize the users first name to null


    // This methi=od is used to get the country from the current location of the user and update
    // immediately to the database
    private String getCountryCodeFromLocation(Location location) {
        if (appContext != null) {
            // Implement logic to get country code from location (you may use Geocoder or other methods)
            // For example:
            if (isAdded()) {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    assert addresses != null;
                    if (!addresses.isEmpty()) {
                        return addresses.get(0).getCountryCode();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "An error occurred while getting the user location " + e);
                }
            }
        }
        return null;
    }


    // Will be used to get the users currency type based on the country
    // code generated
    private String getCurrency(String country) {
        switch (country) {
            case "GB":
                return "gbp";
            case "NG":
                return "ngn";
            case "CA":
                return "cad";
            default:
                return "usd";
        }
    }

    private boolean checkLocationPermission() {
        // Check if the location permission is granted
        return ContextCompat.checkSelfPermission(
                appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        // Request location permission
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }


    /* Overridden method of the location listener which gives the app the functionality
        to update the users current location when changed.
        this method will be used to handle the users db passing
        the necessary data to the database
       */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Check is the fragment is still connected to a context first
        String countryCode = getCountryCodeFromLocation(location); // Get the country code of the user (eg US, GB, CA, NG)
        if (countryCode != null) {

            String currencyCode = getCurrency(countryCode); // Get the currency by passing the country code
            // Checks if the database has been instantiated or if the user reference has the correct db
            if (userRef != null) {
                // update the database using a hashmap to map the strings to their objects
                // eg (country and currency)
                Map<String, Object> userUpdate = new HashMap<>();
                userUpdate.put("country", countryCode);
                userUpdate.put("currency_type", currencyCode);
                // Update the database and implement the add complete listener to indicate when the
                // update has succeeded
                userRef.updateChildren(userUpdate).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "successfully updated the currency and country code");
                    } else {
                        Log.d(TAG, "error updating the user db");
                    }
                });
            }
        }

    }

    private void initializeViews(View view) {
     /*  Initialize the profile image image view
         Initialize side navigation views
         the initialize side nav, the preference nav the change  number nav
         Initialize the text views in the side bar nav */
        mainDrawer = view.findViewById(R.id.main_home);
        navigationView = view.findViewById(R.id.navigation_view);
        user = view.findViewById(R.id.user_name); //todo check this
        userCurrency = view.findViewById(R.id.currency_type);
        userBalance = view.findViewById(R.id.balance_text);
        homeBar = view.findViewById(R.id.home_bar);
        mainHomeLayout = view.findViewById(R.id.main_home_layout);
        userNameText = view.findViewById(R.id.user_fullname);// Todo check this

    }


    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && checkLocationPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }


    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                menustack.push(navigationView.getMenu().getItem(0).getItemId());// Push the menu into the stack
                if (itemId == R.id.profile) {
                    navigationView.getMenu().clear();// Clear  the initial menu item
                    navigationView.inflateMenu(R.menu.update_profile_menu); // inflate the profile menu
                    mainDrawer.openDrawer(GravityCompat.START, true);
                    // If the user clicks the preference nav open and inflate the associated menu
                } else if (itemId == R.id.preference_navigation) {
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.preference_menu);
                    mainDrawer.openDrawer(GravityCompat.START, true); // Close the drawer

                } else if (itemId == R.id.notifications) {
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.notification_menu);
                    mainDrawer.openDrawer(GravityCompat.START, true);

                } else if (itemId == R.id.manage_sim) {
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.manage_sim);
                    mainDrawer.openDrawer(GravityCompat.START, true);
                } else {
                    handleProfileSheet(itemId);
                    mainDrawer.closeDrawer(GravityCompat.START);
                    syncToggleState();
                }

                return true;
            }
        };
    }

    /*
     * Helper method sed to transverse the menus on the drawer navigation
     * -- popping out the item on the stack and initializes the local previous*/
    final void navigateBack() {
        if (!menustack.isEmpty()) {
            int previousMenuItemId = menustack.pop(); // Pop the top item from the stack
            navigationView.setCheckedItem(previousMenuItemId); // Navigate to the previous menu item
        } else {
            // If the stack is empty, close the drawer
            mainDrawer.closeDrawer(GravityCompat.START, true);
        }
    }

    /*
     * Helper method is used to reset the navigation menu when closed.
     * --this means that the view will refresh displaying the parent
     * menu when the drawer is re-opened*/
    final void resetNavigationViewMenu() {
        // Clear the current menu and inflate the original menu
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.side_menu); // Replace "original_menu" with the ID of your original menu
    }


    /**
     * Method signature is used to handle the backpresssed listener for this activity when fired
     * --it takes no params but initialized a couple of fields locally.
     * <p>
     * --returns a new onBackPressedCallback with instructions on how to handle the
     * --listener
     */
    final OnBackPressedCallback onBackPressedCallback() {
        final long DOUBLE_PRESS_INTERVAL = 2000; // 2 seconds
        final long[] lastPressTime = {0};
        return new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastPressTime[0] > DOUBLE_PRESS_INTERVAL) {
                    // First press within the interval
                    Toast.makeText(getContext(), "Press again to exit", Toast.LENGTH_SHORT).show();
                    lastPressTime[0] = currentTime;
                } else {
                    // Second press within the interval, perform the exit action
                    navigateBack();
                    // requireActivity().finish(); // Or navigate to the previous screen as needed
                }

                if (mainDrawer.isDrawerOpen(GravityCompat.START)) {
                    mainDrawer.closeDrawer(GravityCompat.START, true);
                }

            }
        };
    }


    /**
     * Helper method used to create a new bottomSheetFragment for the user profile update
     * -- when called the method checks for the argment passed in the params and if the
     * --id matches the required id, the sheet is created
     *
     * @param id
     */
    private void handleProfileSheet(int id) {
        if (isAdded()) {
            if (profileUpdateSheet != null && profileUpdateSheet.isVisible()) {
                profileUpdateSheet.dismiss();
            } else {
                profileUpdateSheet = new ProfileUpdateSheet(id, appContext, mainHomeLayout);
                profileUpdateSheet.show(requireActivity().getSupportFragmentManager(), "ModalBottomSheet");
            }
        }
    }



    final void setUpUser() {
        // Create an instance of the firebase user using the currently signed in user
        // check if the user is valid on the system and if valid get the user id.
        // get the userRef by passing the reference to the (Users) collection
        // and its child user node where the id is the id of the current user
        customer = FirebaseAuth.getInstance().getCurrentUser();// Initializes the user bases on the currently signed in user
        if (customer != null) { // Checks if the user is valid
            // Get the user id of the currently signed in user and use it to
            // Query the firebase realtime db at the (user table in the User collection
            USER_ID = customer.getUid(); // Get the current user id
            userRef = FirebaseDatabase.getInstance().getReference("Users").child("user").child(USER_ID);
//            if (USER_ID != null ) startTokenWork(USER_ID);// Start the work to generate and save the token

        }
    }

    /**
     * This method handles the intialization and implementations of the token worker class which
     * is used to fetch the users accesstoken from the server by passing the users firebase uID
     * as argument to the method params
     */
    final void startAccessTokenWorker(String userId) {
        // Since the worker class requires the class to not be an inner class a data builder
        // will be used to create a data map with the userod tag and the user id
        Data inputData = new Data.Builder()
                .putString("userId", userId)  // Example parameter
                .build();

        // Sets the constraint for the worker to a network type connected which means the
        // worker would run only on the ground there is a valid internet connection
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Sets the periodic workrequest of the AccessTokenWorker to 12hours
        // even if the user is not online
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder
                (AccessTokenWorker.class, 12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build();
        Log.d(TAG, "Token Retrieval work started Work started");

        WorkManager.getInstance(requireContext()).enqueue(workRequest);
    }


    final void syncToggleState() {
        toggle = new ActionBarDrawerToggle(
                getActivity(),
                mainDrawer, homeBar,
                R.string.open_drawer,
                R.string.close_drawer);

        mainDrawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState(); // Sync the toggel to enable the toggle icons

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setUpUser();
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        appContext = requireContext().getApplicationContext(); // Get the current application context
        handler = new Handler(); // Initialize a new handler

        /* Start the token worker immediately the activity starts to ensure every user has
        * a new token every 12 hour immediately after login*/
        startAccessTokenWorker(USER_ID);

        /* Request user permission to manage own calls when the starts...
        * //Todo this should be re-done and put into the welcome activity*/
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.MANAGE_OWN_CALLS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.MANAGE_OWN_CALLS}, MANAGE_OWN_CALLS);

       /*  Create and initialize a new ActionBarToggle to open and close the drawer when clicked.
         the object takes the drawer-layout, actionbar, and other objects to build the new toggle
         calling the sync state to begin the process using a method reference*/
        initializeViews(view);
        requireActivity().runOnUiThread(this::syncToggleState);

        /* Set the listener to the navigation view to listen to menu and sub-menu clicks
        * firing up the required methods */
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener());

        /* Set the listener to the drawer xml which holds the navigation view
        * the view resets when the user closes the drawer layout */
        mainDrawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                super.onDrawerClosed(drawerView);
                // Reset the navigation menu when the drawer is closed
                resetNavigationViewMenu();
            }
        });

        // Sets the listener for the users back action when fired
        // the activity is required since this is a fragment, before calling the
        // dispatcher method which adds a call back that requires the onBackPressedCallBack
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), onBackPressedCallback());
        // Method call sets the listeners for the views in the side bar
        //setSideBarNavListeners();
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            // Request location updates
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        }

        // Call the async task to execute its methods which is design to get the users
        // balance from the firebase realtime db based on the current user uid.
        // updates the balance and currency accordingly

        UserDataUpdateTask userDataUpdateTask = new UserDataUpdateTask(
                getContext(),
                userBalance,
                userCurrency
        );
        userDataUpdateTask.execute();
        return view;
    }


    /*
     * Attaches the context to the application context to avoid could break
     * if the context is nout found when the fragment is reattached code works
     * bad
     * */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (isAdded() && appContext == null) {
            appContext = context.getApplicationContext();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (appContext != null && getActivity() != null) {
            if (isAdded()) {
                startLocationUpdates();
            }
        }
    }


    public static class ProfileUpdateSheet extends BottomSheetDialogFragment {
        private final int id;
        private final
        Context context;
        private
        Button submitPasswordButton, submitNewMobileButton;
        private RelativeLayout mainHomeLayout;
        protected static
        EditText newPhoneField, newPasswordField, confirmPassword;


        public ProfileUpdateSheet(int id, Context context, RelativeLayout mainLayout) {
            this.id = id;
            this.context = context;
            this.mainHomeLayout = mainLayout;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = null;
            if (id == R.id.change_password) {
                view = inflater.inflate(R.layout.fragment_change_password, container, false);
                newPasswordField = view.findViewById(R.id.new_password_field);
                confirmPassword = view.findViewById(R.id.confirm_password);
                submitPasswordButton = view.findViewById(R.id.submit_new_password);
            } else if (id == R.id.change_number) {
                view = inflater.inflate(R.layout.fragment_change_phone, container, false);
                newPhoneField = view.findViewById(R.id.new_phone_field);
                submitNewMobileButton = view.findViewById(R.id.submit_new_number);
            }

            //--set listeners to the button when fired
            if (submitNewMobileButton != null) {
                submitNewMobileButton.setOnClickListener(onClickListener());
            }

            return view;

        }

        private View.OnClickListener onClickListener() {
            return v -> {
                int id = v.getId();

                if (id == R.id.submit_new_password) {
                    String newPassword = newPasswordField.getText().toString();
                    String confirm = confirmPassword.getText().toString();

                    if (newPasswordField == null || confirmPassword == null) {
                        Snackbar.make(mainHomeLayout, "Field must not be empty", Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    //--check if the details match
                    if (newPassword.equals(confirm) && newPassword.startsWith("+")) {
                        changePasswordHelper(newPassword);
                    } else if (!newPassword.equals(confirm)) {
                        newPasswordField.setError("Password don't match");
                    }

                    /* Checks for button clicked id if it matches the required submit button*/
                } else if (id == R.id.submit_new_number) {
                    String newMobile = newPhoneField.getText().toString();
                    // Check the value of the mobile number entered for improper tags
                    if (!newMobile.startsWith("+")) {
                        Snackbar.make(mainHomeLayout, "Mobile  number must begin with dial-code characters (+)",
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        // Perform task
                        changeRegisteredMobileHelper(newMobile);
                    }

                }
            };

        }

        final void changePasswordHelper(String newPassword) {
            customer.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) { // Confirms the password was successfully changed before proceeding
                        Log.d(TAG, "Succuss updating password");
                        Snackbar.make(mainHomeLayout, "Password changed successfully",
                                        Snackbar.LENGTH_LONG)
                                .show();
                    } else {
                        // show a message to the user to contact support
                        // Todo in later version users would be unabble to change password again after three failed attempts
                        Snackbar.make(mainHomeLayout, "Error while changing password... Please contact us", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


        final void changeRegisteredMobileHelper(String newMobile) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) { // Checks if the user exists with the given id in the db
                        // and create a new user object to set the new user phone
                        User currentUser = snapshot.getValue(User.class);
                        // Checks if the current user is invalid before setting the new phone
                        if (currentUser != null) {
                            currentUser.setPhone_number(newMobile);
                            userRef.setValue(currentUser).addOnCompleteListener(task -> {// Set the new value to the database column
                                // and checks if the task is successful before displaying a toast mesage to the user
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Successfully updated your phone number "
                                                            + currentUser.getPhone_number(),
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                } else {
                                    Toast.makeText(context, "Error updating phone number", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}
