package com.example.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeopleActivity extends ComponentActivity {
    private TextView tripDetailsText, peopleListText;
    private EditText personNameInput;
    private Button addPersonButton, saveButton, goBackButton;
    private Spinner tripSpinner;

    private PeopleDAO peopleDAO;
    private TripDAO tripDAO;
    private long currentTripId = -1;
    private Map<String, Long> tripNameToIdMap;

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
        tripSpinner = findViewById(R.id.tripSpinner);

        // Initialize DAOs
        peopleDAO = new PeopleDAO(this);
        tripDAO = new TripDAO(this);

        // Open database connections
        peopleDAO.open();
        tripDAO.open();

        // Get tripId from intent (if provided)
        Intent intent = getIntent();
        long tripId = intent.getLongExtra("tripId", -1);
        if (tripId != -1) {
            currentTripId = tripId;
        }

        // Load trips into spinner
        loadTripsIntoSpinner();

        // Set up spinner selection listener
        tripSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip the "Select a trip" prompt
                    String selectedTripName = parent.getItemAtPosition(position).toString();
                    currentTripId = tripNameToIdMap.get(selectedTripName);
                    loadTripDetails();
                    updatePeopleList();

                    // Enable the add person functionality
                    personNameInput.setEnabled(true);
                    addPersonButton.setEnabled(true);
                } else {
                    // Clear trip details and disable adding people if no trip is selected
                    tripDetailsText.setText("Please select a trip");
                    peopleListText.setText("");
                    currentTripId = -1;

                    // Disable the add person functionality
                    personNameInput.setEnabled(false);
                    addPersonButton.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set up button click listeners
        addPersonButton.setOnClickListener(view -> addPerson());
        saveButton.setOnClickListener(view -> {
            Toast.makeText(this, "All changes saved to database", Toast.LENGTH_SHORT).show();
        });
        goBackButton.setOnClickListener(view -> finish());

        // Initially disable the add person functionality until a trip is selected
        personNameInput.setEnabled(false);
        addPersonButton.setEnabled(false);
    }

    private void loadTripsIntoSpinner() {
        // Get all trips from the database
        List<TripModel> tripList = tripDAO.getAllTrips();
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

        // Pre-select the trip if one was passed in the intent
        if (currentTripId != -1) {
            for (int i = 1; i < tripNames.size(); i++) {
                String tripName = tripNames.get(i);
                if (tripNameToIdMap.get(tripName) == currentTripId) {
                    tripSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void loadTripDetails() {
        if (currentTripId == -1) {
            tripDetailsText.setText("No trip selected");
            return;
        }

        TripModel currentTrip = tripDAO.getTripById((int)currentTripId);

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
        if (currentTripId == -1) {
            peopleListText.setText("");
            return;
        }

        List<PeopleModel> peopleList = peopleDAO.getPeopleByTripId(currentTripId);

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

        if (currentTripId == -1) {
            Toast.makeText(this, "Please select a trip first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and save new person
        PeopleModel newPerson = new PeopleModel(personName, currentTripId);
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

        // Make sure database connections are open
        if (!peopleDAO.isOpen()) {
            peopleDAO.open();
        }
        if (!tripDAO.isOpen()) {
            tripDAO.open();
        }

        // Refresh data
        loadTripsIntoSpinner();
        if (currentTripId != -1) {
            updatePeopleList();
        }
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