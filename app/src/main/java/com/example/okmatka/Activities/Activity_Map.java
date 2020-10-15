package com.example.okmatka.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.okmatka.Fragments.Map_Fragment;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.gson.Gson;

public class Activity_Map extends AppCompatActivity {

    private String userISpeakWithId;
    private Map_Fragment map_fragment;
    private User user;
    public static final String USER = "USER";


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
    }

    private void initFragment() {
        map_fragment = new Map_Fragment(user);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.map_LAY_map, map_fragment);
        transaction.commit();
    }

}