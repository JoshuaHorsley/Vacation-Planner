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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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
            String departureDateStr = sharedPreferences.getString("departureDate", "");
            String returnDateStr = sharedPreferences.getString("returnDate", "");

            if (departureDateStr.isEmpty() || returnDateStr.isEmpty()) {
                Toast.makeText(this, "Invalid trip dates", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Attempt parsing with flexible handling of single-digit months and days
                DateTimeFormatter flexibleFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
                DateTimeFormatter strictFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                LocalDate departureDate;
                LocalDate returnDate;

                // Try parsing with flexible format first
                try {
                    departureDate = LocalDate.parse(departureDateStr, flexibleFormatter);
                    returnDate = LocalDate.parse(returnDateStr, flexibleFormatter);
                } catch (Exception e) {
                    // Fallback to strict format if flexible parsing fails
                    departureDate = LocalDate.parse(departureDateStr, strictFormatter);
                    returnDate = LocalDate.parse(returnDateStr, strictFormatter);
                }

                long days = ChronoUnit.DAYS.between(departureDate, returnDate);
                if (days < 1) days = 1; // Ensure at least 1 day

                float additionalCost = 50 * peopleCount * days;
                float updatedCost = additionalCost; // Removed the tripCost multiplication

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("people_count", peopleCount);
                editor.putFloat("updated_trip_cost", updatedCost);
                editor.apply();

                // Update displayed trip details
                String updatedTripDetails = tripDetails + "\nPeople: " + peopleCount + "\nTotal Cost: $" + updatedCost;
                tripDetailsText.setText(updatedTripDetails);

                Toast.makeText(this, "Trip updated with total cost including extra $50 per day per person!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error parsing trip dates!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
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
