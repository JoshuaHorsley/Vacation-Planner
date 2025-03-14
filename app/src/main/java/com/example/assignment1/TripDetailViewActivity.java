package com.example.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import java.util.List;

public class TripDetailViewActivity extends ComponentActivity {
    private TextView tripDetailsText, peopleListText;
    private Button editTripButton, addTravelersButton, saveToFileButton, deleteTripButton, goBackButton;

    private TripDAO tripDAO;
    private PeopleDAO peopleDAO;
    private long tripId;
    private TripModel currentTrip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail_view);

        // Initialize views
        tripDetailsText = findViewById(R.id.tripDetailsText);
        peopleListText = findViewById(R.id.peopleListText);
        editTripButton = findViewById(R.id.editTripButton);
        addTravelersButton = findViewById(R.id.addTravelersButton);
        saveToFileButton = findViewById(R.id.saveToFileButton);
        deleteTripButton = findViewById(R.id.deleteTripButton); // New delete button
        goBackButton = findViewById(R.id.goBackButton);

        // Initialize DAOs
        tripDAO = new TripDAO(this);
        peopleDAO = new PeopleDAO(this);

        // Open database connections
        tripDAO.open();
        peopleDAO.open();

        // Get tripId from intent
        Intent intent = getIntent();
        tripId = intent.getLongExtra("tripId", -1);

        if (tripId == -1) {
            Toast.makeText(this, "Error: No trip selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load trip details and people list
        loadTripDetails();
        loadPeopleList();

        // Set up button click listeners
        editTripButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(TripDetailViewActivity.this, TripDetailsActivity.class);
            editIntent.putExtra("tripId", tripId);
            startActivity(editIntent);
        });

        addTravelersButton.setOnClickListener(v -> {
            Intent peopleIntent = new Intent(TripDetailViewActivity.this, PeopleActivity.class);
            peopleIntent.putExtra("tripId", tripId);
            startActivity(peopleIntent);
        });

        saveToFileButton.setOnClickListener(v -> {
            // Show a dialog to name the file
            final EditText input = new EditText(this);
            input.setHint("Enter file name (optional)");

            new AlertDialog.Builder(this)
                    .setTitle("Save Trip Details")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String fileName = input.getText().toString().trim();

                        // Generate the content
                        String tripContent = tripDetailsText.getText().toString();
                        String peopleContent = peopleListText.getText().toString();
                        String fullContent = tripContent + "\n\n" + peopleContent;

                        // Save to file
                        FileUtils.saveTripDetailsToFile(this, fullContent, fileName);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Set up delete button click listener
        deleteTripButton.setOnClickListener(v -> {
            showDeleteTripConfirmation();
        });

        goBackButton.setOnClickListener(v -> finish());
    }

    /**
     * Show a confirmation dialog before deleting the trip
     */
    private void showDeleteTripConfirmation() {
        if (currentTrip == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Trip")
                .setMessage("Are you sure you want to delete the trip \"" + currentTrip.getTripName() + "\"? " +
                        "This will delete all travelers associated with this trip and cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTrip();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Delete the current trip and all associated travelers
     */
    private void deleteTrip() {
        // First delete all travelers associated with this trip
        List<PeopleModel> peopleList = peopleDAO.getPeopleByTripId(tripId);
        for (PeopleModel person : peopleList) {
            peopleDAO.deletePerson(person.getId());
        }

        // Then delete the trip
        tripDAO.deleteTrip(tripId);

        Toast.makeText(this, "Trip deleted successfully", Toast.LENGTH_SHORT).show();

        // Return to the previous screen
        finish();
    }

    private void loadTripDetails() {
        currentTrip = tripDAO.getTripById((int)tripId);

        if (currentTrip != null) {
            StringBuilder details = new StringBuilder();
            details.append("Trip: ").append(currentTrip.getTripName()).append("\n\n");
            details.append("Destination: ").append(currentTrip.getDestination()).append("\n\n");
            details.append("Budget: $").append(currentTrip.getBudget()).append("\n\n");
            details.append("Departure Date: ").append(currentTrip.getDepartureDate()).append("\n\n");
            details.append("Return Date: ").append(currentTrip.getReturnDate());

            tripDetailsText.setText(details.toString());
        } else {
            tripDetailsText.setText("Trip details not available");
        }
    }

    private void loadPeopleList() {
        List<PeopleModel> peopleList = peopleDAO.getPeopleByTripId(tripId);

        if (peopleList.isEmpty()) {
            peopleListText.setText("No travelers added to this trip yet.\nClick 'Add Travelers' to add people.");
        } else {
            StringBuilder peopleDisplay = new StringBuilder("Travelers:\n\n");
            for (PeopleModel person : peopleList) {
                peopleDisplay.append("• ").append(person.getName()).append("\n");
            }
            peopleListText.setText(peopleDisplay.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make sure database connections are open
        if (!tripDAO.isOpen()) {
            tripDAO.open();
        }
        if (!peopleDAO.isOpen()) {
            peopleDAO.open();
        }

        // Refresh data
        loadTripDetails();
        loadPeopleList();
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