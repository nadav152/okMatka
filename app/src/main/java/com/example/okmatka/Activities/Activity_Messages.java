package com.example.okmatka.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.okmatka.Adapters.MessageAdapter;
import com.example.okmatka.ChatMessage;
import com.example.okmatka.MyFireBase;
import com.example.okmatka.MySignal;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class Activity_Messages extends AppCompatActivity {

    private TextView messages_LBL_userName;
    private ImageView messages_IMG_userPic;
    private RecyclerView messages_RCV_recyclerView;
    private EditText messages_EDT_sendMessage;
    private ImageButton messages_BTN_sendButton,  messages_BTN_mapButton;
    private SwitchCompat messages_STC_locationAllow;
    private MessageAdapter messageAdapter;
    private ArrayList<ChatMessage> chatsList;
    private FirebaseUser firebaseUser;
    private String userISpeakWithId;
    private DatabaseReference myRef;
    private User userISpeakWith;
    public static final String USER_ID = "USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        findViews();
        setFireBase();
        setConversation();
        setRecyclerView();
        loadImageToButton();
        setButtonListeners();
    }

    private void loadImageToButton() {
        Glide.with(this).load(R.drawable.map_icon).into(messages_BTN_mapButton);
    }

    private void setFireBase() {
        myRef = FirebaseDatabase.getInstance().getReference();
    }

    private void setRecyclerView() {
        messages_RCV_recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        messages_RCV_recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void setButtonListeners() {
        messages_BTN_sendButton.setOnClickListener(sendMessageListener());
        messages_BTN_mapButton.setOnClickListener(mapListener());
    }

    private View.OnClickListener sendMessageListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.valueOf(messages_EDT_sendMessage.getText());
                if (!message.equals(""))
                    sendMessage(firebaseUser.getUid(),userISpeakWithId,message);
                else
                    MySignal.getInstance().showToast("You can not send empty messages");

                messages_EDT_sendMessage.setText("");
            }
        };
    }

    private View.OnClickListener mapListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMapActivity();
            }
        };
    }

    private void goToMapActivity() {
        Intent intent = new Intent(Activity_Messages.this, Activity_Map.class);
        Gson gson = new Gson();
        assert userISpeakWith !=null;
        String myGson = gson.toJson(userISpeakWith);
        intent.putExtra(Activity_Map.USER,myGson);
        startActivity(intent);
    }

    private void sendMessage(String sender, String receiver, String message) {

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);

        myRef.child(MyFireBase.KEYS.USERS_CHATS).push().setValue(hashMap);

    }

    private void setConversation() {
        Intent intent = getIntent();
        userISpeakWithId = intent.getStringExtra(USER_ID);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert userISpeakWithId != null;
        DatabaseReference userISpeakWithReference = myRef.child(MyFireBase.KEYS.USERS_LIST).child(userISpeakWithId);
        userISpeakWithReference.addValueEventListener(valueListener());
    }

    private ValueEventListener valueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //setting the toolBar with user details first
                userISpeakWith = snapshot.getValue(User.class);
                assert userISpeakWith != null;
                messages_LBL_userName.setText(userISpeakWith.getName());

                if (userISpeakWith.getImageURL().equalsIgnoreCase("default"))
                    Glide.with(getApplicationContext()).load(R.drawable.general_user).into(messages_IMG_userPic);
                else
                    Glide.with(getApplicationContext()).load(userISpeakWith.getImageURL()).into(messages_IMG_userPic);

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
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(MyFireBase.KEYS.USERS_CHATS);
        myRef.addValueEventListener(readMessageListener(myId, userISpeakToId));

    }

    private ValueEventListener readMessageListener(final String myId, final String userISpeakToId) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();
                for (DataSnapshot snap : snapshot.getChildren()){
                    ChatMessage currentChatMessage = snap.getValue(ChatMessage.class);
                    assert currentChatMessage != null;
                    if (currentChatMessage.getReceiver().equals(myId) && currentChatMessage.getSender().equals(userISpeakToId)||
                            currentChatMessage.getReceiver().equals(userISpeakToId) && currentChatMessage.getSender().equals(myId)){
                        chatsList.add(currentChatMessage);
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

    private void findViews() {
        messages_IMG_userPic = findViewById(R.id.messages_IMG_userPic);
        messages_LBL_userName = findViewById(R.id.messages_LBL_userName);
        messages_RCV_recyclerView = findViewById(R.id.messages_RCV_recyclerView);
        messages_EDT_sendMessage = findViewById(R.id.messages_EDT_sendMessage);
        messages_BTN_sendButton = findViewById(R.id.messages_BTN_sendButton);
        messages_BTN_mapButton = findViewById(R.id.messages_BTN_mapButton);
        messages_STC_locationAllow = findViewById(R.id.messages_STC_locationAllow);
    }


}