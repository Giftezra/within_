package com.example.within.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This class and its workers are background worker which i will be used to fetch the users access
 * token every 12hours if the user is active. generated tokens will be stored in the main class as
 * shared preference.
 * */
public final class AccessTokenWorker extends Worker {
    private final String TAG = getClass().getSimpleName();

    public AccessTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String userId = data.getString("userId");
        // Create a new http client
        String accessToken = fetchAccessToken(userId);
        if (accessToken != null){
            Log.d(TAG, "Access Token: " + accessToken);
            saveAccessToken(accessToken); // save to the shared preference
            return Result.success();
        }else{
            return Result.failure();
        }
    }

    /*
     * Helper method used to fetch the access token using the okhttp library to
     * make a request to the server to get the access token for the required
     * client or user bases on their userId*/
    private String fetchAccessToken (String userId){
        OkHttpClient client = new OkHttpClient(); // Create a new okhttp client

        String baseUrl = "https://1a27-147-147-235-29.ngrok-free.app/generateToken"; //-- Base url for calls

        //Build a request body that contains the userid tag and the userID
        // values to serve as request body to the server
        assert userId != null;
        RequestBody body = new FormBody.Builder()
                .add("userId", userId)
                .build();

        Log.d(TAG, "Token response. body: " + body);

        // Create  a new request passing the base url as params
        Request request = new Request.Builder()
                .url(baseUrl)
                .post(body)
                .build();
        Log.d(TAG, "token response. request Code: " + request);

        // Use a try catch block to catch the thrown errors before they break the code
        //create a new response and call the okhttp client.newclient with the
        // request
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String responseBody = response.body().string(); // Get the response body
                // Handle the response format based on server implementation
                // For example, if the response is a JSON object and the access
                // token is under a key named "accessToken"
                JSONObject json = new JSONObject(responseBody);
                String token = json.getString("accessToken");
                return token;
            }else {
                Log.d(TAG, "Error with token response. HTTP Code: " + response.code());

            }
            response.close();
        }catch (IOException | JSONException ioe){
            Log.d(TAG, "Error with token response");
        }
        return null;
    }


    // Helper method used to store the access token as a sharedprefenrence to enhance
    // user token for extra security
    private void saveAccessToken(String accessToken) {
        // Store the access token in SharedPreferences
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("within_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("access_token", accessToken);
        editor.apply();
    }
}