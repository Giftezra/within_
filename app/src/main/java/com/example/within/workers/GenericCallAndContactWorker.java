package com.example.within.workers;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.within.R;
import com.example.within.calllog_activities.app_recent.RecentModel;
import com.example.within.calllog_activities.phone_recent.PhoneRecentModel;
import com.example.within.contacts.ContactModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is the worker class that would get the users contact list, phone call log and the app call log
 * the class is a generic class expecting a type T of object which would be defined when called to perform
 * tasks. the doWork method in this worker would only perform work based on the object type passed
 * as argument to the generic type params .
 * the class implements the comparable interface so the lists would be compared to other lists which
 * will enable me to use the collections interface to sort through the users contactsList and callLog*/
public class GenericCallAndContactWorker extends Worker implements Comparable{
    // Create  a list of strings to hold the return values for the super
    // list

    //--Create list for the different object types expected
    private static List<RecentModel> recentModelList;
    private static int  CALL_REPEATED =  0;
    private static List<PhoneRecentModel> phoneRecentModelList;
    private static List<ContactModel> contactModelList, filteredContact;
    private Set<String> uniqueContacts;
    private final Context appContext;

    public GenericCallAndContactWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        appContext = context.getApplicationContext(); // Get the context for the object where this worker will run
    }

    @NonNull
    @Override
    public Result doWork() {
        ContentResolver contentResolver = appContext.getContentResolver(); // Get the contentResolver
        Data input = getInputData(); //--Get the input type sent from the calling classes
        String modelType = input.getString("objectType"); //--get the input key

        //-- Perform tasks based on the type of input received
        if (contentResolver != null) {
            switch (Objects.requireNonNull(modelType)){
                case "PhoneLogModel":
                    queryCallLog(contentResolver);
                    break;
                case "ContactModel":
                    queryContacts(contentResolver); //--Get the users contact list from their device
                    break;
            }
            return Result.success(); // --Return success is the query has ran successfully
        }else {
            return Result.failure();
        }
    }

    private void queryCallLog(ContentResolver contentResolver) {
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
                        phoneRecentModelList.add(new PhoneRecentModel(
                                name,
                                number,
                                R.drawable.ic_person,
                                formatDuration(duration),
                                false,
                                callType(type),
                                formattedDate

                        ));
                    } else {
                        phoneRecentModelList.add(new PhoneRecentModel(
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


    private String formatCallLogDate(long dateInMillis) {
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

    private boolean isSameDay(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }


    /**
     * This method is a helper static method which takes the param
     * @param  model of RecentModel type and adds it to the recentModelList which will
     * be updated in the app recent call log recycler view. the method checks if the
     * model list contains the incoming model object before adding the list to the
     * recent model*/
    public static void addRecentCalls(RecentModel model){
        //-- Returns a boolean that checks the list using a streams
        //-- to ensure the list does not contain the same model with similar
        //-- phone numbers and call type
        if (recentModelList != null) {
            boolean containsNumber = recentModelList.stream()
                    .anyMatch(existingList -> existingList.getReciepientNumber()
                            .equals(model.getReciepientName())
                            && existingList.getCallType().equals(model.getCallType()));

            if (!containsNumber) { //-- If the recent models do not have the same signature, add the new model to the list
                recentModelList.add(model);
            }else {
                CALL_REPEATED++; //-- Increment the call timer
            }
        }
    }


    public static List<RecentModel> getRecentModelList() {
        return recentModelList;
    }

    /*
    * Method is used to retrieve the number of times the call was repeated
    * and this number will be attached to the phone number on the recycler
    * indicating the number of times the call was repeated based on the call type
    * so if a callee number has been dialled twice and has called the user twice,
    * the adapter would display +4412222211 (2) missed or +4412222211 (2) dialed*/
    public static int getCallRepeated() {
        return CALL_REPEATED;
    }

//    /*
//    * Methods returns the list of unique models which are models that are already
//    * present in the recent call log. the unique list will then be used to populate
//    * call log of the specific phone number when the user want to see more call details*/
//    public static List<PhoneRecentModel> getPhoneLogUniqueList() {
//
//        List<PhoneRecentModel> phoneLogUniqueList = phoneRecentModelList.stream()
//                .filter(distinctByPhoneNumberAndCallType())
//                .distinct();
//
//        return phoneLogUniqueList;
//    }

    private static Predicate<PhoneRecentModel> distinctByPhoneNumberAndCallType() {
        Set<String> seen = new HashSet<>();
        return p -> seen.add(p.getReciepientNumber() + p.getCallType());
    }

    //--helper method that returns a list of the users phone contact after
    //--creation in the do work method call. this method will be called
    //--from the calling class after workflow has been returned with a work
    //--state of SUCCESS
    public static List<PhoneRecentModel> getPhoneRecentModelList() {
        return phoneRecentModelList;
    }

    //--Helper method takes the long duration params to calculate the call durations
    //--and returns a series of strings based on the call duration
    private String formatDuration(long duration) {
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

    //--Helper method takes the call calltype as an integer because the call log returns
    //--an int as call types. the method returns a string with messages based on
    //--the call type int  value
    private String callType(int type) {
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

    /* Method to get contacts */
    private void queryContacts(ContentResolver contentResolver) {
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

//    final void processContactList(List<ContactModel> contact) {
//        filteredContact = new ArrayList<>();
//        uniqueContacts = new HashSet<>();
//
//        filteredContact = contact;
//
//
//    }

    public static List<ContactModel> getContactModelList() {
        if (contactModelList != null){
            List<ContactModel> uniqueContact = contactModelList.stream()
                    .limit(contactModelList.size())
                    .filter(GenericCallAndContactWorker::isValidContact)
                    .distinct()
                    .collect(Collectors.toList());
            return uniqueContact;
        }
        return null;
    }

    private static boolean isValidContact(ContactModel contact) {
        // Add your validation logic here based on your requirements
        // For example, check if the name and phone number are not empty or null
        return contact != null &&
                contact.getPhoneNumber() != null && contact.getName() != null;
    }


    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
