package com.example.okmatka.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.okmatka.Fragments.Map_Fragment;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.gson.Gson;

public class Activity_Map extends AppCompatActivity {

    private Map_Fragment map_fragment;
    private User user;
    private String usersLocationRef;
    private boolean iCreatedTheRoom;
    public static final String USER = "USER";
    public static final String KEY_REF = "KEY_REF";
    public static final String ROOM_CREATOR = "ROOM_CREATOR";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getMyIntent();
        initFragment();
    }

    private void getMyIntent() {
        Gson gson = new Gson();
        user =  gson.fromJson(getIntent().getStringExtra(USER), User.class);
        usersLocationRef = getIntent().getStringExtra(KEY_REF);
        iCreatedTheRoom = getIntent().getBooleanExtra(ROOM_CREATOR,false);
    }

    private void initFragment() {
        map_fragment = new Map_Fragment(user,usersLocationRef,iCreatedTheRoom);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.map_LAY_map, map_fragment);
        transaction.commit();
    }

}