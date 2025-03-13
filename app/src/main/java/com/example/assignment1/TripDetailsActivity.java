package com.example.assignment1;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

public class TripDetailsActivity extends ComponentActivity {
    private EditText tripNameInput, destinationInput, budgetInput, departureDateInput, returnDateInput;
    private TripDAO tripDAO;
    private TripModel currentTrip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        tripDAO = new TripDAO(this);
        tripDAO.open(); // Open the database when activity is created

        tripNameInput = findViewById(R.id.tripNameInput);
        destinationInput = findViewById(R.id.destinationInput);
        budgetInput = findViewById(R.id.budgetInput);
        departureDateInput = findViewById(R.id.departureDateInput);
        returnDateInput = findViewById(R.id.returnDateInput);
        Button saveTripButton = findViewById(R.id.saveTripButton);
        Button goBackButton = findViewById(R.id.goBackButton);

        loadSavedTrip();

        departureDateInput.setOnClickListener(v -> showDatePicker(departureDateInput));
        returnDateInput.setOnClickListener(v -> showDatePicker(returnDateInput));

        saveTripButton.setOnClickListener(v -> saveTrip());
        goBackButton.setOnClickListener(v -> finish());
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

        // Make sure the database is open before operations
        if (!tripDAO.isOpen()) {
            tripDAO.open();
        }

        if (currentTrip != null) {
            trip.setId(currentTrip.getId());
            tripDAO.updateTrip(trip);
            Toast.makeText(this, "Trip updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            long newTripId = tripDAO.addTrip(trip);
            if (newTripId > 0) {
                Toast.makeText(this, "Trip saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save trip", Toast.LENGTH_SHORT).show();
            }
        }

        // Set the result to indicate success and pass back any needed data
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);

        finish();
    }

    private void loadSavedTrip() {
        Intent intent = getIntent();
        int tripId = intent.getIntExtra("tripId", -1);

        if (tripId != -1) {
            currentTrip = tripDAO.getTripById(tripId);
            if (currentTrip != null) {
                tripNameInput.setText(currentTrip.getTripName());
                destinationInput.setText(currentTrip.getDestination());
                budgetInput.setText(currentTrip.getBudget());
                departureDateInput.setText(currentTrip.getDepartureDate());
                returnDateInput.setText(currentTrip.getReturnDate());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database when activity is destroyed
        if (tripDAO != null) {
            tripDAO.close();
        }
    }
}