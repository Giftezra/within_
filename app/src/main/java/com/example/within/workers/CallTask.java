package com.example.within.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CallTask  extends Worker {
    private final String TAG = getApplicationContext().getClass().getSimpleName();

    public CallTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String to = data.getString("to");
        Log.d(TAG, "callee number " + to);
        int statusCode = placeCall(to);
        if (statusCode == 200) {
            return Result.success();
        }else {
            return Result.failure();
        }
    }

    /*
     * Helper method used to fetch the access token using the okhttp library to
     * make a request to the server to get the access token for the required
     * client or user bases on their userId*/
    private int placeCall (String to){
        OkHttpClient client = new OkHttpClient(); // Create a new okhttp client
        // Create  a new request passing the base url as params
        String baseUrl = "https://1a27-147-147-235-29.ngrok-free.app/make-call";

        //Build a request body that contains the userid tag and the userID
        // values to serve as request body to the server
        assert to != null;
        //-- Create a request body initialized with a new form builder
        //-- add the recipient number (to)
        RequestBody body = new FormBody.Builder()
                .add("to", to)
                .build();

        Log.d(TAG, "Token response. body: " + to);

        Request request = new Request.Builder()
                .url(baseUrl)
                .post(body)
                .build();
        Log.d(TAG, "token response. request Code: " + request);

        // Use a try catch block to catch the thrown errors before they break the code
        // create a new response and call the okhttp client.newclient with the
        // using a Response object to get the response body from the request if successful
        // returning a status code 200 if the call was created successfully on the server side
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                Log.d(TAG, "Server returned HTTP Code: " + response.code());
                return response.code();
            }else {
                Log.d(TAG, "Error with token response. HTTP Code: " + response.code());
            }
        }catch (IOException ioe){
            Log.d(TAG, "Error with token response");
        }
        return -1;
    }

}
