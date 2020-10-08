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
    private ArrayList<User> userList;
    private FirebaseUser firebaseUser;
    private DatabaseReference myRef;

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
        myRef = FirebaseDatabase.getInstance().getReference("USERS_LIST");
        userList = new ArrayList<>();
    }

    private void readUsers() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    User user = snap.getValue(User.class);

                    assert user != null;
                    if (!user.getId().equals(firebaseUser.getUid()))
                        userList.add(user);

                }
                userAdapter = new UserAdapter(getContext(), userList);
                matchingUsers_RCV_recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setRecyclerView(View view) {
        matchingUsers_RCV_recyclerView = view.findViewById(R.id.matchingUsers_RCV_recyclerView);
        matchingUsers_RCV_recyclerView.setHasFixedSize(true);
        matchingUsers_RCV_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}