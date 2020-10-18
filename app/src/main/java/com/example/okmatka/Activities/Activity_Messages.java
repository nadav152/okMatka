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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.okmatka.Adapters.MessageAdapter;
import com.example.okmatka.ChatMessage;
import com.example.okmatka.Interfaces.InviteDialogCallback;
import com.example.okmatka.Interfaces.KeyDialogCallBack;
import com.example.okmatka.InviteDialog;
import com.example.okmatka.KeyDialog;
import com.example.okmatka.MyFireBase;
import com.example.okmatka.MySignal;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class Activity_Messages extends AppCompatActivity {

    private TextView messages_LBL_userName;
    private ImageView messages_IMG_userPic;
    private RecyclerView messages_RCV_recyclerView;
    private EditText messages_EDT_sendMessage;
    private ImageButton messages_BTN_sendButton,  messages_BTN_mapButton;
    private MaterialButton messages_BTN_inviteButton;
    private MessageAdapter messageAdapter;
    private ArrayList<ChatMessage> chatsList;
    private FirebaseUser firebaseUser;
    private String userISpeakWithId;
    private DatabaseReference myRef;
    private User userISpeakWith;
    private String matchMapIdRefKey= "def";
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

    @Override
    protected void onStop() {
        //to prevent storing a not needed data
        removeLocations();
        super.onStop();
    }

    private void removeLocations() {
        myRef.child(MyFireBase.KEYS.USERS_LOCATIONS).child(matchMapIdRefKey).removeValue();
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
        messages_BTN_inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInvitationDialog();
            }
        });
    }

    private void openInvitationDialog() {
        InviteDialog inviteDialog = new InviteDialog(inviteDialogCallback);
        inviteDialog.show(this.getSupportFragmentManager(),"Invitation");
    }

    private InviteDialogCallback inviteDialogCallback = new InviteDialogCallback() {
        @Override
        public void getAnswer(Boolean invite) {
            if (invite) {
                //checking if there is already room ready
                if (!matchMapIdRefKey.equals("def"))
                    myRef.child(MyFireBase.KEYS.USERS_LOCATIONS).child(matchMapIdRefKey).removeValue();

                //preparing new room
                String uniqueNumber = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime());
                String invitationMessage = "I made a shared location map for us," +
                        " if you wish to enter press the map icon and the key will be :\n" +
                        uniqueNumber;
                sendMessage(firebaseUser.getUid(),userISpeakWithId,invitationMessage);
                matchMapIdRefKey = uniqueNumber;
                myRef.child(MyFireBase.KEYS.USERS_LOCATIONS).child(matchMapIdRefKey).setValue("room has opened");
            }
        }
    };

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
                myRef.child(MyFireBase.KEYS.USERS_LOCATIONS).
                        addListenerForSingleValueEvent(roomExistenceListener(matchMapIdRefKey,true));
            }
        };
    }

    private ValueEventListener roomExistenceListener(final String key,final boolean enterDialog) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(key))
                    goToMapActivity(key);
                else if(enterDialog)
                    checkKeyDialog();
                else
                    MySignal.getInstance().showToast("This key is wrong\n or no longer valid");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
    }

    private void checkKeyDialog() {
        KeyDialog keyDialog = new KeyDialog(keyDialogCallBack);
        keyDialog.show(this.getSupportFragmentManager(),"key");
    }

    //checking if the dialog key matches the new map room
    private KeyDialogCallBack keyDialogCallBack = new KeyDialogCallBack() {
        @Override
        public void getKey(final String key) {
            if (!key.equals(""))
             myRef.child(MyFireBase.KEYS.USERS_LOCATIONS).
                     addListenerForSingleValueEvent(roomExistenceListener(key,false));
        }
    };

    private void goToMapActivity(String keyRef) {
        Intent intent = new Intent(Activity_Messages.this, Activity_Map.class);
        Gson gson = new Gson();
        assert userISpeakWith !=null;
        intent.putExtra(Activity_Map.KEY_REF,keyRef);
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
        messages_BTN_inviteButton = findViewById(R.id.messages_BTN_inviteButton);
    }


}