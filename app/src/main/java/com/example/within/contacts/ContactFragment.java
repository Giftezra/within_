package com.example.within.contacts;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
import com.example.within.workers.GenericCallAndContactWorker;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {
    private static final int READ_CONTACTS_PERMISSION_REQUEST = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 2;
    private final String TAG = "ContactFragment";
    private Context appContext;
    private ContactAdapter contactAdapter;
    private RecyclerView contactRecycler;
    private List<ContactModel> contactModels, filteredContacts;


    SearchView searchBar;
    ProgressBar progressBar;



    /* Method checks for asks the user for permission upon opening the activity*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case READ_CONTACTS_PERMISSION_REQUEST:
                // Check if READ_CONTACTS permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // READ_CONTACTS permission denied, show a message or handle it accordingly
                    Toast.makeText(getContext(), "READ_CONTACTS Permission denied. Cannot access contacts.", Toast.LENGTH_SHORT).show();
                    requestPermissions();
                }
                break;
            case MY_PERMISSIONS_REQUEST_SEND_SMS:
                // Check if SEND_SMS permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // SEND_SMS permission granted, initialize contacts
                } else {
                    // SEND_SMS permission denied, show a message or handle it accordingly
                    Toast.makeText(getContext(), "SEND_SMS Permission denied. Cannot send SMS.", Toast.LENGTH_SHORT).show();
                    // You may want to disable functionality that requires SMS here
                }
                break;
        }
    }

    /* Requests the user contact permission to read the phone contact list */
    private void requestPermissions() {
        // Request both READ_CONTACTS and SEND_SMS permissions
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.SEND_SMS
                },
                READ_CONTACTS_PERMISSION_REQUEST
        );
    }

    /*
    * Helper method used yto show the progress bar while the contact is loading in the background
    * */
    private void showProgressBar(){
        requireActivity().runOnUiThread(()->progressBar.setVisibility(View.VISIBLE));
    }

    /**
    * This listener is basically for the search view to perform its search operations
    * on the users contact list. the method has two overridden method where one listens
    * to text submission to process its request while the other checks the query in real-time
    * while the user searches
    * */
    final SearchView.OnQueryTextListener onQueryTextListener(){
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            // --Call the filter method her to fire its algorithm
            @Override
            public boolean onQueryTextChange(String newText) {
                filterContacts(newText);
                return true;
            }
        };
    }

    /**
    * Helper method is used to filter the contactModels list in search of contact matching
    * the users entered query.
    *  */
    private void filterContacts(String query){
        filteredContacts.clear(); //--clear the main list
        for (ContactModel contacts:contactModels) { //--Loop the contactModel to get single contacts
            //-- Checks if query is contained in the contact list using the contact name
            if (contacts.getName().toLowerCase().contains(query.toLowerCase())){
                filteredContacts.add(contacts); //-- add the retrieved contacts
                contactAdapter = new ContactAdapter(filteredContacts);
                contactRecycler.setAdapter(contactAdapter);
                contactAdapter.notifyDataSetChanged(); // --Notify the adapter of the changes
            }
        }
    }

    /*
    * Method is called to check the permission request for the users contacts and sms
    * when no granted it returns a boolean false and true when already granted
    * */
    private boolean checkPermission() {
        // Check if both READ_CONTACTS and SEND_SMS permissions are granted
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container,false);
        contactModels = new ArrayList<>();
        filteredContacts = new ArrayList<>();
        appContext = requireContext().getApplicationContext();


        // Get the views from the UI using the ID
        progressBar = view.findViewById(R.id.progress_bar);
        searchBar = view.findViewById(R.id.search_bar);
        contactRecycler = view.findViewById(R.id.contact_recycler);

        contactRecycler.setLayoutManager( new LinearLayoutManager(getContext()));
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) contactRecycler.getLayoutParams();
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.recycler_margin);
        contactRecycler.setLayoutParams(layoutParams);

        //-- this code will check for the users permission before creating a work request
        if (checkPermission()) {
            Data input = new Data.Builder()
                    .putString("objectType", "ContactModel")
                    .build();

            //--create a onetime work request to start the worker
            OneTimeWorkRequest contactWorkRequest = new OneTimeWorkRequest.Builder(GenericCallAndContactWorker.class)
                    .setInputData(input)
                    .build();

            WorkManager.getInstance(appContext).enqueue(contactWorkRequest); //--Start the work
            //--Here i will monitor the workmanager cycle to check the state for a success before
            //--calling the generic classes static methods to get the list of the contact model
            //--in the enqued class
            WorkManager.getInstance(appContext).getWorkInfoByIdLiveData(contactWorkRequest.getId())
                    .observe(getViewLifecycleOwner(), workInfo -> {
                        showProgressBar(); //-- show the progress the bar
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED){
                            //--Check for the success state before getting th static method to return the
                            //-- users generated call log
                            contactModels = CallLogger.getContactModelList();
                            contactAdapter = new ContactAdapter(contactModels);
                            contactRecycler.setAdapter(contactAdapter);
                            contactAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    });

        } else {
            requestPermissions();
        }
        searchBar.setOnQueryTextListener(onQueryTextListener());
        return view;
    }

    /* Set the behaviour for when the fragment has been reattached to fetch
    * the contacts again */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (appContext == null){
            appContext = context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        contactAdapter = new ContactAdapter(contactModels);
        contactRecycler.setAdapter(contactAdapter);
        contactAdapter.notifyDataSetChanged();
    }

}
