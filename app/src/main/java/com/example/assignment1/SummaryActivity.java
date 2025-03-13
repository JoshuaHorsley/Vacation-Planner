package com.example.assignment1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import java.util.List;

public class SummaryActivity extends ComponentActivity {
    private TextView summaryTextView;
    private Button goBackButton;
    private Button saveToFileButton;
    private TripDAO tripDAO;
    private PeopleDAO peopleDAO;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Initialize views
        summaryTextView = findViewById(R.id.summaryTextView);
        goBackButton = findViewById(R.id.goBackButton);
        saveToFileButton = findViewById(R.id.saveToFileButton);

        // Initialize DAOs
        tripDAO = new TripDAO(this);
        peopleDAO = new PeopleDAO(this);

        // Open database connections
        tripDAO.open();
        peopleDAO.open();

        // Load and display trip details from database
        loadTripDetails();

        // Set up the "Save Trip Details" button
        saveToFileButton.setOnClickListener(v -> {
            String summaryText = summaryTextView.getText().toString();
            FileUtils.saveTripDetailsToFile(this, summaryText);
        });

        // Set up the "Go Back" button to finish the activity
        goBackButton.setOnClickListener(v -> finish());
    }

    private void loadTripDetails() {
        // Get all trips from the database
        List<TripModel> tripList = tripDAO.getAllTrips();

        // Create a formatted string with trip details
        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append("Trip Summary\n\n");

        if (tripList.isEmpty()) {
            summaryBuilder.append("No trips available.");
        } else {
            for (TripModel trip : tripList) {
                summaryBuilder.append("Trip: ").append(trip.getTripName()).append("\n");
                summaryBuilder.append("Destination: ").append(trip.getDestination()).append("\n");
                summaryBuilder.append("Budget: ").append(trip.getBudget()).append("\n");
                summaryBuilder.append("Departure: ").append(trip.getDepartureDate()).append("\n");
                summaryBuilder.append("Return: ").append(trip.getReturnDate()).append("\n");

                // Get people associated with this trip
                List<PeopleModel> peopleList = peopleDAO.getPeopleByTripId(trip.getId());

                if (!peopleList.isEmpty()) {
                    summaryBuilder.append("\nPeople on this trip:\n");
                    for (PeopleModel person : peopleList) {
                        summaryBuilder.append("- ").append(person.getName()).append("\n");
                    }
                } else {
                    summaryBuilder.append("\nNo people added to this trip.\n");
                }

                summaryBuilder.append("\n-----------------------------------------\n\n");
            }
        }

        // Display the summary in the TextView
        summaryTextView.setText(summaryBuilder.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        loadTripDetails();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connections
        if (tripDAO != null) {
            tripDAO.close();
        }
        if (peopleDAO != null) {
            peopleDAO.close();
        }
    }
}