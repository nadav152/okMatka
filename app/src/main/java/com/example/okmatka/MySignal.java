package com.example.okmatka;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class MySignal {

    private static MySignal instance;
    private static Context appContext;

    public static MySignal getInstance() {
        return instance;
    }

    private MySignal(Context context) {
        appContext = context;
    }

    public static MySignal initHelper(Context context) {
        if (instance == null)
            instance = new MySignal(context);
        return instance;
    }

    public void showToast(final String message) {
        // If we put it into handler - we can call in from asynctask outside of main uithread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addSound(int mpSound){
        MediaPlayer mediaPlayer = MediaPlayer.create(appContext, mpSound);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
    }
}