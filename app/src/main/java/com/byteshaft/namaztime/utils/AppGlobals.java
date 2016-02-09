package com.byteshaft.namaztime.utils;

import android.app.Application;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;

public class AppGlobals extends Application {

    public static ArrayList<Geofence> sGeoFenceList;

    @Override
    public void onCreate() {
        super.onCreate();
        sGeoFenceList = new ArrayList<>();

    }
}
