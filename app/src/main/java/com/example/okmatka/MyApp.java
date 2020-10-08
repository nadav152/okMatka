package com.example.okmatka;

import android.app.Application;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MySignal.initHelper(this);
        MySP.initHelper(this);
//        MyFireBase.initHelper(this);
    }
}
