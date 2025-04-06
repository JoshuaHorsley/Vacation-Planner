package com.example.assignment1.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import java.util.List;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.example.assignment1.R;
import com.example.assignment1.database.TripDAO;
import com.example.assignment1.model.TripModel;

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
            TripDAO tripDAO = new TripDAO(MainActivity.this);
            tripDAO.open();

            // Fetch the first available trip (or show a message if no trips exist)
            List<TripModel> trips = tripDAO.getAllTrips();
            tripDAO.close();

            if (trips.isEmpty()) {
                Toast.makeText(MainActivity.this, "No trips found. Create a trip first!", Toast.LENGTH_LONG).show();
            } else {
                TripModel selectedTrip = trips.get(0); // For now, pick the first trip (you can add a trip selector later)
                Intent intent = new Intent(MainActivity.this, PeopleActivity.class);
                intent.putExtra("tripId", selectedTrip.getId()); // Pass the trip ID
                startActivity(intent);
            }
        });

        goToSummaryButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
            startActivity(intent);
        });

        Button fileManagerButton = findViewById(R.id.fileManagerButton);
        fileManagerButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FileManagerActivity.class);
            startActivity(intent);
        });
    }
}
