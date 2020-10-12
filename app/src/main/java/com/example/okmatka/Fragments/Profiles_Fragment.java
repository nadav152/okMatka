package com.example.okmatka.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.okmatka.Interfaces.FireBaseCallBack;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class Profiles_Fragment extends Fragment {


    private ImageView profiles_IMG_character_pic;
    private TextView profiles_LBL_name, profiles_LBL_age, profiles_LBL_roll,
            profiles_LBL_experience, profiles_LBL_favourite_beach, profiles_LBL_rate;
    private MaterialButton profiles_BTN_red, profiles_BTN_green;
    private DatabaseReference appUsersRef;
    private FirebaseUser firebaseUser;
    private ArrayList<User> appUserslist;
    private User displayedUser;
    private Boolean alreadyMatched = false;
    private int index = -1;

    public Profiles_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profiles, container, false);
        findViews(view);
        initFireBase();
        readData(getFireBaseCallBack());
        setClickers();
        return view;
    }

    private FireBaseCallBack getFireBaseCallBack() {
        return new FireBaseCallBack() {
            @Override
            public void onCallBack(List<User> list) {
                setNextUser();
            }
        };
    }


    private View.OnClickListener buttonClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index != -1){
                    if (((String) view.getTag()).equals("red")) {
                        setNextUser();
                    } else
                        checkMatchWithUser(displayedUser);
                }
            }
        };
    }

    private void checkMatchWithUser(final User displayedUser) {
        final DatabaseReference displayedUserLikeList = appUsersRef.child(displayedUser.getId())
                .child(MyFireBase.KEYS.USER_LIKES_LIST);
        displayedUserLikeList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(firebaseUser.getUid())){
                    MySignal.getInstance().showToast("You have a new match with " + displayedUser.getName() + "!");
                    User myself = snapshot.child(firebaseUser.getUid()).getValue(User.class);

                    //todo add on completer listener
                    //add him to my matches list
                    assert myself != null;
                    addUserToChatList(myself, displayedUser);
                    //add my self to his matches list
                    addUserToChatList(displayedUser, myself);
                    //remove my self from his likes list
                    displayedUserLikeList.child(firebaseUser.getUid()).removeValue();

                }else
                    addUserToLikesList(displayedUser);

                //after adding the user - show me the next user
                setNextUser();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addUserToLikesList(User displayedUser) {
        DatabaseReference myLikesList = appUsersRef.child(firebaseUser.getUid())
                .child(MyFireBase.KEYS.USER_LIKES_LIST);
        myLikesList.child(displayedUser.getId()).setValue(displayedUser);
    }

    private void addUserToChatList(User mySelf,User userToBeAdded) {
        DatabaseReference myMatchesList = appUsersRef.child(mySelf.getId())
                .child(MyFireBase.KEYS.USER_MATCHES_LIST);
        myMatchesList.child(userToBeAdded.getId()).setValue(userToBeAdded);
    }

    private void readData(final FireBaseCallBack fireBaseCallBack){
        appUserslist = new ArrayList<>();
        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appUserslist.clear();
                //getting app users
                for (DataSnapshot snap : snapshot.getChildren()){
                    User currentUser = snap.getValue(User.class);
                    assert currentUser != null;
                    if (!currentUser.getId().equals(firebaseUser.getUid()))
                        appUserslist.add(currentUser);
                }
                index = appUserslist.size();
                fireBaseCallBack.onCallBack(appUserslist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        appUsersRef.addValueEventListener(usersListener);
    }

    private void setNextUser() {
        index -=1;
        if (index<0)
            index = appUserslist.size()-1;

        displayedUser = appUserslist.get(index);
        //checking if we already have a match or like
        //todo add the like check after finish testing
        checkNextUser(displayedUser, new FireBaseCallBack() {
            @Override
            public void onCallBack(List<User> list) {
                if (alreadyMatched)
                    setNextUser();
            }
        });
    }

    private void checkNextUser(final User currentlyDisplayedUser, final FireBaseCallBack fireBaseCallBack) {
        final DatabaseReference myMatchesRef = appUsersRef.child(firebaseUser.getUid()).child(MyFireBase.KEYS.USER_MATCHES_LIST);
        myMatchesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(currentlyDisplayedUser.getId())){
                    displayUser(currentlyDisplayedUser);
                    alreadyMatched = false;
                }else
                    alreadyMatched = true;

                fireBaseCallBack.onCallBack(appUserslist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayUser(final User currentUser) {
        if (currentUser.getImageURL().equalsIgnoreCase("default"))
            Glide.with(this).load(R.drawable.general_user).into(profiles_IMG_character_pic);
        else
            Glide.with(this).load(currentUser.getImageURL()).into(profiles_IMG_character_pic);

        profiles_LBL_name.setText(currentUser.getName());
        profiles_LBL_age.setText(String.valueOf(currentUser.getAge()));
        profiles_LBL_roll.setText(currentUser.getRoll());
        profiles_LBL_experience.setText(currentUser.getExperience());
        profiles_LBL_favourite_beach.setText(currentUser.getFavouriteBeach());
        profiles_LBL_rate.setText(String.valueOf(currentUser.getRate()));
    }

    private void setClickers() {
        profiles_BTN_green.setOnClickListener(buttonClickListener());
        profiles_BTN_red.setOnClickListener(buttonClickListener());
    }

    private void initFireBase() {
        appUsersRef = MyFireBase.getInstance().getReference(MyFireBase.KEYS.USERS_LIST);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void findViews(View view) {
        profiles_IMG_character_pic = view.findViewById(R.id.profiles_IMG_character_pic);
        profiles_LBL_name = view.findViewById(R.id.profiles_LBL_name);
        profiles_LBL_age = view.findViewById(R.id.profiles_LBL_age);
        profiles_LBL_roll = view.findViewById(R.id.profiles_LBL_roll);
        profiles_LBL_experience = view.findViewById(R.id.profiles_LBL_experience);
        profiles_LBL_favourite_beach = view.findViewById(R.id.profiles_LBL_favourite_beach);
        profiles_LBL_rate = view.findViewById(R.id.profiles_LBL_rate);
        profiles_BTN_red = view.findViewById(R.id.profiles_BTN_red);
        profiles_BTN_green = view.findViewById(R.id.profiles_BTN_green);
    }
}