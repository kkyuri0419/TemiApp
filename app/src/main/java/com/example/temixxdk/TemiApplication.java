package com.example.temixxdk;

import android.app.Application;

public class TemiApplication extends Application {
    public static boolean [] booleans = new boolean[]{};
    public static int bSize;
    public static String showlocations = "";

    @Override
    public void onCreate() {
        super.onCreate();
    }
    public boolean[] getBooleans() {
        return booleans;
    }

    public void setBooleans(boolean[] booleans) {
        this.booleans = booleans;
    }
}

