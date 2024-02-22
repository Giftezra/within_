package com.example.within.contacts;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.within.R;
import com.example.within.calls.CallTaskActivity;
import com.example.within.helpers.Constants;
import com.example.within.profile.AddCreditActivity;
import com.example.within.profile.settings.InviteActivity;

import java.util.List;

/**
 * The class handles the users interaction with the contact list and display
 * it contains the method implementations fired when the user clicks any contact in the
 * contactModel.
 * extends the arrayAdapter class to implement the contactModel object which is the way
 * the contact is modeled to look like to the user
 * */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private final List<ContactModel> contactModels;
    private List<ContactModel> originalContactList;
    private List<ContactModel> filteredContactList;
    private static Context appContext;

    TextView contactImage, contactName, contactPhone;

    public ContactAdapter( List<ContactModel> contactModels){
        this.contactModels = contactModels;
    }


    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_contact_item, parent, false);
        appContext = view.getContext();
        return new ContactViewHolder(view);
    }

    /*
    * Overridden method used to bind the views to its holder */
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ContactModel model = contactModels.get(position);
        holder.itemView.setTag(model);
        holder.bind(model); //--Bind the holder to the model
        //-- Bind the holder to the buttons on the contact items which will
        //-- implement methods to call the contact, send them airtime, or send them credit
        //-- Todo update another button that enables users to send a direct text message to the user
        holder.callButton.setOnClickListener(holder.onClickListener(model));
        holder.inviteOthers.setOnClickListener(holder.onClickListener(model));
        holder.sendCredit.setOnClickListener(holder.onClickListener(model));

        // Check if this is the first item or if the initial letter has changed
        if (position == 0 || !model.getInitialLetter().equals(contactModels.get(position - 1).getInitialLetter())) {
            // Display a separator line or some visual indication (e.g., a header)
            holder.seperator.setVisibility(View.VISIBLE);
            holder.seperator.setText(model.getInitialLetter()); // Display the initial letter as a header
        } else {
            // Hide the separator if it's the same initial letter as the previous item
            holder.seperator.setVisibility(View.GONE);
        }

        holder.contactDetails.setOnClickListener(v -> {
            toggleExpansion(position);
        });
        holder.contactDetailLayout.setVisibility(model.isExpanded() ? View.VISIBLE : View.GONE);
    }
    /* This is used to keep count of the items on the contactModel so
    * the adapter would not run into an error*/
    @Override
    public int getItemCount() {
        if (contactModels != null) {
            return contactModels.size();
        }else{
            return 0;
        }
    }

    /**
     * This method is used to toggle the display that holds the image buttons
     * used to send airtime, call the contact and send them an invite.
     * @param position is used to target the particular position of the item
     * in the recycler adapter making its hidden layout display*/
    private void toggleExpansion(int position) {
        ContactModel item = contactModels.get(position);
        item.setExpanded(!item.isExpanded()); // Toggle expanded state
        // Hide other expanded items
        for (int i = 0; i < contactModels.size(); i++) {
            if (i != position) {
                contactModels.get(i).setExpanded(false);
            }
        }
        notifyDataSetChanged(); // Notify adapter about the data change
    }

    /*
    * The contactViewHolder holds the views in the adapter */
    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        private final TextView fullName, phoneNumber, intialText, seperator;
        private final ImageButton callButton, sendCredit, inviteOthers;
        private final LinearLayout contactDetailLayout, contactDetails;


        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            fullName = itemView.findViewById(R.id.contact_name);
            phoneNumber = itemView.findViewById(R.id.contact_phone);
            intialText = itemView.findViewById(R.id.letter_image);
            callButton = itemView.findViewById(R.id.call_contact);
            sendCredit = itemView.findViewById(R.id.send_credit);
            inviteOthers = itemView.findViewById(R.id.invite_others);
            contactDetailLayout = itemView.findViewById(R.id.contact_controls_layout);
            contactDetails = itemView.findViewById(R.id.contact_detail);
            seperator = itemView.findViewById(R.id.seperator);
        }

        /**
         * This listener method is used to open a new activity based on the button clicked
         * by the user.
         * @param  model is used to get the tag of the current item clicked or focused
         * on by the user. the contact name and number of the item is retrieved
         * from the model and passed into the created intent before starting the
         * activity
         * @return view with an intent */
        final View.OnClickListener onClickListener(ContactModel model) {
            String contactNumber = model.getPhoneNumber(); //--Initialize two null strings to hold the name and number of the user
            String name = model.getName();

            Log.d("Contact Adapter", "Model: " + contactNumber + name);
            //-- Returns a view that opens an intent to the provided activity passing the
            //-- action and extras to the intent
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contactNumber != null && name != null) {//-- check for null values in the contact details
                        Intent callIntent;

                        if (v.getId() == R.id.call_contact) {//-- Check the view id clicked
                            callIntent = new Intent(appContext, CallTaskActivity.class); //-- create a new intent
                            callIntent.setAction(Constants.ACTION_OUTGOING_CALL); //-- Set the action
                            callIntent.putExtra(Constants.OUTGOING_CALL_RECIPIENT, contactNumber); //--Put the contact number
                            callIntent.putExtra(Constants.RECIPIENT_NAME, name);
                            appContext.startActivity(callIntent); //-- Start activity
                            //-- code functionalities are the same for the above code and the lines below
                            //-- so there is no need for a comment here as only the receiving class changes
                        } else if (v.getId() == R.id.send_credit) {
                            callIntent = new Intent(appContext, AddCreditActivity.class);
                            callIntent.setAction(Constants.ACTION_OUTGOING_CALL);
                            callIntent.putExtra(Constants.OUTGOING_CALL_RECIPIENT, contactNumber);
                            callIntent.putExtra(Constants.RECIPIENT_NAME, name);
                            appContext.startActivity(callIntent);

                        } else if (v.getId() == R.id.invite_others) {
                            callIntent = new Intent(appContext, InviteActivity.class);
                            callIntent.setAction(Constants.ACTION_OUTGOING_CALL);
                            callIntent.putExtra(Constants.OUTGOING_CALL_RECIPIENT, contactNumber);
                            callIntent.putExtra(Constants.RECIPIENT_NAME, name);
                            appContext.startActivity(callIntent);
                        }
                    }
                }
            };
        }

        private void bind (ContactModel model){
            fullName.setText(model.getName());
            phoneNumber.setText(model.getPhoneNumber());
            intialText.setText(model.getInitialLetter());

        }


    }
}
