package com.example.within.interfaces;

import androidx.annotation.NonNull;

import com.example.within.calllog_activities.app_recent.RecentModel;

/**
 * Interface is used to listen to call updates within the app. any class that implements this sd*/
public interface AppRecentCallLogListener {

    void onItemAdded( RecentModel model);
}
