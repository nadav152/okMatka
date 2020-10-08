package com.example.okmatka.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.okmatka.Activity_Messages;
import com.example.okmatka.R;
import com.example.okmatka.User;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private ArrayList<User> userList;

    public UserAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.user_item,parent,false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final User currentUser = userList.get(position);

        //textView
        holder.userItem_LBL_name.setText(currentUser.getName());

        //ImageView
        if (currentUser.getImageURL().equalsIgnoreCase("default"))
            holder.item_IMG_userPic.setImageResource((R.mipmap.ic_launcher));
        else
            Glide.with(context)
                .load(currentUser.getImageURL())
                .into(holder.item_IMG_userPic);

        //setting holder listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Activity_Messages.class);
                intent.putExtra(Activity_Messages.USER_ID,currentUser.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView item_IMG_userPic;
        private TextView userItem_LBL_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item_IMG_userPic = itemView.findViewById(R.id.item_IMG_userPic);
            userItem_LBL_name = itemView.findViewById(R.id.userItem_LBL_name);
        }
    }


}