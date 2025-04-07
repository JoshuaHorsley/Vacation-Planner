package com.example.assignment1.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.assignment1.R;
import com.example.assignment1.database.PeopleDAO;
import com.example.assignment1.database.TripDAO;
import com.example.assignment1.model.PeopleModel;
import com.example.assignment1.model.TripModel;
import com.example.assignment1.utils.ContactsUtility;
import com.example.assignment1.utils.PermissionsUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeopleActivity extends ComponentActivity {
    private static final String TAG = "PeopleActivity";

    private TextView tripDetailsText;
    private EditText personNameInput;
    private Button addPersonButton, addFromContactsButton, saveButton, goBackButton;
    private Spinner tripSpinner;
    private ListView peopleListView;

    private PeopleDAO peopleDAO;
    private TripDAO tripDAO;
    private long currentTripId = -1;
    private Map<String, Long> tripNameToIdMap;
    private List<PeopleModel> currentPeopleList;
    private ArrayAdapter<String> peopleAdapter;
    private List<String> displayNames;
    private Map<Integer, Long> positionToPersonIdMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_activity);

        // Initialize views
        tripDetailsText = findViewById(R.id.tripDetailsText);
        personNameInput = findViewById(R.id.personNameInput);
        peopleListView = findViewById(R.id.peopleListView); // New ListView
        addPersonButton = findViewById(R.id.addPersonButton);
        saveButton = findViewById(R.id.saveButton);
        goBackButton = findViewById(R.id.goBackButton);
        tripSpinner = findViewById(R.id.tripSpinner);

        // Add the new button for contacts
        addFromContactsButton = findViewById(R.id.addFromContactsButton);

        // Initialize collections
        displayNames = new ArrayList<>();
        positionToPersonIdMap = new HashMap<>();

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

        // Set up people list adapter
        peopleAdapter = new ArrayAdapter<>(
                this,
                R.layout.trip_list_item, // Use your custom list item
                android.R.id.text1,
                displayNames
        );
        peopleListView.setAdapter(peopleAdapter);

        // Set up item click listener for removing people
        peopleListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < displayNames.size()) {
                showRemovePersonDialog(displayNames.get(position), position);
            }
        });

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
                    addFromContactsButton.setEnabled(true);
                } else {
                    // Clear trip details and disable adding people if no trip is selected
                    tripDetailsText.setText("Please select a trip");
                    displayNames.clear();
                    peopleAdapter.notifyDataSetChanged();
                    currentTripId = -1;

                    // Disable the add person functionality
                    personNameInput.setEnabled(false);
                    addPersonButton.setEnabled(false);
                    addFromContactsButton.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set up button click listeners
        addPersonButton.setOnClickListener(view -> addPerson());

        // Add new button for selecting contacts
        addFromContactsButton.setOnClickListener(view -> showContactsSelection());

        saveButton.setOnClickListener(view -> {
            Toast.makeText(this, "All changes saved to database", Toast.LENGTH_SHORT).show();
        });
        goBackButton.setOnClickListener(view -> finish());

        // Initially disable the add person functionality until a trip is selected
        personNameInput.setEnabled(false);
        addPersonButton.setEnabled(false);
        addFromContactsButton.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionsUtility.REQUEST_CONTACTS_PERMISSION) {
            if (PermissionsUtility.handlePermissionResult(requestCode, grantResults)) {
                // Permission granted, show contacts selection
                showContactsSelectionDialog();
            } else {
                // Permission denied
                Toast.makeText(this, "Contacts permission is required to select contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Show contact selection dialog to choose contacts to add as travelers
     */
    private void showContactsSelection() {
        if (currentTripId == -1) {
            Toast.makeText(this, "Please select a trip first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if we have contacts permission
        if (!PermissionsUtility.hasContactsPermission(this)) {
            // Request permission
            PermissionsUtility.requestContactsPermission(this);
            return;
        }

        // We have permission, show contacts
        showContactsSelectionDialog();
    }

    /**
     * Show the contacts selection dialog
     */
    private void showContactsSelectionDialog() {
        // Get contacts from the device
        List<String> contacts = ContactsUtility.getContacts(this);

        if (contacts.isEmpty()) {
            Toast.makeText(this, "No contacts found on device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert to array for AlertDialog
        final String[] contactsArray = contacts.toArray(new String[0]);

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Contact");

        builder.setItems(contactsArray, (dialog, which) -> {
            String selectedContact = contactsArray[which];

            // Add the contact as a traveler using the content provider
            boolean success = ContactsUtility.addContactAsTraveler(this, selectedContact, currentTripId);

            if (success) {
                Toast.makeText(this, selectedContact + " added from contacts!", Toast.LENGTH_SHORT).show();
                updatePeopleList();
            } else {
                // Fallback to direct database access if content provider fails
                PeopleModel newPerson = new PeopleModel(selectedContact, currentTripId);
                long personId = peopleDAO.addPerson(newPerson);

                if (personId > 0) {
                    Toast.makeText(this, selectedContact + " added to the trip!", Toast.LENGTH_SHORT).show();
                    updatePeopleList();
                } else {
                    Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
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
        displayNames.clear();
        positionToPersonIdMap.clear();

        if (currentTripId == -1) {
            peopleAdapter.notifyDataSetChanged();
            return;
        }

        // Try to get people using the content provider
        List<String> travelers = ContactsUtility.getTravelersForTrip(this, currentTripId);

        // If content provider fails or returns no results, fall back to direct database access
        if (travelers.isEmpty()) {
            currentPeopleList = peopleDAO.getPeopleByTripId(currentTripId);

            if (currentPeopleList.isEmpty()) {
                displayNames.add("No people added to this trip. Tap 'Add' to add someone.");
            } else {
                for (int i = 0; i < currentPeopleList.size(); i++) {
                    PeopleModel person = currentPeopleList.get(i);
                    displayNames.add(person.getName());
                    positionToPersonIdMap.put(i, person.getId());
                }
            }
        } else {
            displayNames.addAll(travelers);
        }

        peopleAdapter.notifyDataSetChanged();
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

        // Try to add person using content provider first
        boolean success = ContactsUtility.addContactAsTraveler(this, personName, currentTripId);

        if (success) {
            Toast.makeText(this, personName + " added to the trip!", Toast.LENGTH_SHORT).show();
            personNameInput.setText("");
            updatePeopleList();  // Refresh the list display
        } else {
            // Fallback to direct database access
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
    }

    private void showRemovePersonDialog(String personName, int position) {
        // Don't show dialog for the placeholder text
        if (currentPeopleList == null || currentPeopleList.isEmpty()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove Person")
                .setMessage("Do you want to remove " + personName + " from this trip?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removePerson(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removePerson(int position) {
        Long personId = positionToPersonIdMap.get(position);
        if (personId != null) {
            boolean success = peopleDAO.deletePerson(personId);
            if (success) {
                Toast.makeText(this, "Person removed from trip", Toast.LENGTH_SHORT).show();
                updatePeopleList();
            } else {
                Toast.makeText(this, "Failed to remove person", Toast.LENGTH_SHORT).show();
            }
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