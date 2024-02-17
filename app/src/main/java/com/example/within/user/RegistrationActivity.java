package com.example.within.user;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.within.R;
import com.example.within.helpers.NavigationManager;
import com.example.within.home.HomepageFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.PatternSyntaxException;

/** The registration activity handles the registrations page and all its backend service.
 * it extends the app compact activity and implements its onCreate method.
 * all methods in this activity are arrange in alphabetical order */
public class  RegistrationActivity extends AppCompatActivity {
    Spinner country_code; // Create the spinner object
    EditText firstNameField, lastNameField, emailField, phone, password, confirm_password; // Create the edit text object
    Button createAccount; // Create the user create account button
    TextView password_error;
    private CheckBox termsCheck;

    // Create firebase auth object
    private FirebaseAuth mAuth;

    /**
     * Method creates a new user when called.
     * the method uses other helper methods to implement some of the functions.
     * the data entered by the user is validated and called within the method to validate the users the datails and if it meets the
     * criteria, the user object initialized and passed the user data, thereby creating a new user object.
     * the new user is inserted into the database and redirected to the home page activity
     * */
    private void createUser() {
        // Check if the fields are empty or null before getting the values
        if (TextUtils.isEmpty(firstNameField.getText()) || TextUtils.isEmpty(lastNameField.getText())
                || TextUtils.isEmpty(emailField.getText()) || TextUtils.isEmpty(phone.getText())
                || TextUtils.isEmpty(password.getText())) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();// Show an alert message to the user
            return;
        }
        // Get the values in the user fields
        String first_name = firstNameField.getText().toString();
        String last_name = lastNameField.getText().toString();
        String email = emailField.getText().toString();
        String phone_number = phone.getText().toString();
        String password1 = password.getText().toString();
        double balance = 0.0; // Initialize a user balance to 0
        // Creates a new user using firebase authentication and firebase database to store
        // user data if the registration is successful
        mAuth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) { // If the users registration is a success create a new firebase
                // database table using the current user id to streamline the users connection
                // to other tables in the Users collection
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String u_id = firebaseUser.getUid(); // Generate a unique id for the user
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child("user").child(u_id); // Create a new firebase database object

                    // Create a new user object passing the users required arguments
                    // to the user object to initiate a new user
                    User user = new User(first_name, last_name,
                            email, phone_number,
                            balance);
                    user.setDate_created(ServerValue.TIMESTAMP); // Update the users date created passing the current server timestamp
                    // Add the data to the database and call its on-complete listener to know when then
                    // data is updated
                    dbRef.setValue(user)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    // Convert the epoch account creation date to a human readable language
                                    long timestamp = (long) user.getDate_created();
                                    String dateCreated = convertTimestampToDateString(timestamp);
                                    user.setDate_created(dateCreated);

                                    // Take the user to the homepage if the users page was created successfully
                                    NavigationManager.navigateToPage(RegistrationActivity.this, HomepageFragment.class);
                                    finish(); // Ensure the user is not taken back to the activity
                                    Log.d("Registration success", "User registered successfully");
                                } else {
                                    Toast.makeText(this, "Error updating the user the database", Toast.LENGTH_SHORT).show();
                                    Log.d("Registration activity", "Error registering the new user");
                                }
                            });
                }
                // Check if the user with the credentials is already been created
                // before redirecting the user to the login activity
            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                Toast.makeText(this, "User with the email already exists \nSign in instead", Toast.LENGTH_SHORT).show();
                NavigationManager.navigateToPage(RegistrationActivity.this, LoginActivity.class);
                finish();//Ensure the user does not return to the previous page when back is clicked
            } else {
                // Other registration failure, log the error
                Log.e("Registration Error", "Failed to register new user", task.getException());
                Toast.makeText(this, "Failed to register new user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    * The method signature is used to convert the epoch timestamp to a human
    * readable one. uses the ofEpochToMilis method to get the timestamp epoch signature
    * which would then be converted using the LocalDateTime object and the DateTimeFormatter
    * */
    private static String convertTimestampToDateString(long timestamp) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Instant instant = Instant.ofEpochMilli(timestamp);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

            // Format the date if needed
            DateTimeFormatter formatter = null;
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return localDateTime.format(formatter);
        }
        return null;
    }


    // TextWatcher object creates a new text watcher object calling its overr`ide methods
    // to listen to what happens before text changes and after
    // calls the validate_password method on the password field after text changes`
    private final TextWatcher passwordListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
            validatePassword();
        } // Validated the password after text change
    };
    // TextWatcher object creates a new text watcher object calling its override methods
    // to listen to what happens before text changes and after
    // calls the validate_password on the confirm password field
    private final TextWatcher confirmPasswordListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
            validatePassword();
        }
    };
    // Creates a text watcher for the phone number field which
    // listens to alert the user if the field is empty
    private final TextWatcher phoneListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
            String text = s.toString(); // Get the strings in the field
            if (text.isEmpty()){ // Checks if field is empty and sets an error
                phone.setError("Must not be empty");
            } else if (!text.matches("[0-9+]+")) {
                phone.setError("Contains invalid characters");
            }else {
                phone.setError(null);
            }
        }
    };
    /**
     * The method validates the user password making sure the user does not use their names.
     * makes sure the password is not less than 8 digits and contains digits,and at-least one block letter.
     * makes sure the passwords match.
     * */
    private void validatePassword() {
        String password1 = password.getText().toString(); // Get password value
        String password2 = confirm_password.getText().toString(); // Get confirm password value
        password_error = findViewById(R.id.passwords_do_not_match_text); // Get the password error text view
        // Initialize the first and last names
        firstNameField = findViewById(R.id.first_name_field);
        lastNameField = findViewById(R.id.last_name_field);

        // Check if the password and confirm password match
        if (!password1.equals(password2)) {
            confirm_password.setError(getString(R.string.passwor_not_match)); // Set error if not a match
            password_error.setText(getString(R.string.passwor_not_match));
            return; // exit the method if passwords don't match
        }
        // Checks if the password contains the first name or the last name values
        // by first getting the values
        if (firstNameField != null && lastNameField != null) {
            String first_name = firstNameField.getText().toString();
            String last_name = lastNameField.getText().toString();
            // Check if the password or the confirm password contains any of the name field
            // and set requires error.
            // use the return keyword to repeat the process again
            if (password1.toLowerCase().contains(first_name.toLowerCase()) ||
                    password1.toLowerCase().contains(last_name.toLowerCase())) {
                password.setError("Password cannot contain your name");
                password_error.setText(getString(R.string.password_error));
                confirm_password.clearFocus(); // Set the confirm password field
                return; // exit the method if password contains the name
            }
        }
        try {
            // Check if the password is less than 8 digits and contains an uppercase, lowercase, and numbers
            if (password1.length() < 8 || !password1.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+")) {
                password.setError(getString(R.string.password_error));
                password_error.setText(getString(R.string.password_error));
                password.clearFocus();
                confirm_password.clearFocus();
            }
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }
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
    /** Method is called immediately the register activity is activated
     * implements several listeners and method to handle the user registration
     * @param savedInstanceState
     * method */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance(); // Initialize a the firebase auth object
        FirebaseApp.initializeApp(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);




        // Initialize the required fields for the edit text views
        firstNameField = findViewById(R.id.first_name_field);
        lastNameField = findViewById(R.id.last_name_field);
        emailField = findViewById(R.id.email_field);
        phone = findViewById(R.id.phone_number_field);
        password = findViewById(R.id.password_field);
        confirm_password = findViewById(R.id.confirm_password_field);
        termsCheck = findViewById(R.id.term_check);
        createAccount = findViewById(R.id.register_button);


        runOnUiThread(()->{
            Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_eye);
            assert icon != null;
            icon.setBounds(0,0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            password.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
            confirm_password.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        });

        password.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable iconDrawable = ContextCompat.getDrawable(RegistrationActivity.this, R.drawable.ic_eye);
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

        country_code = findViewById(R.id.country_code_spinner); // Get the spinner view
        if (password != null && confirm_password != null) {
            password.addTextChangedListener(passwordListener); // Add change listener to the password field
            confirm_password.addTextChangedListener(confirmPasswordListener); // Add change listener to the confirm password field
        }
        // Set the listener to the phone field
        phone.addTextChangedListener(phoneListener);

        // Listens to the users submit button to submit all of the users data required.
        // the check ensures the user agrees to the terms of service first before proceeding
        // if the agrees to the terms the check change listener would ensure the user does
        // not agree to it only to cancel again....
        // if the check is toggled off, the button is disables
        createAccount.setEnabled(false);
        termsCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    createAccount.setEnabled(true);
                    createAccount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createUser();
                        }
                    });
                }else {
                    createAccount.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Agree to the terms of service to continue", Toast.LENGTH_SHORT).show();
                }
            }
        });

        String [] spinner_item = getResources().getStringArray(R.array.spinner_items);
        // Create an array adapter of type string with the current context,
        // androids layout spinner drop down,
        // spinner
        ArrayAdapter<String> country_code_adapter = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                spinner_item);
        country_code.setAdapter(country_code_adapter); // Set the spinner adapter
        // Set a listener to the spinner items and append it to the
        // textField view of phone number
        country_code.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item
                String selected_country_code = (String) parent.getItemAtPosition(position);
                phone = findViewById(R.id.phone_number_field);
                phone.setText("");
                phone.append(selected_country_code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
