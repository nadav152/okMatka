package com.example.okmatka;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyFireBase {

    private DatabaseReference userListRef;
    private static FirebaseDatabase database;
    private static MyFireBase instance;
    private static Context appContext;

    private MyFireBase(Context context) {
        appContext = context;
        database = FirebaseDatabase.getInstance();
    }

    public static MyFireBase getInstance() {
        return instance;
    }

    public static MyFireBase initHelper(Context context) {
        if (instance == null)
            instance = new MyFireBase(context);
        return instance;
    }

    public void addUserToList(final User user) {
        userListRef = database.getReference(KEYS.USERS_LIST);
        userListRef.child(user.getName()).setValue(user);
    }

    public DatabaseReference getReference(String path) {
       return database.getReference(path);
    }

    public interface KEYS {
        String USERS_LIST = "USERS_LIST/";
        String USERS_CHATS = "USERS_CHATS/";
        String USER_LIKES_LIST = "USER_LIKES_LIST/";
        String USER_MATCHES_LIST = "USER_MATCHES_LIST/";
        String UPLOADS = "UPLOADS";
        String NAME = "name";
        String ROLL = "roll";
        String EXPERIENCE = "experience";
        String AGE = "age";
        String FAVOURITE_BEACH = "favouriteBeach";
        String IMG_URL = "imageURL";
        String RATE = "rate";
        String NUMBER_OF_REVIEWS = "numberOfReviews";
    }


}