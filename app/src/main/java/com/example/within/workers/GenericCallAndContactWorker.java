package com.example.within.workers;

import android.content.ContentResolver;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.within.calllog_activities.CallLogger;
import com.example.within.calllog_activities.app_recent.RecentModel;
import com.example.within.calllog_activities.phone_recent.PhoneRecentCallModel;
import com.example.within.contacts.ContactModel;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This is the worker class that would get the users contact list, phone call log and the app call log
 * the class is a generic class expecting a type T of object which would be defined when called to perform
 * tasks. the doWork method in this worker would only perform work based on the object type passed
 * as argument to the generic type params .
 * the class implements the comparable interface so the lists would be compared to other lists which
 * will enable me to use the collections interface to sort through the users contactsList and callLog*/
public final class GenericCallAndContactWorker extends Worker {
    // Create  a list of strings to hold the return values for the super
    // list

    //--Create list for the different object types expected
    private static List<RecentModel> recentModelList;
    private static int  CALL_REPEATED =  0;
    private static List<PhoneRecentCallModel> phoneRecentModelList;
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
                    CallLogger.queryCallLog(contentResolver);
                    break;
                case "ContactModel":
                    CallLogger.queryContacts(contentResolver); //--Get the users contact list from their device
                    break;
            }
            return Result.success(); // --Return success is the query has ran successfully
        }else {
            return Result.failure();
        }
    }

}
