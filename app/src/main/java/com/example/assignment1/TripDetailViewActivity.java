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
    private Button editTripButton, addTravelersButton, saveToFileButton, goBackButton;

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

        // Replace the saveToFileButton click listener in TripDetailViewActivity.java with:

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

        goBackButton.setOnClickListener(v -> finish());
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