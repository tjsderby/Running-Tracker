package com.example.runningtracker;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class EditEntry extends AppCompatActivity {
    private EditText txtNotes;
    private int position;
    private String selection;
    private ImageView imageView;
    private Button btnAddImage;
    private String imagePath;
    private static final int PERMISSION_REQUEST = 0;
    private static final int RESULT_LOAD_IMAGE = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editentry);

        imageView = findViewById(R.id.imageView);
        btnAddImage = findViewById(R.id.btnAddImage);

        // checks if user has permissions adn enables the buttons if they do
        if (!runtimePermissions())
            enableButton();

        position = 0;
        selection = "_id = " + position;

        // if information is passed it sets the position number
        // adds 1 as the database starts from 1 instead of 0
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            position = bundle.getInt("position");
            position++;
            selection = "_id = " + position;
        }

        // displays table in a text view based on position
        TextView txtEntry = findViewById(R.id.txtEntry);
        Button btnSave = findViewById(R.id.btnSave);
        txtNotes = findViewById(R.id.txtNotes);

        Cursor sessions = getContentResolver().query(MyProviderContract.SESSIONS_URI, new String[] { "_id", "time", "distance", "averagespeed", "notes", "image"}, selection, null, null);

        if(sessions.moveToFirst()) {

            String entry;

            // format time
            int totalSecs = sessions.getInt(1);
            int hours = totalSecs / 3600;
            int minutes = (totalSecs % 3600) / 60;
            int seconds = totalSecs % 60;
            String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            // format distance
            int distance = sessions.getInt(2);

            // format speed
            float avgSpeed = sessions.getFloat(3);
            String strAvgSpeed = String.format("%.2f", avgSpeed);

            // format notes
            String notes = sessions.getString(4);

            // format image
            String imagePath = sessions.getString(5);

            // check if notes is empty
            if (TextUtils.isEmpty(notes))
            {
                entry = "Time: " + timeString + "\n" + "Distance: " + distance + "m\n" + "Average Speed: " + strAvgSpeed + " m/s";
            }
            else
            {
                entry = "Time: " + timeString + "\n" + "Distance: " + distance + "m\n" + "Average Speed: " + strAvgSpeed + " m/s";
                txtNotes.setText(notes);
            }

            // check if image in empty
            if (TextUtils.isEmpty(imagePath))
            {
                // do nothing
            }
            else
            {
                imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            }

            txtEntry.setText(entry);
        }

        final String finalSelection = selection;
        final int finalPosition = position;

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // updates the database depending on which values have been entered by the user
                if (finalPosition != 0) {
                    if (txtNotes.getText().toString().equals("") && TextUtils.isEmpty(imagePath)) {
                        // do nothing
                    } else if (!txtNotes.getText().toString().equals("") && TextUtils.isEmpty(imagePath)) {
                        ContentValues updateValues = new ContentValues();
                        updateValues.put(MyProviderContract.NOTES, txtNotes.getText().toString());
                        getContentResolver().update(MyProviderContract.SESSIONS_URI, updateValues, finalSelection, null);
                    } else if (txtNotes.getText().toString().equals("") && !TextUtils.isEmpty(imagePath)) {
                        ContentValues updateValues = new ContentValues();
                        updateValues.put(MyProviderContract.IMAGE, imagePath);
                        getContentResolver().update(MyProviderContract.SESSIONS_URI, updateValues, finalSelection, null);
                    } else {
                        ContentValues updateValues = new ContentValues();
                        updateValues.put(MyProviderContract.NOTES, txtNotes.getText().toString());
                        updateValues.put(MyProviderContract.IMAGE, imagePath);
                        getContentResolver().update(MyProviderContract.SESSIONS_URI, updateValues, finalSelection, null);
                    }
                    finish();
                }
            }
        });
    }

    private void enableButton() {
        btnAddImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // allows the user to select an image with a results value expected
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
    }

    private boolean runtimePermissions() {
        // checks if the user has permissions enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // if they allow it enables the buttons
                    enableButton();
                }
                else
                {
                    // if they don't it asks again
                    runtimePermissions();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case RESULT_LOAD_IMAGE:
                if (resultCode == RESULT_OK)
                {
                    // retrieves image path once they select an image
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imagePath = cursor.getString(columnIndex);
                    cursor.close();
                    imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                }
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // saving edit text state for persistence
        outState.putString("txtNotes", txtNotes.getText().toString());
        outState.putInt("position", position);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            // restoring state
            if (savedInstanceState.getInt("position") == position)
            {
                txtNotes.setText(savedInstanceState.getString("txtNotes"));
            }
        }
    }
}
