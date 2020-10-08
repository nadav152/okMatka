package com.example.okmatka.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.okmatka.ChatMessage;
import com.example.okmatka.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ChatMessage> chatMessageList;
    public static final int LETF_MSG = 0;
    public static final int RIGHT_MSG = 1;
    private FirebaseUser firebaseUser;

    public MessageAdapter(Context context, ArrayList<ChatMessage> chatMessageList) {
        this.context = context;
        this.chatMessageList = chatMessageList;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == RIGHT_MSG) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.chat_right_item, parent, false);

        }else {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.chat_left_item, parent, false);
        }
        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        ChatMessage currentChatMessage = chatMessageList.get(position);
        holder.userMessage.setText(currentChatMessage.getMessage());
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatMessageList.get(position).getSender().equals(firebaseUser.getUid()))
            return RIGHT_MSG;
        else
            return LETF_MSG;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView userMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.userMessage);
        }
    }


}
