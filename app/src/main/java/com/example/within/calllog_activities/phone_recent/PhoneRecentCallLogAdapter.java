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

import java.util.ArrayList;
import java.util.List;

public class PhoneRecentCallLogAdapter extends ArrayAdapter<PhoneRecentModel> {
    private final String TAG = PhoneRecentCallLogAdapter.class.getSimpleName();
    private final Context context;
    private ImageView callTypeIcon;
    private TextView callTypeText, callDateTime, callDuration;
    private List<PhoneRecentModel> filteredList;
    private final List<PhoneRecentModel> originalList;


    public PhoneRecentCallLogAdapter(@NonNull Context context, List<PhoneRecentModel> list) {
        super(context, R.layout.adapter_call_log_item);
        this.context = context;
        this.filteredList = new ArrayList<>();
        this.originalList = list;
        Log.d(TAG, "Original list size " + originalList.size());
        Log.d(TAG, "Filtered list size " + filteredList.size());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view != null) {
            LayoutInflater.from(context.getApplicationContext())
                    .inflate(R.layout.adapter_call_log_item, parent, false);
        }else {
            Log.d(TAG, "View is : " + null);
        }

        callDateTime = view.findViewById(R.id.call_date_time);
        callTypeText = view.findViewById(R.id.call_type_text);
        callTypeIcon = view.findViewById(R.id.call_type_icons);
        callDuration = view.findViewById(R.id.call_log_duration);


        PhoneRecentModel currentItem = getItem(position);


        Log.d(TAG, "Original list size " + currentItem);
        if (currentItem != null){
            String contactNumber = currentItem.getReciepientNumber();
            String callType = currentItem.getCallType();
            filteredList = filter(contactNumber, callType);

            Log.d(TAG, "Original list size " + currentItem.getCallType() + currentItem.getRecentDate());


            currentItem = filteredList.get(position);
            Log.d(TAG, "Current filtered list size " +filteredList.size());
            callDuration.setText(currentItem.getCallDuration());
            //callTypeIcon.setImageIcon();
            callTypeText.setText(currentItem.getCallType());
            callDateTime.setText(currentItem.getRecentDate());
        }




        return view;
    }



    /*
    * Helper filter method is used to check the original list passed to the
    * adapter looking through all and returns model with the same phone number
    * and call type into the filtered list*/
    public List<PhoneRecentModel> filter(String number, String callType) {
        filteredList.clear();
        for (PhoneRecentModel model: originalList) {
            if(model.getCallType().equals(callType) &&
                    model.getReciepientNumber().equals(number)){
                filteredList.add(model);
                Log.d(TAG, "Model " + model);
            }
        }

        notifyDataSetChanged();
        return filteredList;
    }

    @NonNull
    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Nullable
    @Override
    public PhoneRecentModel getItem(int position) {
        return filteredList.get(position);
    }
}
