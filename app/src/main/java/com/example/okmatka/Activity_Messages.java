package com.example.okmatka;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.okmatka.Adapters.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Activity_Messages extends AppCompatActivity {

    private TextView messages_LBL_userName;
    private ImageView messages_IMG_userPic;
    private RecyclerView messages_RCV_recyclerView;
    private EditText messages_EDT_sendMessage;
    private ImageButton messages_BTN_sendButton;
    private MessageAdapter messageAdapter;
    private ArrayList<Chat> chatsList;
    private FirebaseUser firebaseUser;
    private Intent intent;
    private String userISpeakWithId;
    public static final String USER_ID = "USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        findViews();
        setConversation();
        setRecyclerView();
        setButtonListeners();
    }

    private void setRecyclerView() {
        messages_RCV_recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        messages_RCV_recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void setButtonListeners() {
        messages_BTN_sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.valueOf(messages_EDT_sendMessage.getText());
                if (!message.equals(""))
                    sendMessage(firebaseUser.getUid(),userISpeakWithId,message);
                else
                    MySignal.getInstance().showToast("You can not send empty messages");

                messages_EDT_sendMessage.setText("");
            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);

        myRef.child("USERS_CHATS").push().setValue(hashMap);

    }

    private void setConversation() {
        intent = getIntent();
        userISpeakWithId = intent.getStringExtra(USER_ID);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert userISpeakWithId != null;
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("USERS_LIST").child(userISpeakWithId);
        userReference.addValueEventListener(valueListener());
    }

    private ValueEventListener valueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //setting the toolBar with user details first
                User userISpeakWith = snapshot.getValue(User.class);
                assert userISpeakWith != null;
                messages_LBL_userName.setText(userISpeakWith.getName());

                if (userISpeakWith.getImageURL().equalsIgnoreCase("default"))
                    messages_IMG_userPic.setImageResource(R.mipmap.ic_launcher);
                else
                    glide(userISpeakWith.getImageURL(),messages_IMG_userPic);

                //reading the latest messages from the user
                readMessage(firebaseUser.getUid(),userISpeakWithId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void readMessage(final String myId, final String userISpeakToId) {
        chatsList = new ArrayList<>();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("USERS_CHATS");
        myRef.addValueEventListener(readMessageListener(myId, userISpeakToId));

    }

    private ValueEventListener readMessageListener(final String myId, final String userISpeakToId) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();
                for (DataSnapshot snap : snapshot.getChildren()){
                    Chat currentChat = snap.getValue(Chat.class);
                    if (currentChat.getReceiver().equals(myId) && currentChat.getSender().equals(userISpeakToId)||
                            currentChat.getReceiver().equals(userISpeakToId) && currentChat.getSender().equals(myId)){
                        chatsList.add(currentChat);
                    }
                    messageAdapter = new MessageAdapter(Activity_Messages.this,chatsList);
                    messages_RCV_recyclerView.setAdapter(messageAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void glide(String url, ImageView into) {
        Glide.with(this).load(url).into(into);
    }

    private void findViews() {
        messages_IMG_userPic = findViewById(R.id.messages_IMG_userPic);
        messages_LBL_userName = findViewById(R.id.messages_LBL_userName);
        messages_RCV_recyclerView = findViewById(R.id.messages_RCV_recyclerView);
        messages_EDT_sendMessage = findViewById(R.id.messages_EDT_sendMessage);
        messages_BTN_sendButton = findViewById(R.id.messages_BTN_sendButton);
    }


}