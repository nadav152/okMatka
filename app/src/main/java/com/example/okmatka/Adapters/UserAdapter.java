package com.example.okmatka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.okmatka.Activities.Activity_Messages;
import com.example.okmatka.MyFireBase;
import com.example.okmatka.MySignal;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private DatabaseReference usersRef;
    private Context context;
    private ArrayList<User> userList;

    public UserAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
        usersRef = MyFireBase.getInstance()
                .getReference(MyFireBase.KEYS.USERS_LIST);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.user_item,parent,false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final User currentUser = userList.get(position);
        loadUser(holder, currentUser);


        //setting holder listeners
        holder.userItem_BTN_chat.setOnClickListener(userItemListener(holder, currentUser));
        holder.userItem_BTN_sendButton.setOnClickListener(userItemListener(holder, currentUser));

    }

    private View.OnClickListener userItemListener(@NonNull final ViewHolder holder, final User currentUser) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().equals("imgButton"))
                    setUserRate(holder, currentUser);
                else
                    moveToChatActivity(currentUser);
            }
        };
    }

    private void setUserRate(@NonNull ViewHolder holder, User currentUser) {
        String userRate = String.valueOf(holder.userItem_EDT_rate.getText());
        if ((!userRate.equals("")) && checkRate(userRate)) {
            int reviewsNumber = getNumOfUserReviews(currentUser);
            //setting rate
            usersRef.child(currentUser.getId())
                    .child(MyFireBase.KEYS.RATE)
                    .setValue(Double.parseDouble(userRate) / reviewsNumber);
            //setting number of reviews
            usersRef.child(currentUser.getId())
                    .child(MyFireBase.KEYS.NUMBER_OF_REVIEWS).setValue(reviewsNumber);

            MySignal.getInstance().showToast("Thanks!");
        }
        holder.userItem_EDT_rate.setText("");
    }

    private boolean checkRate(String rateResult) {
        if(android.text.TextUtils.isDigitsOnly(rateResult)){
            double numericResult = Double.parseDouble(rateResult);
            if (numericResult <= 10 && numericResult >= 0)
                return true;
            else
                MySignal.getInstance().showToast("Rate must be 0-10");
            return false;
        }
        MySignal.getInstance().showToast("Rate must be a number");
        return false;
    }

    private int getNumOfUserReviews(User currentUser) {
        currentUser.setNumberOfReviews(currentUser.getNumberOfReviews()+1);
        return currentUser.getNumberOfReviews();
    }

    private void moveToChatActivity(User currentUser) {
        Intent intent = new Intent(context, Activity_Messages.class);
        intent.putExtra(Activity_Messages.USER_ID,currentUser.getId());
        context.startActivity(intent);
    }

    private void loadUser(@NonNull ViewHolder holder, User currentUser) {
        //textView
        holder.userItem_LBL_name.setText(currentUser.getName());

        //ImageView
        if (currentUser.getImageURL().equalsIgnoreCase("default"))
            Glide.with(context)
                    .load(R.drawable.general_user)
                    .into(holder.userItem_IMG_userPic);
        else
            Glide.with(context)
                .load(currentUser.getImageURL())
                .into(holder.userItem_IMG_userPic);

        //status check
        if (currentUser.getStatus().equals("online"))
            holder.userItem_LBL_online.setBackgroundColor(Color.GREEN);
        else
            holder.userItem_LBL_online.setBackgroundColor(Color.RED);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView userItem_IMG_userPic;
        private TextView userItem_LBL_name,userItem_LBL_online;
        private MaterialButton userItem_BTN_chat;
        private ImageButton userItem_BTN_sendButton;
        private EditText userItem_EDT_rate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userItem_EDT_rate = itemView.findViewById(R.id.userItem_EDT_rate);
            userItem_BTN_chat = itemView.findViewById(R.id.userItem_BTN_chat);
            userItem_BTN_sendButton = itemView.findViewById(R.id.userItem_BTN_sendButton);
            userItem_IMG_userPic = itemView.findViewById(R.id.userItem_IMG_userPic);
            userItem_LBL_name = itemView.findViewById(R.id.userItem_LBL_name);
            userItem_LBL_online = itemView.findViewById(R.id.userItem_LBL_online);
        }
    }


}
