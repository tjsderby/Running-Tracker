package com.example.runningtracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class MyProvider extends ContentProvider {
    private DBHelper dbHelper = null;
    SQLiteDatabase db;
    private static final UriMatcher uriMatcher;

    static {
        // defines the tables from MyProviderContract
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MyProviderContract.AUTHORITY, "sessions", 1);
        uriMatcher.addURI(MyProviderContract.AUTHORITY, "sessions/#", 2);
    }

    @Override
    public boolean onCreate() {
        Log.d("g53mdp", "contentprovider oncreate");
        this.dbHelper = new DBHelper(this.getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        if (uri.getLastPathSegment()==null)
        {
            return "vnd.android.cursor.dir/MyProvider.data.text";
        }
        else
        {
            return "vnd.android.cursor.item/MyProvider.data.text";
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // gets a writable database and inserts an entry using parameters
        db = dbHelper.getWritableDatabase();
        String tableName;

        tableName = "sessions";

        long id = db.insert(tableName, null, values);
        db.close();
        Uri nu = ContentUris.withAppendedId(uri, id);

        Log.d("g53mdp", nu.toString());

        getContext().getContentResolver().notifyChange(nu, null);

        return nu;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // gets writable database and queries with parameters
        db = dbHelper.getWritableDatabase();
        Cursor c;

        c = db.query("sessions", projection, selection, selectionArgs, null, null, sortOrder);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // gets writable database and updates with parameters
        db = dbHelper.getWritableDatabase();
        String tableName;

        tableName = "sessions";

        int updated = db.update(tableName, values, selection, selectionArgs);

        if (updated != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // gets writable database and deletes with parameters
        db = dbHelper.getWritableDatabase();
        String tableName;

        tableName = "sessions";

        int deleted = db.delete(tableName, selection, selectionArgs);

        if (deleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deleted;
    }
}
