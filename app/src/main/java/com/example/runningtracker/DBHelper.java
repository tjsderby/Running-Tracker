package com.example.runningtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{

    public DBHelper(Context context)
    {
        super(context, "DB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // creates the tables with appropriate fields
        db.execSQL("CREATE TABLE sessions (" + "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " + "time INTEGER NOT NULL, " + "distance FLOAT NOT NULL, " + "averagespeed FLOAT NOT NULL, " + "notes VARCHAR(128), " + "image VARCHAR(128)" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (newVersion > oldVersion) {
            // nothing
        }
    }
}
