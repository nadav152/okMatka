package com.example.okmatka.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.okmatka.Adapters.UserAdapter;
import com.example.okmatka.MyFireBase;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MatchingUsers_Fragment extends Fragment {

    private RecyclerView matchingUsers_RCV_recyclerView;
    private UserAdapter userAdapter;
    private ArrayList<User> matchingUsersList;
    private FirebaseUser firebaseUser;
    private DatabaseReference myMatchesRef;

    public MatchingUsers_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_matching_users, container, false);
        setFirebase();
        readUsers();
        setRecyclerView(view);
        return view;
    }

    private void setFirebase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myMatchesRef = FirebaseDatabase.getInstance().
                getReference(MyFireBase.KEYS.USERS_LIST).
                child(firebaseUser.getUid()).child(MyFireBase.KEYS.USER_MATCHES_LIST);
        matchingUsersList = new ArrayList<>();
    }

    private void readUsers() {
        /*
        when ever there is a new user or
        some user update his settings display the new list
         */
        myMatchesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchingUsersList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    User user = snap.getValue(User.class);
                    assert user != null;
                    addUserToList(user);
                }
                userAdapter = new UserAdapter(getContext(), matchingUsersList);
                matchingUsers_RCV_recyclerView.setAdapter(userAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void addUserToList(User user) {
        if (!user.getId().equals(firebaseUser.getUid()))
            matchingUsersList.add(user);
    }

    private void setRecyclerView(View view) {
        matchingUsers_RCV_recyclerView = view.findViewById(R.id.matchingUsers_RCV_recyclerView);
        matchingUsers_RCV_recyclerView.setHasFixedSize(true);
        matchingUsers_RCV_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}