package com.example.within.interfaces;

import androidx.annotation.NonNull;

import com.example.within.calllog_activities.phone_recent.PhoneRecentCallModel;

import java.util.List;
/**This interface is used to add more functionalities between the app recent calls,
 * the phone recent call and the call log. ths interface when implemented is used to
 * trigger the bottom sheet in the Recent Fragment to open a call log sheet with the
 * contacts call history of the model in view
 *
 * this interface uses a wildcard params for the list and the an object model
 * type to enable any class use this interface passing the required object required
 * and specify the type the list would contain
 *
 * Only the recentModel and the phoneRecentModel would work with this interface*/
public interface CallLogDisplayListener {
    void displayCallLog (@NonNull Object model, List<?> modelList);
}
