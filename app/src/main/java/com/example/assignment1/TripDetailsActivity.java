package com.example.assignment1;

import android.app.DatePickerDialog;
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        tripNameInput = findViewById(R.id.tripNameInput);
        destinationInput = findViewById(R.id.destinationInput);
        budgetInput = findViewById(R.id.budgetInput);
        departureDateInput = findViewById(R.id.departureDateInput);
        returnDateInput = findViewById(R.id.returnDateInput);
        Button saveTripButton = findViewById(R.id.saveTripButton);
        Button goBackButton = findViewById(R.id.goBackButton);

        String msgFromMain = getIntent().getStringExtra("message");
        Toast.makeText(this, msgFromMain, Toast.LENGTH_LONG).show();

        // Handle departure date selection
        departureDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(departureDateInput);
            }
        });

        // Handle return date selection
        returnDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(returnDateInput);
            }
        });

        saveTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tripName = tripNameInput.getText().toString();
                String destination = destinationInput.getText().toString();
                String budget = budgetInput.getText().toString();
                String departureDate = departureDateInput.getText().toString();
                String returnDate = returnDateInput.getText().toString();

                if (tripName.isEmpty() || destination.isEmpty() || budget.isEmpty() || departureDate.isEmpty() || returnDate.isEmpty()) {
                    Toast.makeText(TripDetailsActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent sendBack = new Intent();
                sendBack.putExtra("returned_data", "Trip to " + destination +
                        " with a budget of $" + budget +
                        ", departing on " + departureDate +
                        " and returning on " + returnDate);
                setResult(RESULT_OK, sendBack);
                finish();
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Date Picker Dialog
    private void showDatePicker(final EditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    dateInput.setText(selectedDate);

                    // Validate departure and return dates
                    if (dateInput == departureDateInput && !returnDateInput.getText().toString().isEmpty()) {
                        Calendar departureCal = Calendar.getInstance();
                        departureCal.set(selectedYear, selectedMonth, selectedDay);

                        Calendar returnCal = Calendar.getInstance();
                        String[] returnDateParts = returnDateInput.getText().toString().split("-");
                        returnCal.set(Integer.parseInt(returnDateParts[0]), Integer.parseInt(returnDateParts[1]) - 1, Integer.parseInt(returnDateParts[2]));

                        if (departureCal.after(returnCal)) {
                            Toast.makeText(this, "Departure date must be before return date", Toast.LENGTH_SHORT).show();
                            dateInput.setText("");
                        }
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }

}