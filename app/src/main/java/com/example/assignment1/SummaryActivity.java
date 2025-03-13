package com.example.assignment1;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryActivity extends ComponentActivity {
    private TextView summaryTextView;
    private Spinner tripSpinner;
    private Button goBackButton;
    private Button saveToFileButton;
    private TripDAO tripDAO;
    private PeopleDAO peopleDAO;
    private List<TripModel> tripList;
    private Map<String, Long> tripNameToIdMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Initialize views
        summaryTextView = findViewById(R.id.summaryTextView);
        tripSpinner = findViewById(R.id.tripSpinner);
        goBackButton = findViewById(R.id.goBackButton);
        saveToFileButton = findViewById(R.id.saveToFileButton);

        // Initialize DAOs
        tripDAO = new TripDAO(this);
        peopleDAO = new PeopleDAO(this);

        // Open database connections
        tripDAO.open();
        peopleDAO.open();

        // Load trips into the spinner
        loadTripsIntoSpinner();

        // Set up spinner selection listener
        tripSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip the "Select a trip" prompt
                    String selectedTripName = parent.getItemAtPosition(position).toString();
                    long tripId = tripNameToIdMap.get(selectedTripName);
                    displayTripDetails(tripId);
                } else {
                    // Clear the summary text if "Select a trip" is chosen
                    summaryTextView.setText("Please select a trip to view details.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set up the "Save Trip Details" button
        saveToFileButton.setOnClickListener(v -> {
            String summaryText = summaryTextView.getText().toString();
            if (summaryText.equals("Please select a trip to view details.")) {
                Toast.makeText(this, "Please select a trip first", Toast.LENGTH_SHORT).show();
            } else {
                FileUtils.saveTripDetailsToFile(this, summaryText);
            }
        });

        // Set up the "Go Back" button to finish the activity
        goBackButton.setOnClickListener(v -> finish());
    }

    private void loadTripsIntoSpinner() {
        // Get all trips from the database
        tripList = tripDAO.getAllTrips();
        tripNameToIdMap = new HashMap<>();

        // Create a list for the spinner with a prompt as the first item
        List<String> tripNames = new ArrayList<>();
        tripNames.add("Select a trip");

        // Add trip names to the list and map them to their IDs
        for (TripModel trip : tripList) {
            String tripName = trip.getTripName();
            tripNames.add(tripName);
            tripNameToIdMap.put(tripName, trip.getId());
        }

        // Create an adapter for the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tripNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the spinner
        tripSpinner.setAdapter(adapter);

        // Update the initial view
        if (tripList.isEmpty()) {
            summaryTextView.setText("No trips available. Create a trip first!");
            saveToFileButton.setEnabled(false);
        } else {
            summaryTextView.setText("Please select a trip to view details.");
            saveToFileButton.setEnabled(true);
        }
    }

    private void displayTripDetails(long tripId) {
        // Find the selected trip
        TripModel selectedTrip = null;
        for (TripModel trip : tripList) {
            if (trip.getId() == tripId) {
                selectedTrip = trip;
                break;
            }
        }

        if (selectedTrip != null) {
            // Build the summary text for the selected trip
            StringBuilder summaryBuilder = new StringBuilder();
            summaryBuilder.append("Trip Summary\n\n");
            summaryBuilder.append("Trip: ").append(selectedTrip.getTripName()).append("\n");
            summaryBuilder.append("Destination: ").append(selectedTrip.getDestination()).append("\n");
            summaryBuilder.append("Budget: ").append(selectedTrip.getBudget()).append("\n");
            summaryBuilder.append("Departure: ").append(selectedTrip.getDepartureDate()).append("\n");
            summaryBuilder.append("Return: ").append(selectedTrip.getReturnDate()).append("\n");

            // Get people associated with this trip
            List<PeopleModel> peopleList = peopleDAO.getPeopleByTripId(tripId);

            if (!peopleList.isEmpty()) {
                summaryBuilder.append("\nPeople on this trip:\n");
                for (PeopleModel person : peopleList) {
                    summaryBuilder.append("- ").append(person.getName()).append("\n");
                }
            } else {
                summaryBuilder.append("\nNo people added to this trip.\n");
            }

            // Display the summary in the TextView
            summaryTextView.setText(summaryBuilder.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        if (tripDAO != null && !tripDAO.isOpen()) {
            tripDAO.open();
        }
        if (peopleDAO != null && !peopleDAO.isOpen()) {
            peopleDAO.open();
        }
        loadTripsIntoSpinner();
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