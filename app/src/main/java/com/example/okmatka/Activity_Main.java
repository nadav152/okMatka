package com.example.okmatka;

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

import com.example.okmatka.Fragments.Chats_Fragment;
import com.example.okmatka.Fragments.MatchingUsers_Fragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Activity_Main extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private DatabaseReference myRef;
    private TabLayout profiles_TAB_tabLayout;
    private ViewPager profiles_VPR_viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        setFireBase();
        readDate();
        setTabs();
    }

    private void setTabs() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFraments(new MatchingUsers_Fragment(),"Matches");
        viewPagerAdapter.addFraments(new Chats_Fragment(),"Chats");

        //setting the viewPager into the tabLayout
        profiles_VPR_viewPager.setAdapter(viewPagerAdapter);
        profiles_TAB_tabLayout.setupWithViewPager(profiles_VPR_viewPager);

    }

    private void setFireBase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().
                getReference("USERS_LIST").child(firebaseUser.getUid());
    }

    private void readDate() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                MySignal.getInstance().showToast("User : " + user.getName() + " Logged in");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
            moveToLogin();
            return true;
        }
        return false;
    }

    private void moveToLogin() {
        Intent intent = new Intent(Activity_Main.this,Activity_Login.class);
        startActivity(intent);
        finish();
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