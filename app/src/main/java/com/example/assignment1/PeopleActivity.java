package com.example.assignment1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

public class PeopleActivity extends ComponentActivity {
    private static final String TRIP_DATA = "TripData";
    private SharedPreferences sharedPreferences;
    private TextView tripDetailsText;
    private EditText peopleCountInput;
    private Button saveButton, goBackButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_activity);

        sharedPreferences = getSharedPreferences(TRIP_DATA, Context.MODE_PRIVATE);
        tripDetailsText = findViewById(R.id.tripDetailsText);
        peopleCountInput = findViewById(R.id.peopleCountInput);
        saveButton = findViewById(R.id.saveButton);
        goBackButton = findViewById(R.id.goBackButton);

        // Load trip details
        String tripDetails = sharedPreferences.getString("trip_summary", "No trip details available");
        tripDetailsText.setText(tripDetails);

        saveButton.setOnClickListener(view -> {
            String peopleCountStr = peopleCountInput.getText().toString();
            if (peopleCountStr.isEmpty()) {
                Toast.makeText(this, "Please enter number of people", Toast.LENGTH_SHORT).show();
                return;
            }

            int peopleCount = Integer.parseInt(peopleCountStr);
            float tripCost = sharedPreferences.getFloat("trip_cost", 0);
            float updatedCost = tripCost * peopleCount;

            // Retrieve and update the trip summary
            String tripSummary = sharedPreferences.getString("trip_summary", "No trip details available");
            String updatedTripSummary = tripSummary + "\nPeople: " + peopleCount + "\nTotal Cost: $" + updatedCost;

            // Save updated cost and summary
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("people_count", peopleCount);
            editor.putFloat("updated_trip_cost", updatedCost);
            editor.putString("trip_summary", updatedTripSummary);
            editor.apply();

            // **Add to CashedOutTrips**
            SharedPreferences cashedOutTripsPrefs = getSharedPreferences("CashedOutTrips", Context.MODE_PRIVATE);
            SharedPreferences.Editor cashOutEditor = cashedOutTripsPrefs.edit();
            String tripKey = "Trip_" + System.currentTimeMillis(); // Unique key
            cashOutEditor.putString(tripKey, updatedTripSummary);
            cashOutEditor.apply();

            Toast.makeText(this, "Trip saved & cashed out!", Toast.LENGTH_SHORT).show();
        });



        goBackButton.setOnClickListener(view -> finish());
    }
}
