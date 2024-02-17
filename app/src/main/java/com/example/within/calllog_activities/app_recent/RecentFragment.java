package com.example.within.calllog_activities.app_recent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.within.R;
import com.example.within.calllog_activities.phone_recent.PhoneCallLogAdapter;
import com.example.within.calllog_activities.phone_recent.PhoneRecentModel;
import com.example.within.workers.GenericCallAndContactWorker;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

public class RecentFragment extends Fragment {
    private final String TAG = "RECENT FRAGMENT";
    private Context appContext;
    private RecyclerView recentRecyclerView, phoneCallLogRecycler;
    private final int READ_CALL_LOG_PERMISSION = 1;
    private List<RecentModel> appRecentCallLogList;
    private  List<PhoneRecentModel> phoneCallLogList;// Populate this list with your data
    private RecentAdapter recentAdapter;
    private ProgressBar phoneRecentProgress;
    private PhoneCallLogAdapter phoneCallLogAdapter;
    private RelativeLayout mainLayout;
    private TextView edit;
    private MaterialSwitch recentTypeSwitch;
    private ViewGroup.MarginLayoutParams phoneLayoutParams, layoutParams;

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Before creating the view i want to get the users permission to get thier phone contacts and this will
        // be used in the background thread to populate the phone dataList entries

        View view = inflater.inflate(R.layout.fragment_recent_page, container, false);
        appContext = requireActivity().getApplicationContext();

        phoneCallLogList = new ArrayList<>();
        appRecentCallLogList = new ArrayList<>();
        initializeViews(view);

        /*
        * The method is used to hide the visibility of the call log models when the switch button
        * is selected by the user*/
        recentTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (recentRecyclerView.getVisibility() == View.VISIBLE){
                    recentRecyclerView.setVisibility(View.GONE);
                    phoneCallLogRecycler.setVisibility(View.VISIBLE);
                }else{
                    phoneCallLogRecycler.setVisibility(View.GONE);
                    recentRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });


        recentRecyclerView.setLayoutManager( new LinearLayoutManager(appContext));
        phoneCallLogRecycler.setLayoutManager(new LinearLayoutManager(appContext));

        layoutParams = (ViewGroup.MarginLayoutParams) recentRecyclerView.getLayoutParams();
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.recycler_margin);

        handleContactRetrievalWork();

        return view;
    }

    final void handleContactRetrievalWork (){
        //--Check if the call log permission has been granted before handling the
        //--phone call log retrieval
        Log.d(TAG, "Permission " + checkPermission());
        Data inputData = new Data.Builder()
                .putString("objectType", "PhoneLogModel")
                .build();
        Log.d(TAG, "Data Sent"+ inputData);

        // Create a work request that build a request for the callLogWorker
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(GenericCallAndContactWorker.class)
                .setInputData(inputData)
                .build();


        WorkManager workManager = WorkManager.getInstance(appContext);
        workManager.enqueue(workRequest);
        workManager.getWorkInfoByIdLiveData(workRequest.getId()) // -- start monitoring work flow
                .observe(getViewLifecycleOwner(), workInfo -> { // -- get the life-cycle-owner and use the lambda to run the work flow
                    phoneRecentProgress.setVisibility(View.VISIBLE); // Start the progress bar
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED){ //-- check the current work info against the State success
                        //--After work completion, get the returned list of the call log
                        //-- list using the static method of the generic call and contact worker
                        phoneCallLogList = GenericCallAndContactWorker.getPhoneRecentModelList();
                        phoneCallLogAdapter = new PhoneCallLogAdapter(phoneCallLogList);
                        phoneCallLogRecycler.setAdapter(phoneCallLogAdapter);
                        phoneCallLogAdapter.notifyDataSetChanged();

                        appRecentCallLogList = GenericCallAndContactWorker.getRecentModelList();
                        recentAdapter = new RecentAdapter(appRecentCallLogList);
                        recentRecyclerView.setAdapter(recentAdapter);
                        recentAdapter.notifyDataSetChanged();
                        phoneRecentProgress.setVisibility(View.GONE);
                    }
                });
    }

    final boolean checkPermission(){
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED;
    }

    /* Requests the user contact permission to read the phone contact list */
    private void requestPermissions() {
        // Request both Read_call log and SEND_SMS permissions
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.WRITE_CALL_LOG
                },
                READ_CALL_LOG_PERMISSION
        );
    }

    final void initializeViews(View view){
        recentRecyclerView = view.findViewById(R.id.recent_recycler_view);
        phoneCallLogRecycler = view.findViewById(R.id.incall_recent_recycler);
        recentTypeSwitch = view.findViewById(R.id.recent_type);
        phoneRecentProgress = view.findViewById(R.id.phone_recent_progress);
        mainLayout = view.findViewById(R.id.main_recent_layout);
    }

    /*
    * -- Helper method used to create a work request to fetch the users call log
    * -- calls the generic class which is the object that retrieves the users
    * -- call log by using the input data sent to decide which work to perform
    *
    * -- the work flow is monitored until the work state shows success before the
    * -- static list returned in the worker can be retrieved and used to populate
    * -- phoneDataList
    * */
    final void handlePhoneCallLogWork(){


    }


    @Override
    public void onResume() {
        super.onResume();
        phoneCallLogAdapter = new PhoneCallLogAdapter(phoneCallLogList);
        phoneCallLogRecycler.setAdapter(phoneCallLogAdapter);
        phoneCallLogAdapter.notifyDataSetChanged();
    }




//
//
//    public class CallLogFragment extends BottomSheetDialogFragment {
//
//        //-- Initialize an empty constructor
//        public CallLogFragment(){
//
//        }
//
//        @Nullable
//        @Override
//        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//            View view = inflater.inflate(R.layout.call_log_list, container, false);
//            return view;
//        }
//
//        @Override
//        public void onStart() {
//            super.onStart();
//            Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).
//                    setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        }
//    }


}

