package com.example.okmatka.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.okmatka.Fragments.MatchingUsers_Fragment;
import com.example.okmatka.Fragments.Profiles_Fragment;
import com.example.okmatka.MyFireBase;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Activity_Main extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private DatabaseReference myUserRef;
    private TabLayout profiles_TAB_tabLayout;
    private ViewPager profiles_VPR_viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    FloatingActionButton main_FAB_add;
    FloatingActionButton main_FAB_settings;
    FloatingActionButton main_FAB_logout;
    private Animation openAnimation ;
    private Animation cloeAnimation ;
    private Animation fromTopAnimation;
    private Animation toBTopAnimation;
    private boolean clickable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        setAnimations();
        //todo load picture to float button on glide
        setClickersListeners();
        setFireBase();
        setTabs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkStatus("offline");
    }

    private void setTabs() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFraments(new Profiles_Fragment(),"Profiles");
        viewPagerAdapter.addFraments(new MatchingUsers_Fragment(),"Matches");

        //setting the viewPager into the tabLayout
        profiles_VPR_viewPager.setAdapter(viewPagerAdapter);
        profiles_TAB_tabLayout.setupWithViewPager(profiles_VPR_viewPager);

    }

    private void setFireBase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myUserRef = FirebaseDatabase.getInstance().
                getReference(MyFireBase.KEYS.USERS_LIST).child(firebaseUser.getUid());

        /* keeping my matches updated
            the listener will be activated on the main activity
         */
        myUserRef.addValueEventListener(myDetailsListener());
    }

    private ValueEventListener myDetailsListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User mySelf = snapshot.getValue(User.class);
                assert mySelf != null;

                //notifying my matches on my settings updates
                for(DataSnapshot myMatch : snapshot.child(MyFireBase.KEYS.USER_MATCHES_LIST).getChildren()) {
                    User myMatchUser = myMatch.getValue(User.class);
                    assert myMatchUser != null;
                    updateMyMatch(myMatchUser,mySelf);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
    }

    private void updateMyMatch(User myMatchUser, User mySelf) {
        DatabaseReference myMatchRef = MyFireBase.getInstance()
                .getReference(MyFireBase.KEYS.USERS_LIST).child(myMatchUser.getId());
        myMatchRef.child(MyFireBase.KEYS.USER_MATCHES_LIST).child(mySelf.getId()).setValue(mySelf);
    }

    private void setClickersListeners() {
        main_FAB_add.setOnClickListener(buttonsListener());
        main_FAB_logout.setOnClickListener(buttonsListener());
        main_FAB_settings.setOnClickListener(buttonsListener());
    }

    private View.OnClickListener buttonsListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getTag().equals("plus"))
                    addButtons();
                else if(view.getTag().equals("settings"))
                    changeActivity(Activity_Main.this, Activity_Settings.class,false);
                else
                    changeActivity(Activity_Main.this, Activity_Login.class,true);
            }
        };
    }

    private void addButtons() {
        setvisiblty(clickable);
        setButtonsAnimation(clickable);
        clickable = !clickable;
    }

    private void setButtonsAnimation(boolean startSetting) {
        if (!startSetting){
            main_FAB_settings.startAnimation(fromTopAnimation);
            main_FAB_logout.startAnimation(fromTopAnimation);
            main_FAB_add.startAnimation(openAnimation);
        } else {
            main_FAB_settings.startAnimation(toBTopAnimation);
            main_FAB_logout.startAnimation(toBTopAnimation);
            main_FAB_add.startAnimation(cloeAnimation);
        }
    }

    private void setvisiblty(boolean makeVisible) {
        if (!makeVisible){
            main_FAB_settings.setVisibility(View.VISIBLE);
            main_FAB_logout.setVisibility(View.VISIBLE);
        }else {
            main_FAB_settings.setVisibility(View.GONE);
            main_FAB_logout.setVisibility(View.GONE);
        }
    }

    private void changeActivity(Activity_Main activity, Class<?> activityClass,boolean finish) {
        Intent intent = new Intent(activity, activityClass);
        startActivity(intent);
        if (finish)
            finish();
    }

    private void checkStatus(String status){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        myUserRef.updateChildren(hashMap);
    }

    private void setAnimations() {
        openAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        cloeAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromTopAnimation = AnimationUtils.loadAnimation(this, R.anim.from_top_anim);
        toBTopAnimation = AnimationUtils.loadAnimation(this, R.anim.to_top_anim);
    }

    private void findViews() {
        profiles_TAB_tabLayout = findViewById(R.id.profiles_TAB_tabLayout);
        profiles_VPR_viewPager = findViewById(R.id.profiles_VPR_viewPager);
        main_FAB_add = findViewById(R.id.main_FAB_add);
        main_FAB_settings = findViewById(R.id.main_FAB_settings);
        main_FAB_logout = findViewById(R.id.main_FAB_logout);
    }



    // inner Class ViewPagerAdapter
    public static class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String > fragmentTitles;

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.fragments = new ArrayList<>();
            this.fragmentTitles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFraments(Fragment fragment,String title){
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }
}