package com.example.okmatka.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.okmatka.Fragments.MatchingUsers_Fragment;
import com.example.okmatka.Fragments.Profiles_Fragment;
import com.example.okmatka.MyFireBase;
import com.example.okmatka.MySignal;
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
        loadFloatBtnPictures();
        setClickersListeners();
        setFireBase();
        setTabs();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationPermissions();
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
        viewPagerAdapter.addFragments(new Profiles_Fragment(),"Profiles");
        viewPagerAdapter.addFragments(new MatchingUsers_Fragment(),"Matches");

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

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
    }

    //checking if the the user gave permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                MySignal.getInstance().showToast("Some functions may not work..");
        }
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
                else {
                    FirebaseAuth.getInstance().signOut();
                    changeActivity(Activity_Main.this, Activity_Login.class, true);
                }
            }
        };
    }

    private void addButtons() {
        setVisibly(clickable);
        setButtonsAnimation(clickable);
        setClicks(clickable);
        clickable = !clickable;
    }

    private void setClicks(boolean clickable) {
        if(!clickable) {
            main_FAB_settings.setClickable(true);
            main_FAB_logout.setClickable(true);
        }else {
            main_FAB_settings.setClickable(false);
            main_FAB_logout.setClickable(false);
        }

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

    private void setVisibly(boolean makeVisible) {
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

    private void loadFloatBtnPictures() {
        Glide.with(this).load(R.drawable.add).into(main_FAB_add);
        Glide.with(this).load(R.drawable.settings).into(main_FAB_settings);
        Glide.with(this).load(R.drawable.exit_user).into(main_FAB_logout);
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

        public void addFragments(Fragment fragment, String title){
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