package com.example.okmatka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.okmatka.Activities.Activity_Messages;
import com.example.okmatka.Interfaces.RateDialogCallBack;
import com.example.okmatka.MyFireBase;
import com.example.okmatka.MySignal;
import com.example.okmatka.R;
import com.example.okmatka.RateDialog;
import com.example.okmatka.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private DatabaseReference usersRef;
    private Context context;
    private ArrayList<User> userList;
    private String currentHolderRate = "";
    private User currentUser;

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
        loadUserFromMatchList(holder, currentUser);

        //setting holder listeners
        holder.userItem_BTN_chat.setOnClickListener(userItemListener(holder, currentUser));
        holder.userItem_BTN_rate.setOnClickListener(userItemListener(holder, currentUser));

    }

    private View.OnClickListener userItemListener(@NonNull final ViewHolder holder, final User user) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().equals("rate")) {
                    currentUser = user;
                    openRateDialog();
                }
                else
                    moveToChatActivity(user);
            }
        };
    }

    private void openRateDialog() {
        RateDialog rateDialog = new RateDialog(rateDialogCallBack);
        rateDialog.show(((AppCompatActivity) context).getSupportFragmentManager(),"Rate");
    }

    private RateDialogCallBack rateDialogCallBack = new RateDialogCallBack() {
        @Override
        public void getRate(String rate) {
            currentHolderRate = rate;
            setUserRate(currentHolderRate,currentUser);
        }
    };

    private void setUserRate(String userRate, User user) {
        if ((!userRate.equals("")) && checkRate(userRate)) {
            int reviewsNumber = user.getNumberOfReviews() + 1;
            double newTotalRate = Double.parseDouble(userRate) + user.getRate();

            //updating the new rate
            usersRef.child(user.getId())
                    .child(MyFireBase.KEYS.RATE)
                    .setValue(newTotalRate).
                    addOnCompleteListener(rateUpdateCompleteListener(user,reviewsNumber));

            MySignal.getInstance().showToast("Thanks!");
        }
    }

    private OnCompleteListener<Void> rateUpdateCompleteListener(final User user, final double reviews) {
        return new OnCompleteListener<Void>() {
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        //updating the new number of reviews
            usersRef.child(user.getId())
                .child(MyFireBase.KEYS.NUMBER_OF_REVIEWS)
                .setValue(reviews).addOnCompleteListener(reviewsUpdateCompleterListener(user));
            }
        };
    }

    private OnCompleteListener<Void> reviewsUpdateCompleterListener(final User user) {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                usersRef.child(user.getId()).addListenerForSingleValueEvent(notifyUserMatchesListener());
            }
        };
    }

    private ValueEventListener notifyUserMatchesListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User updatedUser = snapshot.getValue(User.class);
                assert updatedUser != null;

                //notifying the user's matches on the new rate
                for(DataSnapshot userMatch : snapshot.child(MyFireBase.KEYS.USER_MATCHES_LIST).getChildren()) {
                    User myMatchUser = userMatch.getValue(User.class);
                    assert myMatchUser != null;
                    updateMatches(myMatchUser,updatedUser);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
    }

    private void updateMatches(User myMatchUser, User mySelf) {
        DatabaseReference myMatchRef = MyFireBase.getInstance()
                .getReference(MyFireBase.KEYS.USERS_LIST).child(myMatchUser.getId());
        myMatchRef.child(MyFireBase.KEYS.USER_MATCHES_LIST).child(mySelf.getId()).setValue(mySelf);
    }

    private boolean checkRate(String rateResult) {
        if(android.text.TextUtils.isDigitsOnly(rateResult)){
            return isRateInRange(rateResult);
        }
        MySignal.getInstance().showToast("Rate must be a number");
        return false;
    }

    private boolean isRateInRange(String rateResult) {
        double numericResult = Double.parseDouble(rateResult);
        if (numericResult <= 10 && numericResult >= 0)
            return true;
        else
            MySignal.getInstance().showToast("Rate must be 0-10");
        return false;
    }

    private void moveToChatActivity(User currentUser) {
        Intent intent = new Intent(context, Activity_Messages.class);
        intent.putExtra(Activity_Messages.USER_ID,currentUser.getId());
        context.startActivity(intent);
    }

    private void loadUserFromMatchList(@NonNull ViewHolder holder, User currentUser) {
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

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView userItem_IMG_userPic;
        private TextView userItem_LBL_name;
        private MaterialButton userItem_BTN_chat , userItem_BTN_rate;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userItem_BTN_rate = itemView.findViewById(R.id.userItem_BTN_rate);
            userItem_BTN_chat = itemView.findViewById(R.id.userItem_BTN_chat);
            userItem_IMG_userPic = itemView.findViewById(R.id.userItem_IMG_userPic);
            userItem_LBL_name = itemView.findViewById(R.id.userItem_LBL_name);
        }
    }


}
