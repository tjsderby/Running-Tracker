package com.example.runningtracker.ui.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.example.runningtracker.EditEntry;
import com.example.runningtracker.MyProviderContract;
import com.example.runningtracker.R;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {
    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private boolean sorted;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Button btnSort = root.findViewById(R.id.btnSort);

        // displays table
        sorted = false;
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listItems);
        ListView list = root.findViewById(R.id.listView);
        list.setAdapter(adapter);

        Cursor sessions = getActivity().getContentResolver().query(MyProviderContract.SESSIONS_URI, new String[] { "_id", "time", "distance", "averagespeed", "notes" }, null, null, null);

        if(sessions.moveToFirst()) {
            do {
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

                // check if notes is empty
                if (TextUtils.isEmpty(notes))
                {
                    entry = "Time: " + timeString + "\n" + "Distance: " + distance + "m\n" + "Average Speed: " + strAvgSpeed + " m/s\n" + "Notes: ";
                }
                else
                {
                    entry = "Time: " + timeString + "\n" + "Distance: " + distance + "m\n" + "Average Speed: " + strAvgSpeed + " m/s\n" + "Notes: " + notes;
                }

                // adds formatted data to the text view
                listItems.add(entry);

                adapter.notifyDataSetChanged();

            } while (sessions.moveToNext());
        }
        sessions.close();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i = 0;
                int finalPosition = 0;

                // calculates the id of the selected item from the list
                // if the list is sorted it calculated the position in the sorted list

                if (sorted) {
                    Cursor sessions = getActivity().getContentResolver().query(MyProviderContract.SESSIONS_URI, new String[]{"_id", "time", "distance", "averagespeed", "notes"}, null, null, "averagespeed DESC");

                    if (sessions.moveToFirst()) {
                        while (i <= position) {
                            finalPosition = (sessions.getInt(0)) - 1;
                            i++;
                            sessions.moveToNext();
                        }
                    }
                }
                else
                {
                    finalPosition = position;
                }

                // creates a bundle with the position and starts an activity with a result expected
                Bundle bundle = new Bundle();
                bundle.putInt("position", finalPosition);
                Intent intent = new Intent(getActivity(), EditEntry.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
            }
        });

        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // displays table sorted by average speed after resetting the adapter
                sorted = true;
                adapter.clear();
                adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listItems);
                ListView list = getActivity().findViewById(R.id.listView);
                list.setAdapter(adapter);

                Cursor sessions = getActivity().getContentResolver().query(MyProviderContract.SESSIONS_URI, new String[] { "_id", "time", "distance", "averagespeed", "notes" }, null, null, "averagespeed DESC");

                if(sessions.moveToFirst()) {
                    do {
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

                        // check if notes is empty
                        if (TextUtils.isEmpty(notes))
                        {
                            entry = "Time: " + timeString + "\n" + "Distance: " + distance + "m\n" + "Average Speed: " + strAvgSpeed + " m/s\n" + "Notes: ";
                        }
                        else
                        {
                            entry = "Time: " + timeString + "\n" + "Distance: " + distance + "m\n" + "Average Speed: " + strAvgSpeed + " m/s\n" + "Notes: " + notes;
                        }

                        listItems.add(entry);

                        adapter.notifyDataSetChanged();

                    } while (sessions.moveToNext());
                }
                sessions.close();
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // displays table
        // if the user has updated an entry this will display the list again with the updated values
        sorted = false;
        adapter.clear();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listItems);
        ListView list = getActivity().findViewById(R.id.listView);
        list.setAdapter(adapter);

        Cursor sessions = getActivity().getContentResolver().query(MyProviderContract.SESSIONS_URI, new String[] { "_id", "time", "distance", "averagespeed", "notes" }, null, null, null);

        if(sessions.moveToFirst()) {
            do {
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

                // check if notes is empty
                if (TextUtils.isEmpty(notes))
                {
                    entry = "Time: " + timeString + "\n" + "Distance: " + distance + "m\n" + "Average Speed: " + strAvgSpeed + " m/s\n" + "Notes: ";
                }
                else
                {
                    entry = "Time: " + timeString + "\n" + "Distance: " + distance + "m\n" + "Average Speed: " + strAvgSpeed + " m/s\n" + "Notes: " + notes;
                }

                listItems.add(entry);

                adapter.notifyDataSetChanged();

            } while (sessions.moveToNext());
        }
        sessions.close();
    }
}