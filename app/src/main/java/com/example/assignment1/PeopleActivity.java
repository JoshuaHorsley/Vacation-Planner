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
            float days = sharedPreferences.getFloat("trip_days", 1); // Assuming trip_days is saved
            float additionalCost = 50 * peopleCount * days;
            float updatedCost = (tripCost * peopleCount) + additionalCost;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("people_count", peopleCount);
            editor.putFloat("updated_trip_cost", updatedCost);
            editor.apply();

            // Update trip summary
            String updatedTripDetails = sharedPreferences.getString("trip_summary", "No trip details available")
                    + "\nPeople: " + peopleCount + "\nTotal Cost: $" + updatedCost;

            tripDetailsText.setText(updatedTripDetails);

            // **Save to CashedOutTrips for SummaryActivity**
            SharedPreferences cashedOutTripsPrefs = getSharedPreferences("CashedOutTrips", Context.MODE_PRIVATE);
            SharedPreferences.Editor cashOutEditor = cashedOutTripsPrefs.edit();
            String tripKey = "Trip_" + System.currentTimeMillis(); // Unique key
            cashOutEditor.putString(tripKey, updatedTripDetails);
            cashOutEditor.apply();

            Toast.makeText(this, "Trip updated & cashed out!", Toast.LENGTH_SHORT).show();
        });


        goBackButton.setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetPeopleActivity();
    }

    private void resetPeopleActivity() {
        // Reset the input field
        peopleCountInput.setText("");

        // Reload the trip summary
        String tripDetails = sharedPreferences.getString("trip_summary", "No trip details available");
        tripDetailsText.setText(tripDetails);
    }
}
