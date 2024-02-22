package com.example.within.calllog_activities.phone_recent;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.within.R;
import com.example.within.calllog_activities.app_recent.RecentModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This adapter is designed to handle the users call log display by populating the
 * list and with items gotten from the currently focused model... this adapter checks the call log
 * list for the models filtering out into another list model with the same phone number signature and
 * call type.... the filtered list is then used to populate the the adapter models so that
 * when the user opens a call log on a model given the position, the call log his contain
 * that contacts call history with the user for the number of occurrences.*/
public class CallLogAdapter extends ArrayAdapter<Object> {
    private final String TAG = CallLogAdapter.class.getSimpleName();
    private final Context context;
    private Object model;
    private ImageView callTypeIcon;
    private TextView callTypeText, callDateTime, callDuration;
    private List<?> phoneFilteredList;
    private final List<?> originalList;


    public CallLogAdapter(@NonNull Context context, List<?> list) {
        super(context, R.layout.row_call_log_item);
        this.context = context;
        this.originalList = list;
        this.phoneFilteredList = new ArrayList<>();

        Log.d(TAG, "Original list size " + originalList.size());
        Log.d(TAG, "Filtered list size " + phoneFilteredList.size());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.row_call_log_item,
                            parent,
                            false);
            Log.d(TAG, "View created");
        }else {
            Log.d(TAG, "View is : " + null);
        }
        phoneFilteredList = filter(originalList);
        Log.d(TAG, "Phone list " + phoneFilteredList);
        model = getItem(position);


        assert convertView != null;
        callDateTime = convertView.findViewById(R.id.call_date_time);
        callTypeText = convertView.findViewById(R.id.call_type_text);
        callTypeIcon = convertView.findViewById(R.id.call_type_icons);
        callDuration = convertView.findViewById(R.id.call_log_duration);

        if (model instanceof PhoneRecentCallModel){
            PhoneRecentCallModel phoneCallModel = (PhoneRecentCallModel) model;
            callDateTime.setText(phoneCallModel.getRecentDate());
            callDuration.setText(phoneCallModel.getCallDuration());
            callTypeText.setText(phoneCallModel.getCallType());

        }


        return convertView;
    }


    /*
     * Helper filter method is used to check the original list passed to the
     * adapter looking through all and returns a model with the same phone number
     * and call type into the filtered list
     */
    public List<Object> filter(List<?> objectList) {
        /* Create a new map to monitor and control the models added to the list
         * Map contains a key (String) value (List of object) */
        Map<String, List<Object>> callLogMap = new HashMap<>();
        List<Object> newCallList = new ArrayList<>();

        /* Loop through the passed object type list checking for the instance type of the list passed
         * using the instanceOf method to check if the list passed */
        for (Object objectModel : objectList) {
            Log.d(TAG, "object model " + objectModel);
            if (objectModel instanceof PhoneRecentCallModel) {
                String number = ((PhoneRecentCallModel) objectModel).getReciepientNumber(); // Get the phone number
                if (!callLogMap.containsKey(number)) {
                    // Create a new list for the phone number if it doesn't exist in the map
                    List<Object> phoneList = new ArrayList<>();
                    phoneList.add(objectModel);
                    newCallList.add(objectModel);
                    callLogMap.put(number, phoneList);
                } else {
                    // If the phone number already exists in the map, add the model to the corresponding list
                    callLogMap.get(number).add(objectModel);
                }
            }
        }

        notifyDataSetChanged();
        return newCallList;
    }


    @NonNull
    @Override
    public int getCount() {
        return phoneFilteredList.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return phoneFilteredList.get(position);
    }
}
