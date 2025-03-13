package com.example.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import java.util.List;

public class PeopleActivity extends ComponentActivity {
    private TextView tripDetailsText, peopleListText;
    private EditText personNameInput;
    private Button addPersonButton, saveButton, goBackButton;

    private PeopleDAO peopleDAO;
    private TripDAO tripDAO;
    private int tripId;
    private TripModel currentTrip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_activity);

        // Initialize views
        tripDetailsText = findViewById(R.id.tripDetailsText);
        personNameInput = findViewById(R.id.personNameInput);
        peopleListText = findViewById(R.id.peopleListText);
        addPersonButton = findViewById(R.id.addPersonButton);
        saveButton = findViewById(R.id.saveButton);
        goBackButton = findViewById(R.id.goBackButton);

        // Initialize DAOs
        peopleDAO = new PeopleDAO(this);
        tripDAO = new TripDAO(this);

        // Open database connections
        peopleDAO.open();
        tripDAO.open();

        // Get tripId from intent
        Intent intent = getIntent();
        tripId = (int) intent.getLongExtra("tripId", -1);

        if (tripId == -1) {
            Toast.makeText(this, "Error: No trip selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load trip details and people list
        loadTripDetails();
        updatePeopleList();

        // Set up button click listeners
        addPersonButton.setOnClickListener(view -> addPerson());
        saveButton.setOnClickListener(view -> {
            Toast.makeText(this, "All changes saved to database", Toast.LENGTH_SHORT).show();
        });
        goBackButton.setOnClickListener(view -> finish());
    }

    private void loadTripDetails() {
        currentTrip = tripDAO.getTripById(tripId);

        if (currentTrip != null) {
            String tripDetails = "Trip: " + currentTrip.getTripName() + "\n" +
                    "Destination: " + currentTrip.getDestination() + "\n" +
                    "Dates: " + currentTrip.getDepartureDate() + " to " + currentTrip.getReturnDate();
            tripDetailsText.setText(tripDetails);
        } else {
            tripDetailsText.setText("Trip details not available");
        }
    }

    private void updatePeopleList() {
        List<PeopleModel> peopleList = peopleDAO.getPeopleByTripId(tripId);

        if (peopleList.isEmpty()) {
            peopleListText.setText("No people added to this trip.");
        } else {
            StringBuilder peopleDisplay = new StringBuilder("People in this trip:\n");
            for (PeopleModel person : peopleList) {
                peopleDisplay.append("- ").append(person.getName()).append("\n");
            }
            peopleListText.setText(peopleDisplay.toString());
        }
    }

    private void addPerson() {
        String personName = personNameInput.getText().toString().trim();
        if (personName.isEmpty()) {
            Toast.makeText(this, "Please enter a person's name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and save new person
        PeopleModel newPerson = new PeopleModel(personName, tripId);
        long personId = peopleDAO.addPerson(newPerson);

        if (personId > 0) {
            Toast.makeText(this, personName + " added to the trip!", Toast.LENGTH_SHORT).show();
            personNameInput.setText("");
            updatePeopleList();  // Refresh the list display
        } else {
            Toast.makeText(this, "Failed to add person", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePeopleList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connections
        if (peopleDAO != null) {
            peopleDAO.close();
        }
        if (tripDAO != null) {
            tripDAO.close();
        }
    }
}