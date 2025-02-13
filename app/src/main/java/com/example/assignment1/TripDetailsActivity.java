package com.example.assignment1;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        sharedPreferences = getSharedPreferences("TripData", Context.MODE_PRIVATE);

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

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("tripName", tripName);
        editor.putString("destination", destination);
        editor.putString("budget", budget);
        editor.putString("departureDate", departureDate);
        editor.putString("returnDate", returnDate);
        editor.apply();

        Toast.makeText(this, "Trip saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadSavedTrip() {
        String tripName = sharedPreferences.getString("tripName", "");
        String destination = sharedPreferences.getString("destination", "");
        String budget = sharedPreferences.getString("budget", "");
        String departureDate = sharedPreferences.getString("departureDate", "");
        String returnDate = sharedPreferences.getString("returnDate", "");

        tripNameInput.setText(tripName);
        destinationInput.setText(destination);
        budgetInput.setText(budget);
        departureDateInput.setText(departureDate);
        returnDateInput.setText(returnDate);
    }
}
