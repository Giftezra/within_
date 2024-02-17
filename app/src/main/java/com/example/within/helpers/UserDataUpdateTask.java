package com.example.within.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * The class extends the AsyncTask object and implements several of its method to get some
 * of the users data to display them to the user , adding more convieniece for the user
 * */
@SuppressLint("StaticFieldLeak")
public class UserDataUpdateTask extends AsyncTask<Void, Void, Void> {
    String currency = "";
    String name = "";
    Double balance = 0.0;

    private final TextView userBalance;
    private final TextView userCurrency;

    public UserDataUpdateTask (Context context, TextView userBalance, TextView userCurrency){
        context = context.getApplicationContext();
        this.userBalance = userBalance;
        this.userCurrency = userCurrency;

        // Set the listener to the
    }


    @Override
    protected Void doInBackground(Void... voids) {
        getUserData();
        return null;
    }

    // Runs the method update ui in the postExecute method to update the ui after the task has been executed
    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        updateUi(currency, balance,
                userBalance, userCurrency);

        // T
    }



    public void getUserData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child("user").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        balance = snapshot.child("balance").getValue(Double.class);
                        currency = snapshot.child("currency_type").getValue(String.class);
                        name = snapshot.child("first_name").getValue(String.class);
                        updateUi(currency, balance,
                                userBalance, userCurrency);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("UserDataAsyncTask", "Database error: " + error.getMessage());
                }
            });
        }
    }
    /*
     * Helper method used to display the ui for the user balance and the currency type
     * of the user based on the country code
     * */
    // ...

    @SuppressLint("SetTextI18n")
    public void updateUi(String currency, double balance,
                         TextView userBalance, TextView currencyText) {
        // Make sure it runs on the ui thread
        userBalance.setText(String.valueOf(balance)); // Set the user balance

        if (currency != null) {
            if (currency.equals("gbp")) {
                currencyText.setText("£");
            } else if (currency.equals("usd")) {
                currencyText.setText("$");
            } else if (currency.equals("ngn")) {
                currencyText.setText("#");
            }
        }
    }

// ...

}