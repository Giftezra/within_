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
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
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
import com.example.within.calllog_activities.CallLogger;
import com.example.within.calllog_activities.phone_recent.CallLogAdapter;
import com.example.within.calllog_activities.phone_recent.PhoneRecentCallLogAdapter;
import com.example.within.calllog_activities.phone_recent.PhoneRecentCallModel;
import com.example.within.interfaces.CallLogDisplayListener;
import com.example.within.workers.GenericCallAndContactWorker;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecentFragment extends Fragment implements CallLogDisplayListener {
    private final String TAG = getClass().getSimpleName();
    private CallLogFragment callLogFragment;
    private Context appContext;
    private RecyclerView recentRecyclerView, phoneCallLogRecycler;
    private final int READ_CALL_LOG_PERMISSION = 1;
    private List<RecentModel> appRecentCallLogList;
    private  List<PhoneRecentCallModel> phoneCallLogList;// Populate this list with your data
    private RecentAdapter recentAdapter;
    private ProgressBar phoneRecentProgress;
    private PhoneRecentCallLogAdapter phoneCallLogAdapter;
    private RelativeLayout mainLayout;
    private TextView edit;
    private MaterialSwitch recentTypeSwitch;
    private ViewGroup.MarginLayoutParams phoneLayoutParams, layoutParams;

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /* Before creating the view i want to get the users permission to get their phone contacts and this will
         be used in the background thread to populate the phone dataList entries*/
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

    /**Method signature is used to handle the work request to get the users phone call log
     * based on the input data passed to the worker which is used by the GenericWorker to
     * implement tasked based on the object passed.
     *
     * monitors the work and return appropriate list*/
    final void handleContactRetrievalWork (){
        //-- Send input data to the worker
        Data inputData = new Data.Builder()
                .putString("objectType", "PhoneLogModel")
                .build();
        // Create a work request that build a request for the callLogWorker
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(GenericCallAndContactWorker.class)
                .setInputData(inputData)
                .build();
        /* Monitor the work flow which returns the generated list in the CallLogger class which is
        * its static methods are then called to return the list contained for the respective model
        *
        * the Call Logger class is a static class to only be reference statically to return the users
        * call log details*/
        WorkManager workManager = WorkManager.getInstance(appContext);
        workManager.enqueue(workRequest);
        workManager.getWorkInfoByIdLiveData(workRequest.getId()) // -- start monitoring work flow
                .observe(getViewLifecycleOwner(), workInfo -> { // -- get the life-cycle-owner and use the lambda to run the work flow
                    phoneRecentProgress.setVisibility(View.VISIBLE); // Start the progress bar
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED){ //-- check the current work info against the State success
                        //--After work completion, get the returned list of the call log
                        //-- list using the static method of the generic call and contact worker
                        phoneCallLogList = CallLogger.getPhoneRecentModelList();
                        phoneCallLogAdapter = new PhoneRecentCallLogAdapter(phoneCallLogList, this);
                        phoneCallLogAdapter.setCallLogListener(this);
                        phoneCallLogRecycler.setAdapter(phoneCallLogAdapter);
                        phoneCallLogAdapter.notifyDataSetChanged();

                        appRecentCallLogList = CallLogger.getRecentModelList();
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

    /**
     * This is the overridden method from the CallLog listener.
     * the method has an expects two non null arguments of an object type
     * and a list with wildcard bounds...
     *
     * the overridden signature simply triggers the display of the bottomSheet
     * which contains the call log list of the callee contact in focus or
     * in position*/
    @Override
    public void displayCallLog(@NonNull Object model, @NonNull List<?> modelList) {
        if (isAdded()){
            if(callLogFragment != null && callLogFragment.isVisible()){
                callLogFragment.dismiss();
            }else{
                callLogFragment = new CallLogFragment(model, modelList);
                callLogFragment.setAllowEnterTransitionOverlap(true);
                callLogFragment.show(requireActivity().getSupportFragmentManager(), "ModalBottomSheet");
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        recentAdapter = new RecentAdapter(CallLogger.getRecentModelList());
        recentRecyclerView.setAdapter(recentAdapter);
        recentAdapter.notifyDataSetChanged();

        phoneCallLogAdapter = new PhoneRecentCallLogAdapter(phoneCallLogList, this);
        phoneCallLogAdapter.setCallLogListener(this);
        phoneCallLogRecycler.setAdapter(phoneCallLogAdapter);
        phoneCallLogAdapter.notifyDataSetChanged();
    }




    /** This fragment expands the call log list when triggered
     * the constructor of the object is designed to receive an unknown object type and
     * an unknown list type.
     *
     * The list uses a wildcard as params so that when it receives a new list the list type would
     * then be */

    public static class CallLogFragment extends BottomSheetDialogFragment {

        //-- Initialize an empty constructor
        private Object model;
        private List<?> modelList;

        protected Toolbar callLogToolBar;
        protected ListView callLogList;
        private TextView contactName, contactNumber;
        private CallLogAdapter callLogAdapter;


        public CallLogFragment(Object T, List<?> modelList){
            /*Ensure the passed object types is of the required type then initialize the object T*/
            if (T instanceof RecentModel){
                this.model = T;
            }else if (T instanceof PhoneRecentCallModel){
                this.model = T;
            }
            Log.d("Call log fragment", "Object type received " + T);
            this.modelList = modelList;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_call_log_list, container, false);
            initializeViews(view); // Call the method to initialize the views
            handleHeaderInitialization();

            callLogAdapter = new CallLogAdapter(requireContext().getApplicationContext(), modelList);
            callLogList.setAdapter(callLogAdapter);
            return view;
        }

        @Override
        public void onStart() {
            super.onStart();
            Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }

        final void initializeViews(View view){
            contactName = view.findViewById(R.id.call_log_contact_name);
            contactNumber = view.findViewById(R.id.call_log_contact_number);
            callLogList = view.findViewById(R.id.call_log_list);
        }

        /*Helper method used to handle the initialization of the call logs header which contains
        * the name and number of the contact in focus*/
        final void handleHeaderInitialization (){
            /* InstanceOf method is used to check the object type passing the return value of the
            * callee details to the local var*/
            if (model instanceof RecentModel){
                String phone = ((RecentModel) model).getReciepientNumber();
                String name = ((RecentModel) model).getReciepientName();
                /*Null pointer exception might be thrown for numbers with no name
                * so check the null value before initializing the callee details*/
                if (name != null) {
                    requireActivity().runOnUiThread(
                            () -> {
                                contactName.setText(name);
                                contactNumber.setText(phone);
                            }
                    );
                }else {
                    requireActivity().runOnUiThread(
                            () -> {contactNumber.setText(phone);});
                }

                /*Do the same as the above code if the model is of phone contact model*/
            }else if (model instanceof PhoneRecentCallModel){
                requireActivity().runOnUiThread(
                        ()->{
                            contactName.setText(((PhoneRecentCallModel) model).getReciepientName());
                            contactNumber.setText(((PhoneRecentCallModel) model).getReciepientNumber());
                        }
                );
            }


        }



    }


}

