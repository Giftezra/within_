package com.example.within.calllog_activities.phone_recent;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.within.R;
import com.example.within.calls.CallTaskActivity;
import com.example.within.helpers.Constants;
import com.example.within.initial_pages.MainActivity;
import com.example.within.profile.AddCreditActivity;

import java.util.List;
import java.util.Objects;

public class PhoneCallLogAdapter extends RecyclerView.Adapter<PhoneCallLogAdapter.RecentViewHolder>{
    private final String TAG = PhoneCallLogAdapter.class.getSimpleName();
    private final List<PhoneRecentModel> phoneRecentModels;
    private final boolean isEditMode = false;
    private Handler handler;
    private Context appContext;
    private Toolbar callLogToolBar;
    private ListView callLogList;
    private static final long LONG_PRESSED = 500; // Set for 0.5 seconds

    public PhoneCallLogAdapter(List<PhoneRecentModel> dataList) {
        this.phoneRecentModels = dataList;
    }


    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_phone_call_log, parent, false);
        handler = new Handler();
        appContext = view.getContext();
        return new RecentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        PhoneRecentModel model = phoneRecentModels.get(position);
        // Delete the contact at the current position

        holder.itemView.setTag(model);
        holder.bind(model);

        // This listener listens for when the user holds down the contact
        // they want to interact with. it is fired when held down for 2 seconds
        holder.recipientDetailLayout.setOnLongClickListener(v -> {
            handler.postDelayed(() -> {
                holder.openMenu(v, position);
            }, LONG_PRESSED);
            return true;
        });
        holder.callAgain.setOnClickListener(holder.onClickListener(model, phoneRecentModels));
        holder.sendCredit.setOnClickListener(holder.onClickListener(model, phoneRecentModels));
        holder.callLog.setOnClickListener(holder.onClickListener(model, phoneRecentModels));


        holder.setCallTypeColors(position);

        holder.recipientDetailLayout.setOnClickListener(v -> {
            toggleExpansion(position);
        });
        holder.expandedContainer.setVisibility(model.isExpanded() ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return phoneRecentModels.size();
    }


    /*
     * Handle the toggle for the hidden layout of the recent adapter which contains some view
     * relavant to the user*/
    private void toggleExpansion(int position) {
        PhoneRecentModel item = phoneRecentModels.get(position);
        item.setExpanded(!item.isExpanded()); // Toggle expanded state

        // Hide other expanded items
        for (int i = 0; i < phoneRecentModels.size(); i++) {
            if (i != position) {
                phoneRecentModels.get(i).setExpanded(false);
            }
        }notifyDataSetChanged(); // Notify adapter about the data change
    }

    public class RecentViewHolder extends RecyclerView.ViewHolder {
        TextView recipientsFirstName, recipientsNumber, callDuration, callType, callDate, seperator;
        LinearLayout expandedContainer, recipientDetailLayout;
        ImageButton sendCredit, callAgain, callLog;
        ImageView initialLetter;



        public RecentViewHolder(@NonNull View itemView) {
            super(itemView);

            recipientsFirstName = itemView.findViewById(R.id.reciepient_name);
            recipientsNumber = itemView.findViewById(R.id.reciepient_phone);
            callDuration = itemView.findViewById(R.id.call_durations);
            callDate = itemView.findViewById(R.id.call_date);
            expandedContainer = itemView.findViewById(R.id.expanded_container);
            sendCredit = itemView.findViewById(R.id.credit_reciepient_button);
            callAgain = itemView.findViewById(R.id.call_again);
            callLog = itemView.findViewById(R.id.call_log_button);
            initialLetter = itemView.findViewById(R.id.initial_letter);
            recipientDetailLayout = itemView.findViewById(R.id.reciepient_details_layout);
            callType = itemView.findViewById(R.id.call_type);
            seperator = itemView.findViewById(R.id.call_days_seperator);
        }


        final View.OnClickListener onClickListener(PhoneRecentModel model, List<PhoneRecentModel> list){
            //Create a new instance of  the phone recent model to
            // get its child objects into the method for use which includes
            // the callee name and number
            String recipientNumber = model.getReciepientNumber(); //-- get the recipien number
            String recipientName = model.getReciepientName(); //--get the recipient name
            return v -> {

                if (recipientName != null && recipientNumber != null) {
                    Intent callIntent; //-- Create a new intent with no initialization

                    if (v.getId() == R.id.call_again) {
                        /* Create a new intent passing the callTaskActivity.class,
                        * sets the new intent action to ACTION_OUTGOING CALL so the activity knows
                        * what method to call then the intent is received*/
                        callIntent = new Intent(appContext, CallTaskActivity.class);
                        callIntent.setAction(Constants.ACTION_OUTGOING_CALL);
                        //-- Pass the callee details and start the activity
                        callIntent.putExtra(Constants.OUTGOING_CALL_RECIPIENT, recipientNumber);
                        callIntent.putExtra(Constants.RECIPIENT_NAME, recipientName);
                        appContext.startActivity(callIntent);

                    } else if (v.getId() == R.id.credit_reciepient_button) {
                        callIntent = new Intent(appContext, AddCreditActivity.class);
                        callIntent.setAction(Constants.SEND_CREDIT);
                        callIntent.putExtra(Constants.RECIPIENT_NAME, recipientName);
                        callIntent.putExtra(Constants.OUTGOING_CALL_RECIPIENT, recipientNumber);
                        appContext.startActivity(callIntent);

                    } else if (v.getId() == R.id.call_log_button) {
                        handleCallLogListDialog(model, phoneRecentModels);
                    }
                }
            };
        }


        /*
         * Helper method which is used to display the call log bottom sheet when clicked.
         * the sheet contains more details about the calls made from that phone number
         * during a period of time*/
        final void handleCallLogListDialog(PhoneRecentModel model, List<PhoneRecentModel> list) {
            Dialog callLogDialog = new Dialog(appContext);
            PhoneRecentCallLogAdapter callLogAdapter = new PhoneRecentCallLogAdapter(appContext, list);


            callLogDialog.setContentView(R.layout.fragment_call_log_list);
            callLogToolBar = callLogDialog.findViewById(R.id.call_log_toolbar);
            callLogList = callLogDialog.findViewById(R.id.call_log_list);

            String number = null;
            String name = null;

            if (model != null) {
                name = model.getReciepientName();
                number = model.getReciepientNumber();
            }

            if (callLogList != null) {
                callLogToolBar.setTitle(name);
                callLogToolBar.setSubtitle(number);
                callLogList.setAdapter(callLogAdapter);
            }

            Objects.requireNonNull(callLogDialog.getWindow()).setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            callLogAdapter.setNotifyOnChange(true);
            callLogDialog.show();
        }



        /**
         * Listener method used to listen to click on the popped up item menus
         * when the user clicks a view, the method tracks the id of the view and performs
         * the task.
         * @param position  is used to map the position of the view to the datalist to avoid
         * code corruption*/
        final MenuItem.OnMenuItemClickListener menuItemClickListener(int position, PhoneRecentModel model, View view){
            Context context = view.getContext(); // Get the current context
            return new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {

                    if (item.getItemId() == R.id.delete_number){
                        deleteRecent(position);
                        // The user has asked to edit the number in the dial-pad
                        // before placing the call
                        // create a new intent
                    } else if (item.getItemId() == R.id.edit_number) {
                        Intent editIntent = new Intent(context, MainActivity.class);
                        editIntent.putExtra("phone_number", model.getReciepientNumber());
                        editIntent.setAction(Constants.EDIT_BEFORE_CALL); //Set a flag for the receiving intent
                        context.startActivity(editIntent); // Start the intent activity or fragment
                    }
                    return false;
                }
            };
        }

        // Method will open a menu when fired. the menu contains items for user to delete
        // a perform some codes on a phone number in the list
        private void openMenu(View view, int position) {
            if (view != null) {
                Context context = view.getContext();

                PhoneRecentModel item = phoneRecentModels.get(position);
                PopupMenu menu = new PopupMenu(context, recipientDetailLayout);
                menu.getMenuInflater().inflate(R.menu.onhold_calllog_menu, menu.getMenu());

                // Get the item for the number in focus and checks to ensure it is valid
                // before attaching the recipient in focus phone number to it as the title
                MenuItem numberInFocus = menu.getMenu().findItem(R.id.number_in_focus);
                if (numberInFocus != null) {
                    SubMenu subMenu = numberInFocus.getSubMenu(); // Get the submenu
                    if (subMenu != null) {
                        MenuItem copyNumber = subMenu.findItem(R.id.copy_number);
                        MenuItem editNumber = subMenu.findItem(R.id.edit_number);
                        MenuItem deleteNumber = subMenu.findItem(R.id.delete_number);
                        MenuItem blockNumber = subMenu.findItem(R.id.block_number);

                        // Set the title of the menu to the phone number in focus
                        numberInFocus.setTitle(item.getReciepientNumber());

                        deleteNumber.setOnMenuItemClickListener(menuItemClickListener(position, item, view));
                        editNumber.setOnMenuItemClickListener(menuItemClickListener(position,item,view));
                    }
                }

                menu.show();
            }
        }

        /**
         * This method sets the call type colors based on the type of call returned from
         * the call log which has been passed from the parent fragments
         * @param position  is used to map the position of the data in the list
         * to avoid manipulating random data*/
        private void setCallTypeColors(int position){
            PhoneRecentModel model = phoneRecentModels.get(position);
            String callTypes = model.getCallType();
            if (callTypes != null){
                switch (callTypes){
                    case "missed":
                    case "busy":
                        callType.setTextColor(Color.RED);
                        break;
                    case "incoming":
                        callType.setTextColor(Color.GREEN);
                        break;
                    case "out going":
                        callType.setTextColor(Color.BLUE);
                        break;
                    case "declined":
                        callType.setTextColor(Color.YELLOW);
                        break;
                }
            }
        }

        /*
         * Handles the ability for users to delete a contact from their recent call list.
         * gets the model at the given position and use a loop to iterate through the dataList
         * calling the remove method on the list to remove the item.
         */
        private void deleteRecent(int position) {
            PhoneRecentModel item = phoneRecentModels.get(position);
            // Hide other expanded items
            for (int i = 0; i < phoneRecentModels.size(); i++) {
                if (i != position) {
                    phoneRecentModels.remove(item);
                }
            }
            notifyDataSetChanged();
        }




        public void bind (PhoneRecentModel model){
            recipientsFirstName.setText(model.getReciepientName());
            recipientsNumber.setText(model.getReciepientNumber());
            callDuration.setText(model.getCallDuration());
            initialLetter.setImageResource(model.getIntialLetter());
            callType.setText(model.getCallType());
            callDuration.setText(model.getCallDuration());
            callDate.setText(model.getRecentDate());

        }
    }
}
