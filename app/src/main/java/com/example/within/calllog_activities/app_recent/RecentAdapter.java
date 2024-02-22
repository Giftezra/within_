package com.example.within.calllog_activities.app_recent;

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
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.within.R;
import com.example.within.calls.CallTaskActivity;
import com.example.within.helpers.Constants;
import com.example.within.profile.AddCreditActivity;

import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.RecentViewHolder>{
    private final List<RecentModel> dataList;
    private final boolean isEditMode = false;
    private Handler handler;
    private Context appContext;
    private final long LONG_PRESSED = 500;

    public RecentAdapter(List<RecentModel> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_recent_call, parent, false);
        handler = new Handler();
        appContext = view.getContext(); //-- Get the current app context
        return new RecentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        RecentModel item = dataList.get(position);
        // Delete the contact at the current position

        holder.itemView.setTag(item);
        holder.bind(item);

        // This listener listens for when the user holds down the contact
        // they want to interact with. it is fired when held down for 2 seconds
        holder.recipientDetailLayout.setOnLongClickListener(v -> {
            handler.postDelayed(() -> {
                holder.openMenu(v, position);
            }, LONG_PRESSED);
            return true;
        });

        holder.setCallType(position);
        holder.callAgain.setOnClickListener(holder.onClickListener(item));
        holder.sendCredit.setOnClickListener(holder.onClickListener(item));
        holder.callLog.setOnClickListener(holder.onClickListener(item));

        holder.recipientDetailLayout.setOnClickListener(v -> {
            toggleExpansion(position);
        });

        holder.expandedContainer.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        if (dataList != null) return dataList.size();
        else return 0;
    }


    /*
    * Handle the toggle for the hidden layout of the recent adapter which contains some view
    * relavant to the user*/
    private void toggleExpansion(int position) {
        RecentModel item = dataList.get(position);
        item.setExpanded(!item.isExpanded()); // Toggle expanded state

        // Hide other expanded items
        for (int i = 0; i < dataList.size(); i++) {
            if (i != position) {
                dataList.get(i).setExpanded(false);
            }
        }notifyDataSetChanged(); // Notify adapter about the data change
    }



//
//    public void setEditMode(boolean editMode) {
//        isEditMode = editMode;
//        notifyDataSetChanged();
//    }
//
//    public boolean isEditMode() {
//        return isEditMode;
//    }





    public class RecentViewHolder extends RecyclerView.ViewHolder {
        TextView recipientsFirstName, recipientsNumber, callDuration, callType;
        LinearLayout expandedContainer, recipientDetailLayout;
        ImageButton sendCredit, callAgain, callLog;
        ImageView initialLetter;


        public RecentViewHolder(@NonNull View itemView) {
            super(itemView);
            recipientsFirstName = itemView.findViewById(R.id.reciepient_name);
            recipientsNumber = itemView.findViewById(R.id.reciepient_phone);
            callDuration = itemView.findViewById(R.id.reciepient_duration);
            expandedContainer = itemView.findViewById(R.id.expanded_container);
            sendCredit = itemView.findViewById(R.id.credit_reciepient_button);
            callAgain = itemView.findViewById(R.id.call_again);
            callLog = itemView.findViewById(R.id.call_log_button);
            initialLetter = itemView.findViewById(R.id.initial_letter);
            recipientDetailLayout = itemView.findViewById(R.id.reciepient_details_layout);
            callType = itemView.findViewById(R.id.call_type);

        }
        
        /* 
        * Listens to the menu items for clicks. this is used to monitor the users menu selection
        * when triggered to performs the required task.
        * it requires the current position of the item the user has selected in the in the list
        * and performs operations on them ensuring other data arent disturbed
        * */
        final MenuItem.OnMenuItemClickListener menuItemClickListener(int position){
            return new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {

                    if (item.getItemId() == R.id.delete_number){
                        deleteRecent(position);
                    } else if (item.getItemId() == R.id.edit_number) {
                        
                    }
                    return false;
                }
            };
        }


        final View.OnClickListener onClickListener(RecentModel model){
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
                        // TODO TAKE THE USER TO THE CALL LOG LIST OF THAT PARTICULAR CLIENT
                        // TODO DISPLAY ALL CALLS FROM THAT CALLEE IN THE LIST
                    }
                }
            };
        }


        // Method will open a menu when fired. the menu contains items for user to delete
        // a perform some codes on a phone number in the list
        private void openMenu(View view, int position) {
            if (view != null) {
                RecentModel item = dataList.get(position);
                PopupMenu menu = new PopupMenu(view.getContext(), recipientDetailLayout);
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

                        deleteNumber.setOnMenuItemClickListener(menuItemClickListener(position));
                    }
                }

                menu.show();
            }
        }
        /*
        * This helper methos sets the color of the call type bases on the call type
        * it checks to ensure the model has the calltype before using a switch to check
        * different cases for when the call is either missed, received or dialed.
        **/
        // Todo in later code i will also implement wen the call was denied by the user as denied and when the receiver denies the call as busy
        private void setCallType(int position){
            RecentModel model = dataList.get(position);
            String callTypes = model.getCallType();
            if (callTypes != null){
                switch (callTypes){
                    case "missed":
                        callType.setTextColor(Color.RED);
                        break;
                    case "received":
                        callType.setTextColor(Color.GREEN);
                        break;
                    case "dialed":
                        callType.setTextColor(Color.BLUE);
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
            RecentModel item = dataList.get(position);
            // Hide other expanded items
            for (int i = 0; i < dataList.size(); i++) {
                if (i != position) {
                    dataList.remove(item);
                }
            }
            notifyDataSetChanged();
        }

        public void bind (RecentModel model){
            recipientsFirstName.setText(model.getReciepientName());
            recipientsNumber.setText(model.getReciepientNumber());
            callDuration.setText(model.getCallDuration());
            initialLetter.setImageResource(model.getIntialLetter());
            callType.setText(model.getCallType());

        }
    }
}
