package com.example.okmatka.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_ITM_logout) {
            //Logging out
            FirebaseAuth.getInstance().signOut();
            changeActivity(Activity_Main.this,Activity_Login.class,true);
            return true;
        }
        else if(item.getItemId() == R.id.menu_ITM_settings){
            //Going to settings Activity
            changeActivity(Activity_Main.this,Activity_Settings.class,false);
            return true;
        }
        return false;
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



    private void findViews() {
        profiles_TAB_tabLayout = findViewById(R.id.profiles_TAB_tabLayout);
        profiles_VPR_viewPager = findViewById(R.id.profiles_VPR_viewPager);
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