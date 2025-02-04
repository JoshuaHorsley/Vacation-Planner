package com.example.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

public class TripDetailsActivity extends ComponentActivity {
    private EditText destinationInput, budgetInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        destinationInput = findViewById(R.id.destinationInput);
        budgetInput = findViewById(R.id.budgetInput);
        Button saveTripButton = findViewById(R.id.saveTripButton);
        Button goBackButton = findViewById(R.id.goBackButton);

        String msgFromMain = getIntent().getStringExtra("message");
        Toast.makeText(this, msgFromMain, Toast.LENGTH_LONG).show();

        saveTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destination = destinationInput.getText().toString();
                String budget = budgetInput.getText().toString();

                if (destination.isEmpty() || budget.isEmpty()){
                    Toast.makeText(TripDetailsActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent sendBack = new Intent();
                sendBack.putExtra("returned_data", "Trip to " + destination + " with a budget of $" + budget);
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
}
