package com.example.within.calllog_activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.within.R;
import com.example.within.calllog_activities.app_recent.RecentModel;
import com.example.within.calllog_activities.phone_recent.PhoneRecentCallModel;
import com.example.within.calls.CallTaskActivity;
import com.example.within.contacts.ContactModel;
import com.example.within.interfaces.AppRecentCallLogListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class CallLogger extends AppCompatActivity implements AppRecentCallLogListener, Comparable{
    private final String TAG = getClass().getSimpleName();
    private static List<RecentModel> recentModelList;
    private static int  CALL_REPEATED =  0;
    private static List<PhoneRecentCallModel> phoneRecentModelList;
    private static List<PhoneRecentCallModel> frequentlyCalledList;
    private static List<ContactModel> contactModelList, filteredContact;
    private CallTaskActivity callTaskActivity;



    //--Helper method takes the long duration params to calculate the call durations
    //--and returns a series of strings based on the call duration
    private static String formatDuration(long duration) {
        // Format duration as HH:mm:ss
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;
        if (hours <= 0 && minutes <= 0) {
            return seconds + "sec";
        }else if (hours <= 0){
            return minutes + "min" + seconds + "s";
        }else {
            return hours + "h" + minutes + "m" + seconds + "s";
        }
    }



    /** --Helper method takes the call calltype as an integer because the call log returns
    --an int as call types. the method returns a string with messages based on
    --the call type int  value
     */
    private static String callType(int type) {
        switch (type) {
            case 1:
                return "incoming";
            case 2:
                return "out going";
            case 3:
                return "missed";
            case 4:
                return "declined";
            case 5:
                return "busy";
        }
        return null;
    }




    private static String formatCallLogDate(long dateInMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        long differenceMillis = currentTimeMillis - dateInMillis;

        // Check if the call log date is from the current day
        if (isSameDay(currentTimeMillis, dateInMillis)) {
            // Calculate the time difference in seconds, minutes, or hours
            if (differenceMillis < 60 * 1000) {
                long secondsDifference = differenceMillis / 1000;
                return secondsDifference + " sec ago";
            } else if (differenceMillis < 60 * 60 * 1000) {
                long minutesDifference = differenceMillis / (60 * 1000);
                return minutesDifference + " min ago";
            } else {
                long hoursDifference = differenceMillis / (60 * 60 * 1000);
                return hoursDifference + " hours ago";
            }
        } else if (differenceMillis < 24 * 60 * 60 * 1000) {
            // Less than 24 hours ago, display "Yesterday"
            return "Yesterday";
        } else {
            // Display the date in a more standard format for dates older than 24 hours
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return dateFormat.format(new Date(dateInMillis));
        }
    }

    private static boolean isSameDay(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }



    public static List<RecentModel> getRecentModelList() {
        return recentModelList;
    }

    public static List<ContactModel> getContactModelList() {
        return contactModelList;
    }


    private static Predicate<PhoneRecentCallModel> distinctByPhoneNumberAndCallType() {
        Set<String> seen = new HashSet<>();
        return p -> seen.add(p.getReciepientNumber() + p.getCallType());
    }

    /*  helper method that returns a list of the users phone contact after
        creation in the do work method call. this method will be called
        from the calling class after workflow has been returned with a work
        state of SUCCESS
        */
    public static List<PhoneRecentCallModel> getPhoneRecentModelList() {
        return phoneRecentModelList;
    }



    /* Method to get contacts */
    public static void queryContacts(ContentResolver contentResolver) {
        contactModelList = new ArrayList<>(); // Initialize the contact list using an array

        /* Use a cursor to query the user contact log */
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Contactables.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        );
        Log.d("TAG", "cursor" + cursor);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                //--Retrieve data from the cursor and create ContactModel objects
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                @SuppressLint("Range") String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                //-- Create a new ContactModel and add it to the list
                ContactModel contact = new ContactModel(displayName, phoneNumber, false);
                contactModelList.add(contact);
                //processContactList(contactModelList);
            }
            cursor.close();
        }
    }



   public static void queryCallLog(ContentResolver contentResolver) {
        // Define the URI for the call log
        Uri callLogUri = CallLog.Calls.CONTENT_URI;

        // Specify the columns you want to retrieve
        String[] projection = {
                CallLog.Calls.DATE,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE,
                CallLog.Calls.CACHED_NAME
        };

        // Sort the results by date in descending order
        String sortOrder = CallLog.Calls.DATE + " DESC";

        // Make the query
        Cursor cursor = contentResolver.query(callLogUri, projection, null, null, sortOrder);

        if (cursor != null) {
            try {
                phoneRecentModelList = new ArrayList<>();

                // Iterate through the cursor to retrieve call log details
                while (cursor.moveToNext()) {

                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));

                    String formattedDate = formatCallLogDate(date);

                    // Check if the retrieved call log has a name attached to it and return an empty field
                    // for the name field if not found
                    if (name != null && number != null) {
                        phoneRecentModelList.add(new PhoneRecentCallModel(
                                name,
                                number,
                                R.drawable.ic_person,
                                formatDuration(duration),
                                false,
                                callType(type),
                                formattedDate

                        ));
                    } else {
                        phoneRecentModelList.add(new PhoneRecentCallModel(
                                null,
                                number,
                                R.drawable.ic_person,
                                formatDuration(duration),
                                false,
                                callType(type),
                                formattedDate
                        ));
                    }

                    // Do something with the call log details (e.g., print or process)
                    Log.d("CallLog:", " Date: " + formattedDate + ", Number: " + number +
                            ", Duration: " + duration + ", Type: " + type + ", Name:" + name );
                }
            } catch (IllegalArgumentException e){
                Log.d("Recent Fragment", "error fetching the user call log data " + e);
            }finally {
                // Close the cursor to free up resources
                cursor.close();
            }
        }
    }


    /*
    * Method signature us designed to filter through a given list using a for loop
    * and a map of integer, phoneRecentModel to keep track of models and their occurences
    * in the list. the model with the highest number of occurrences is added to the list
    * */
    public static void phoneLogFrequentlyCalled (){
        Map<PhoneRecentCallModel, Integer> frequentlyCalled = new HashMap<>();
        int maxOccurrence = -1;
        PhoneRecentCallModel recentModel;
        for (PhoneRecentCallModel model:phoneRecentModelList) {
            frequentlyCalled.put(model, frequentlyCalled.getOrDefault(model, 0) + 1);
        }

        for (Map.Entry<PhoneRecentCallModel, Integer> filteredEntry : frequentlyCalled.entrySet()){
            if (filteredEntry.getValue() > maxOccurrence){
                maxOccurrence = filteredEntry.getValue();
                recentModel = filteredEntry.getKey();
                frequentlyCalledList.add(recentModel);
            }
        }
    }


    public static List<PhoneRecentCallModel> getFrequentlyCalledList() {
        return frequentlyCalledList;
    }

    /**Overridden method from the AppRecent interface which returns a model when fired
     * the model is then added to the list of models
     * @param model is the model arg which any invoking class has to pass*/
    @Override
    public void onItemAdded(@NonNull RecentModel model) {
        Log.d(TAG, "Received Model " + model);
        recentModelList.add(model);
    }

    @Override
    public int compareTo(Object o) {
        if (!recentModelList.isEmpty()){

        }
        return 0;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        frequentlyCalledList = new ArrayList<>();
        callTaskActivity = new CallTaskActivity();
        callTaskActivity.setAppRecentCallListener(this);
    }
}
