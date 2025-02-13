package com.example.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

public class MainActivity extends ComponentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private String tripSummary = "No trip details available";

    private static final String PLANNED_TRIPS = "PlannedTrips";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button goToTripDetailsButton = findViewById(R.id.goToTripDetailsButton);
        Button goToPeopleButton = findViewById(R.id.goToPeopleButton);
        Button goToSummaryButton = findViewById(R.id.goToSummaryButton);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent receivedData = result.getData();
                        tripSummary = receivedData.getStringExtra("returned_data");
                        Toast.makeText(MainActivity.this, "Updated: " + tripSummary, Toast.LENGTH_LONG).show();
                    }
                });



        goToTripDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TripDetailsActivity.class);
                intent.putExtra("message", "Enter Trip Details");
                activityResultLauncher.launch(intent);
            }
        });


        goToPeopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PeopleActivity.class);
                intent.putExtra("data_from_main_to_3", tripSummary);  // Pass trip summary
                startActivity(intent);
            }
        });

        goToSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
                intent.putExtra("message", "Manage Travelers");
//                activityResultLauncher.launch(intent);
            }
        });

    }
}
