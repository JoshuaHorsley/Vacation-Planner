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

        tripDetailsText = findViewById(R.id.tripDetailsText);
        personNameInput = findViewById(R.id.personNameInput);
        peopleListView = findViewById(R.id.peopleListView);
        addPersonButton = findViewById(R.id.addPersonButton);
        saveButton = findViewById(R.id.saveButton);
        goBackButton = findViewById(R.id.goBackButton);
        tripSpinner = findViewById(R.id.tripSpinner);

        addFromContactsButton = findViewById(R.id.addFromContactsButton);

        displayNames = new ArrayList<>();
        positionToPersonIdMap = new HashMap<>();

        peopleDAO = new PeopleDAO(this);
        tripDAO = new TripDAO(this);

        // Open database connections
        peopleDAO.open();
        tripDAO.open();

        Intent intent = getIntent();
        long tripId = intent.getLongExtra("tripId", -1);
        if (tripId != -1) {
            currentTripId = tripId;
        }

        peopleAdapter = new ArrayAdapter<>(
                this,
                R.layout.trip_list_item,
                android.R.id.text1,
                displayNames
        );
        peopleListView.setAdapter(peopleAdapter);

        peopleListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < displayNames.size()) {
                showRemovePersonDialog(displayNames.get(position), position);
            }
        });

        loadTripsIntoSpinner();

        tripSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedTripName = parent.getItemAtPosition(position).toString();
                    currentTripId = tripNameToIdMap.get(selectedTripName);
                    loadTripDetails();
                    updatePeopleList();

                    personNameInput.setEnabled(true);
                    addPersonButton.setEnabled(true);
                    addFromContactsButton.setEnabled(true);
                } else {
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


    private void showContactsSelectionDialog() {
        List<String> contacts = ContactsUtility.getContacts(this);

        if (contacts.isEmpty()) {
            Toast.makeText(this, "No contacts found on device", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] contactsArray = contacts.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Contact");

        builder.setItems(contactsArray, (dialog, which) -> {
            String selectedContact = contactsArray[which];

            boolean success = ContactsUtility.addContactAsTraveler(this, selectedContact, currentTripId);

            if (success) {
                Toast.makeText(this, selectedContact + " added from contacts!", Toast.LENGTH_SHORT).show();
                updatePeopleList();
            } else {
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
        List<TripModel> tripList = tripDAO.getAllTrips();
        tripNameToIdMap = new HashMap<>();

        List<String> tripNames = new ArrayList<>();
        tripNames.add("Select a trip");

        for (TripModel trip : tripList) {
            String tripName = trip.getTripName();
            tripNames.add(tripName);
            tripNameToIdMap.put(tripName, trip.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tripNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        tripSpinner.setAdapter(adapter);

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

        List<String> travelers = ContactsUtility.getTravelersForTrip(this, currentTripId);


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
            updatePeopleList();
        } else {
            PeopleModel newPerson = new PeopleModel(personName, currentTripId);
            long personId = peopleDAO.addPerson(newPerson);

            if (personId > 0) {
                Toast.makeText(this, personName + " added to the trip!", Toast.LENGTH_SHORT).show();
                personNameInput.setText("");
                updatePeopleList();
            } else {
                Toast.makeText(this, "Failed to add person", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showRemovePersonDialog(String personName, int position) {
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

        if (!peopleDAO.isOpen()) {
            peopleDAO.open();
        }
        if (!tripDAO.isOpen()) {
            tripDAO.open();
        }

        loadTripsIntoSpinner();
        if (currentTripId != -1) {
            updatePeopleList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (peopleDAO != null) {
            peopleDAO.close();
        }
        if (tripDAO != null) {
            tripDAO.close();
        }
    }
}