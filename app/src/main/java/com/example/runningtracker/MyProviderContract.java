package com.example.runningtracker;

import android.net.Uri;

public class MyProviderContract {
    // defines columns and tables for the database
    public static final String AUTHORITY = "com.example.runningtracker.MyProvider";

    public static final Uri SESSIONS_URI = Uri.parse("content://"+AUTHORITY+"/sessions");

    public static final String _ID = "_id";
    public static final String TIME = "time";
    public static final String DISTANCE = "distance";
    public static final String AVERAGESPEED = "averagespeed";
    public static final String NOTES = "notes";
    public static final String IMAGE = "image";
}
