package com.example.assignment1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
/*
 * FILE         :   MainActivity.java
 * PROJECT      :   PROG3150 – Assignment #1
 * PROGRAMMER   :   Josh Horsley, Daimon Quin
 * DESCRIPTION  :   This activity is the main menu with three buttons that open up the Trip Details,
 *                  the Travelers, and the Summary page. It also holds a Clear Cashe button to
 *                  reset the app information
 */
public class MainActivity extends ComponentActivity {
    private static final String TRIP_DATA = "TripData";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(TRIP_DATA, Context.MODE_PRIVATE);

        Button goToTripDetailsButton = findViewById(R.id.goToTripDetailsButton);
        Button goToPeopleButton = findViewById(R.id.goToPeopleButton);
        Button goToSummaryButton = findViewById(R.id.goToSummaryButton);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Toast.makeText(MainActivity.this, "Trip details updated!", Toast.LENGTH_LONG).show();
                    }
                });

        goToTripDetailsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, TripDetailsActivity.class);
            activityResultLauncher.launch(intent);
        });

        goToPeopleButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PeopleActivity.class);
            startActivity(intent);
        });

        goToSummaryButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
            startActivity(intent);
        });

        Button clearCacheButton = findViewById(R.id.clearCacheButton);
        clearCacheButton.setOnClickListener(view -> {
            SharedPreferences tripData = getSharedPreferences("TripData", Context.MODE_PRIVATE);
            SharedPreferences cashedOutTrips = getSharedPreferences("CashedOutTrips", Context.MODE_PRIVATE);

            tripData.edit().clear().apply();
            cashedOutTrips.edit().clear().apply();

            Toast.makeText(MainActivity.this, "All saved data cleared!", Toast.LENGTH_SHORT).show();
        });
    }
}
