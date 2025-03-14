package com.example.assignment1;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TripDetailsActivity extends ComponentActivity {
    private EditText tripNameInput, destinationInput, budgetInput, departureDateInput, returnDateInput;
    private Button saveTripButton, goBackButton, newTripButton;
    private Spinner tripSpinner;

    private TripDAO tripDAO;
    private TripModel currentTrip;
    private Map<String, Long> tripNameToIdMap;
    private boolean isEditMode = false;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1001;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        tripDAO = new TripDAO(this);
        tripDAO.open();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "REMOVED_MAPS_API_KEY");
        }
        PlacesClient placesClient = Places.createClient(this);

        // Initialize views
        tripSpinner = findViewById(R.id.tripSpinner);
        tripNameInput = findViewById(R.id.tripNameInput);
        destinationInput = findViewById(R.id.destinationInput);
        budgetInput = findViewById(R.id.budgetInput);
        departureDateInput = findViewById(R.id.departureDateInput);
        returnDateInput = findViewById(R.id.returnDateInput);
        saveTripButton = findViewById(R.id.saveTripButton);
        goBackButton = findViewById(R.id.goBackButton);
        newTripButton = findViewById(R.id.newTripButton);

        // Set up date pickers
        departureDateInput.setOnClickListener(v -> showDatePicker(departureDateInput));
        returnDateInput.setOnClickListener(v -> showDatePicker(returnDateInput));

        destinationInput.setOnClickListener(v -> openAutocomplete());

        // Load trips into the spinner
        loadTripsIntoSpinner();

        // New Trip button to clear form
        newTripButton.setOnClickListener(v -> {
            clearForm();
            isEditMode = false;
            saveTripButton.setText("Save Trip");
            tripSpinner.setSelection(0); // Select "Create New Trip"
        });

        // Set up button click listeners
        saveTripButton.setOnClickListener(v -> saveTrip());
        goBackButton.setOnClickListener(v -> finish());

        // Process intent for trip editing
        Intent intent = getIntent();
        long tripId = intent.getLongExtra("tripId", -1);
        if (tripId != -1) {
            loadTripForEditing(tripId);
        }
    }

    private void loadTripsIntoSpinner() {
        // Get all trips from the database
        List<TripModel> tripList = tripDAO.getAllTrips();
        tripNameToIdMap = new HashMap<>();

        // Create a list for the spinner with a prompt as the first item
        List<String> tripNames = new ArrayList<>();
        tripNames.add("Create New Trip");

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

        // Set up spinner selection listener
        tripSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // "Create New Trip" selected
                    clearForm();
                    isEditMode = false;
                    saveTripButton.setText("Save Trip");
                } else {
                    // Existing trip selected
                    String selectedTripName = parent.getItemAtPosition(position).toString();
                    long tripId = tripNameToIdMap.get(selectedTripName);
                    loadTripForEditing(tripId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    private void openAutocomplete() {
        // Define the fields to request
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        // Create the intent
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                destinationInput.setText(place.getName()); // Set the selected place
            } else if (resultCode == RESULT_CANCELED) {
                // User canceled the operation
                Toast.makeText(this, "Autocomplete canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePicker(final EditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
            dateInput.setText(selectedDate);
            validateDates();
        }, year, month, day);

        datePickerDialog.show();
    }

    private void validateDates() {
        String departureDateStr = departureDateInput.getText().toString();
        String returnDateStr = returnDateInput.getText().toString();

        if (!departureDateStr.isEmpty() && !returnDateStr.isEmpty()) {
            String[] depParts = departureDateStr.split("-");
            String[] retParts = returnDateStr.split("-");

            Calendar departureCal = Calendar.getInstance();
            departureCal.set(Integer.parseInt(depParts[0]), Integer.parseInt(depParts[1]) - 1, Integer.parseInt(depParts[2]));

            Calendar returnCal = Calendar.getInstance();
            returnCal.set(Integer.parseInt(retParts[0]), Integer.parseInt(retParts[1]) - 1, Integer.parseInt(retParts[2]));

            if (departureCal.after(returnCal)) {
                Toast.makeText(this, "Departure date must be before return date", Toast.LENGTH_SHORT).show();
                returnDateInput.setText("");
            }
        }
    }

    private void loadTripForEditing(long tripId) {
        currentTrip = tripDAO.getTripById((int) tripId);
        if (currentTrip != null) {
            tripNameInput.setText(currentTrip.getTripName());
            destinationInput.setText(currentTrip.getDestination());
            budgetInput.setText(currentTrip.getBudget());
            departureDateInput.setText(currentTrip.getDepartureDate());
            returnDateInput.setText(currentTrip.getReturnDate());

            isEditMode = true;
            saveTripButton.setText("Update Trip");

            // Pre-select this trip in the spinner
            for (int i = 1; i < tripSpinner.getAdapter().getCount(); i++) {
                String tripName = tripSpinner.getAdapter().getItem(i).toString();
                if (tripNameToIdMap.get(tripName) == tripId) {
                    tripSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void clearForm() {
        tripNameInput.setText("");
        destinationInput.setText("");
        budgetInput.setText("");
        departureDateInput.setText("");
        returnDateInput.setText("");
        currentTrip = null;
    }

    private void saveTrip() {
        String tripName = tripNameInput.getText().toString();
        String destination = destinationInput.getText().toString();
        String budget = budgetInput.getText().toString();
        String departureDate = departureDateInput.getText().toString();
        String returnDate = returnDateInput.getText().toString();

        if (tripName.isEmpty() || destination.isEmpty() || budget.isEmpty() || departureDate.isEmpty() || returnDate.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        TripModel trip = new TripModel(tripName, destination, budget, departureDate, returnDate);

        // Make sure the database is open
        if (!tripDAO.isOpen()) {
            tripDAO.open();
        }

        if (isEditMode && currentTrip != null) {
            trip.setId(currentTrip.getId());
            tripDAO.updateTrip(trip);
            Toast.makeText(this, "Trip updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            long newTripId = tripDAO.addTrip(trip);
            if (newTripId > 0) {
                Toast.makeText(this, "Trip saved successfully", Toast.LENGTH_SHORT).show();
                trip.setId(newTripId);
                currentTrip = trip;
                isEditMode = true;
                saveTripButton.setText("Update Trip");

                // Refresh spinner with the new trip
                loadTripsIntoSpinner();

                // Select the newly created trip
                for (int i = 1; i < tripSpinner.getAdapter().getCount(); i++) {
                    String name = tripSpinner.getAdapter().getItem(i).toString();
                    if (name.equals(tripName)) {
                        tripSpinner.setSelection(i);
                        break;
                    }
                }
            } else {
                Toast.makeText(this, "Failed to save trip", Toast.LENGTH_SHORT).show();
            }
        }

        // Set the result to indicate success
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tripDAO != null && !tripDAO.isOpen()) {
            tripDAO.open();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tripDAO != null) {
            tripDAO.close();
        }
    }
}