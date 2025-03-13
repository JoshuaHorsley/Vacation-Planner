package com.example.assignment1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

public class SummaryActivity extends ComponentActivity {
    private TextView summaryTextView;
    private Button goBackButton;
    private Button saveToFileButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Initialize views
        summaryTextView = findViewById(R.id.summaryTextView);
        goBackButton = findViewById(R.id.goBackButton);

        // Load and display trip and people details
        loadTripAndPeopleDetails();

        saveToFileButton = findViewById(R.id.saveToFileButton);

        // Set up the "Save Trip Details" button
        saveToFileButton.setOnClickListener(v -> {
            // Retrieve the trip summary from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("TripData", Context.MODE_PRIVATE);
            String tripSummary = sharedPreferences.getString("trip_summary", "No trip details available");

            // Load people list
            String peopleJson = sharedPreferences.getString("people_list", "[]");
            StringBuilder peopleDetails = new StringBuilder();

            try {
                JSONArray jsonArray = new JSONArray(peopleJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    peopleDetails.append("- ").append(jsonArray.getString(i)).append("\n");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Combine trip details and people list into a single summary
            String summaryText = tripSummary + "\n\nPeople:\n" + peopleDetails.toString();

            // Save the summary to a file
            FileUtils.saveTripDetailsToFile(this, summaryText);
        });

        // Set up the "Go Back" button to finish the activity
        goBackButton.setOnClickListener(v -> finish());
    }

    private void loadTripAndPeopleDetails() {
        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("TripData", Context.MODE_PRIVATE);

        // Load trip details
        String tripSummary = sharedPreferences.getString("trip_summary", "No trip details available");

        // Load people list
        String peopleJson = sharedPreferences.getString("people_list", "[]");
        StringBuilder peopleDetails = new StringBuilder();

        try {
            JSONArray jsonArray = new JSONArray(peopleJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                peopleDetails.append("- ").append(jsonArray.getString(i)).append("\n");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Combine trip details and people list into a single summary
        String summaryText = tripSummary + "\n\nPeople:\n" + peopleDetails.toString();

        // Display the summary in the TextView
        summaryTextView.setText(summaryText);
    }
}