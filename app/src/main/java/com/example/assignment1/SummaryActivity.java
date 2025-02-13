package com.example.assignment1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
/*
 * FILE         :   SummaryActivity.java
 * PROJECT      :   PROG3150 – Assignment #1
 * PROGRAMMER   :   Josh Horsley, Daimon Quin
 * DESCRIPTION  :   This activity displays all information given by the two other activities.
 */
public class SummaryActivity extends ComponentActivity {
    private TextView summaryTextView;
    private Button goBackButton;
    private CheckBox tripFinishedCheckBox;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        sharedPreferences = getSharedPreferences("TripData", Context.MODE_PRIVATE);

        summaryTextView = findViewById(R.id.summaryTextView);
        goBackButton = findViewById(R.id.goBackButton);
        tripFinishedCheckBox = findViewById(R.id.tripFinishedCheckBox);

        loadTripAndPeopleDetails();

        boolean isTripFinished = sharedPreferences.getBoolean("trip_finished", false);
        tripFinishedCheckBox.setChecked(isTripFinished);

        tripFinishedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("trip_finished", isChecked);
            editor.apply();
            Toast.makeText(this, "Trip status updated!", Toast.LENGTH_SHORT).show();
        });

        goBackButton.setOnClickListener(v -> finish());
    }

    private void loadTripAndPeopleDetails() {
        String tripSummary = sharedPreferences.getString("trip_summary", "No trip details available");

        boolean isAdultsOnly = sharedPreferences.getBoolean("adults_only", false);
        String adultsOnlyText = isAdultsOnly ? "\n\nAdults-Only trip" : "\n\nFamily-friendly trip.";

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

        String summaryText = tripSummary + adultsOnlyText + "\n\nPeople:\n" + peopleDetails.toString();

        summaryTextView.setText(summaryText);
    }
}
