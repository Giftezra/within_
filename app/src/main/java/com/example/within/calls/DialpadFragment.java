package com.example.within.calls;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.within.R;
import com.example.within.calls.CallTaskActivity;
import com.example.within.calls.PricingModel;
import com.example.within.helpers.Constants;
import com.example.within.messages.MessagesActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

/** The Activity handles the users dial pad and */
public class DialpadFragment extends Fragment {
    private PricingModel pricingModel;
    public static Handler handler;
    private DialpadSheet dialpadSheet;
    protected Intent intent;// Create a new intent to receive all incoming traffic
    private LinearLayout dial_pad;
    private FloatingActionButton dialpad, messages;
    public String intentnumber;

    //Constructor
    public DialpadFragment(Intent intent){
        this.intent = intent;
    }


    /*
    * Helper method used to handle the dial-pad sheet.
    * this method ensure the sheet is not null to avoid a null pointer error and
    * also checks if visible before dismissing the view but if the view is not null but
    * not visible it creates a new sheet, requires the parent activity and
    * displays a modelbottomsheet
    * */
    private void handleDialpadSheet(String number){
        if (isAdded()){
            if(dialpadSheet != null && dialpadSheet.isVisible()){
                dialpadSheet.dismiss();
            }else{
                dialpadSheet = new DialpadSheet(number);
                dialpadSheet.show(requireActivity().getSupportFragmentManager(), "ModalBottomSheet");
            }
        }
    }

    private void initializeViews(View view) {
        dialpad = view.findViewById(R.id.dialpad_fab);
        messages = view.findViewById(R.id.message_fab);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialpad_page, container, false);
        initializeViews(view);
        handler = new Handler(); // Handles the ui on the ui thread
        pricingModel = new PricingModel();

        // Check if the action from the intent requires the dialpad to be displayed when this page loads
        // get the value string from the incoming intent and pass in into the dialpad sheet
        String number;
        String intentAction = intent.getAction();
        if (intentAction != null && intentAction.equals(Constants.EDIT_BEFORE_CALL)) {
            number = intent.getStringExtra("phone_number");
            handleDialpadSheet(number);
        } else {
            number = "";
            dialpadSheet = new DialpadSheet(number);
        }
        dialpad.setOnClickListener(onClickListener(number));
        messages.setOnClickListener(onClickListener(number));
        return view;
    }


    /*
    * Sets the listener for touch event on the view
    * when the view is clicked the method fires the appropriate algorithm to
    * handle users request
    **/
    private View.OnClickListener onClickListener(String number){
        return v -> {
            int id = v.getId();
            // if the id matches with the dialpad fab, open the dialpad fragment
            // and display the dialpad sheet to the user
            if (id == R.id.dialpad_fab){
                handleDialpadSheet(number);
                // Create a new intent and pass
            } else if (id == R.id.message_fab) {
                Intent messageIntent = new Intent(getContext(), MessagesActivity.class);
                requireActivity().startActivity(messageIntent);
            }
        };
    }















    /*
    * I will be creating a member class to simply handle the building of a new bottom sheet which
    * will be used to display the dialpad when triggered or when an intent is sent from other classes
    * which may contian an intent sending an extra phone number that will be edited on the dailpad before
    * */
    public static class DialpadSheet extends BottomSheetDialogFragment {
        private Button one, two, three, four, five, six, seven, eight, nine, zero, asterix, hash;
        private EditText inputedNumbers;
        private ImageView deleteNumbers;
        private ImageButton callButton;
        private Context context;

        private long pressStartTime;
        private long pressDuration;
        private boolean isLongPress = false;

        private static StringBuilder enteredNumbers;
        private String number = null;


        // Create an empty constructor to initialize the view
        public DialpadSheet(String number){
            this.number = number;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_dialpad_sheet, container,false);// Create and return a new sheet when called
            enteredNumbers = new StringBuilder();
            context = view.getContext(); // Get the current context


            one = view.findViewById(R.id.one); two = view.findViewById(R.id.two); three = view.findViewById(R.id.three);
            four = view.findViewById(R.id.four); five = view.findViewById(R.id.five); six = view.findViewById(R.id.six);
            seven = view.findViewById(R.id.seven); eight = view.findViewById(R.id.eight); nine = view.findViewById(R.id.nine);
            asterix = view.findViewById(R.id.axterix); zero = view.findViewById(R.id.zero); hash = view.findViewById(R.id.hash);
            inputedNumbers = view.findViewById(R.id.entered_numbers);
            inputedNumbers.setInputType(InputType.TYPE_CLASS_NUMBER);
            deleteNumbers = view.findViewById(R.id.delete_numbers);
            callButton = view.findViewById(R.id.call_button);

            // Get the received numbers in the constructor and set it to the value of inputedNumbers
            if (number != null){
                enteredNumbers.append(number);
            }


            // Set listeners to trigger when their respective id or view is clicked
            deleteNumbers.setOnClickListener(v -> {
                deleteLastDigit();
            });

            // The listener calls the makecall method to initiate a call
            // Toast displays to the user that the call has been initiated.
            // used for debugging but could be left during production
            callButton.setOnClickListener(v -> {
                makeCall();
            });

            // Listens to all numeric buttons in the view appending their respective values to
            // the required view for calls to be placed to the number
            one.setOnClickListener(onClickListener());  two.setOnClickListener(onClickListener());
            three.setOnClickListener(onClickListener());  four.setOnClickListener(onClickListener());
            five.setOnClickListener(onClickListener());  six.setOnClickListener(onClickListener());
            seven.setOnClickListener(onClickListener());  eight.setOnClickListener(onClickListener());
            nine.setOnClickListener(onClickListener()); //zero.setOnClickListener(onClickListener());
            hash.setOnClickListener(onClickListener()); asterix.setOnClickListener(onClickListener());
            zero.setOnTouchListener(onTouchListener());


            return view;
        }

        /**
         * The onstart method is call when ever the sheet is being brought into focus
         * when the user open the sheet this code will ensure the sheets content matches its
         * windows layout params*/
        @Override
        public void onStart() {
            super.onStart();
            Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).
                    setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        }

        /*
        * Helper method used to place a call. the method gets the numbers entered
        * by the user or sent from other activities and sends the numbers to the callTaskActivity
        * where the call will be handles based on the constant passed to it */
        private void makeCall() {
            // Get the phone number entered in the field
            String phone = inputedNumbers.getText().toString();

           //Checks if the first character is '0' or contains '+' which indicates the international
            //dial code
            if (inputedNumbers != null) {
                if (phone.matches("^[0-9].*") || phone.startsWith("+")) {
                    // Create an intent using the current context to pass the data from this context
                    // to the next activity to handle the calls
                    Intent intent = new Intent(getContext(), CallTaskActivity.class);
                    intent.setAction(Constants.ACTION_OUTGOING_CALL); //Set the flag
                    intent.putExtra(Constants.OUTGOING_CALL_RECIPIENT, phone); // Send the phone to the next activity
                    requireActivity().startActivity(intent);
                    Toast.makeText(context, "Call placed....", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Number must contain proper dial code", Toast.LENGTH_SHORT).show();
                }
            }
        }



        private View.OnClickListener onClickListener(){
            return new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int id = view.getId();
                    if (id == R.id.one){
                        appendDigit("1");
                    }else if (id == R.id.two){
                        appendDigit("2");
                    } else if (id == R.id.three) {
                        appendDigit("3");
                    }else if (id == R.id.four){
                        appendDigit("4");
                    } else if (id == R.id.five) {
                        appendDigit("5");
                    }else if (id == R.id.six){
                        appendDigit("6");
                    } else if (id == R.id.seven) {
                        appendDigit("7");
                    }else if (id == R.id.eight){
                        appendDigit("8");
                    } else if (id == R.id.nine) {
                        appendDigit("9");
                    }else if (id == R.id.zero){
                        appendDigit("0");
                    } else if (id == R.id.hash) {
                        appendDigit("#");
                    }else if (id == R.id.axterix){
                        appendDigit("*");
                    } else if (id == R.id.delete_numbers) {
                        deleteLastDigit();
                    }

                }
            };
        }


        private Runnable longPressed() {
            return new Runnable() {
                @Override
                public void run() {
                    isLongPress = true;
                }
            };
        }

        // Helper method used to append digits or values to the string builder
        // updating the current display
        private void appendDigit(String digit) {
            if (digit != null && enteredNumbers != null) {
                enteredNumbers.append(digit);
                updateDisplay();
            }
        }

        // Helper method used to delete the numbers on the string
        // builder appended to the inputted number view
        // and updates the view
        private void deleteLastDigit() {
            if (enteredNumbers.length() > 0) {
                enteredNumbers.deleteCharAt(enteredNumbers.length() - 1);
                updateDisplay();
            }
        }

        // Helper method updates the display for the inputted numbers
        // setting the selections of the view to begin at the end of the number
        // so when the user delete a number the blinker appears at the point
        // where the user stopped
        private void updateDisplay() {
            int start = enteredNumbers.length();
            int stop = enteredNumbers.length();
            if(inputedNumbers != null && enteredNumbers != null){
                inputedNumbers.setText(enteredNumbers.toString());
                inputedNumbers.setSelection(start, stop);
                //Todo: set the code to delete or change numbers at the pointers current location
            }
        }


        private View.OnTouchListener onTouchListener() {
            return new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int id = v.getId();
                    if (id == R.id.zero) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                pressStartTime = System.currentTimeMillis();
                                isLongPress = false;
                                handler.postDelayed(() -> {
                                }, 1000); // 1 second delay for long press
                                break;

                            case MotionEvent.ACTION_UP:
                                pressDuration = System.currentTimeMillis() - pressStartTime;
                                handler.removeCallbacksAndMessages(null); // Remove any pending callbacks

                                if (pressDuration < 1000) {
                                    // If released before 1 second, it's a short click, do nothing (handled in ACTION_DOWN)
                                    appendDigit("0");
                                } else {
                                    // If held for 1 second or more, append "+"
                                    appendDigit("+");
                                }
                                break;
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            };
        }

    }

}


