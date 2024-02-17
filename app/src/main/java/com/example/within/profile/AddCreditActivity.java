package com.example.within.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.within.R;
import com.example.within.helpers.Keys;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;
/** This activity handles the users payment session with stripe*/
public class AddCreditActivity extends AppCompatActivity {
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration customerConfig;
    private final String TAG = "Payment Activity";

    // Stripe credentials
    private final String publishable_key = Keys.STRIPE_PUBLISHABLE_KEY;
    private final String secret_key = Keys.STRIPE_SECRET_KEY;

    // The radio group holds the radio buttons for the users amount
    RadioGroup amountRadioGroup;
    Button submit;
    private TextView amountTosend;
    private static String amount;

    private void onPaymentSheetResult(
            final PaymentSheetResult paymentSheetResult
    ) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, "Payment success", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Display for example, an order confirmation screen
            Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendTransaction(int amount) {
        // Set up the headers for the POST request
        String apiUrl = "https://0fea-92-40-195-214.ngrok-free.app/payment-sheet";
        String queryString = "?amount=" + amount; // Add more parameters as needed
        String fullUrl = apiUrl + queryString;

        Fuel.INSTANCE.post(fullUrl, null).responseString(new Handler<String>() {
                    @Override
                    public void success(String s) {
                        try {
                            final JSONObject result = new JSONObject(s);
                            customerConfig = new PaymentSheet.CustomerConfiguration(
                                    result.getString("customer"),
                                    result.getString("ephemeralKey")
                            );
                            paymentIntentClientSecret = result.getString("paymentIntent");
                            PaymentConfiguration.init(getApplicationContext(),
                                    result.getString("publishableKey"));
                            presentPaymentSheet();
                        } catch (JSONException e) { /* handle error */ }
                    }

                    @Override
                    public void failure(@NonNull FuelError fuelError) {
                        // TODO implement the failure when returned
                    }
                });

    }


    private void presentPaymentSheet() {
        final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Vhotis, Inc.")
                .customer(customerConfig)
                // Set `allowsDelayedPaymentMethods` to true if your business handles payment methods
                // delayed notification payment methods like US bank accounts.
                .allowsDelayedPaymentMethods(true)
                .build();
        paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret,
                configuration
        );
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_credit);
        PaymentConfiguration.init(getApplicationContext(), Keys.STRIPE_PUBLISHABLE_KEY);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        // Get the button clicks by setting a listener to button click
        // to get the radio button
        amountRadioGroup = findViewById(R.id.amount_radio_group);
        submit = findViewById(R.id.add_credit_button);
        amountTosend = findViewById(R.id.show_deposit);
        // Get the customers radio button choice to set the customers top up amount
        amountRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            int selectedAmount = -1;
            @SuppressLint("SetTextI18n")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                if (radioButton != null) {
                    String selectedAmountString = radioButton.getText().toString();
                    amount = selectedAmountString;
                    amountTosend.setText("Your account will be funded with " + selectedAmountString);
                    try {
                        selectedAmount = Integer.parseInt(selectedAmountString);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        // Handle the case where the parsing fails
                    }
                }
            }
        });


        submit.setOnClickListener(v -> {
            amount = amount.replaceAll("[^0-9]", "");
            int depositAmount = Integer.parseInt(amount);
            sendTransaction(depositAmount);
        });

    }

}