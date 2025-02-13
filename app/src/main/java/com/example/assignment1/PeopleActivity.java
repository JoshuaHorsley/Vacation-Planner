package com.example.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.widget.TextView;


import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

public class PeopleActivity extends ComponentActivity {
    Button goBack = null;
    private static final String PLANNED_TRIPS = "TripPrefs";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_activity);
        goBack = findViewById(R.id.goBack3);

        TextView summaryTextView = findViewById(R.id.summaryTextView);

        SharedPreferences sharedPreferences = getSharedPreferences(PLANNED_TRIPS, MODE_PRIVATE);
        String tripSummary = sharedPreferences.getString("trip_summary", "No trip details available.");

        summaryTextView.setText(tripSummary);


        String msgFromMain = getIntent().getStringExtra("data_from_main_to_3");
        if (msgFromMain != null && !msgFromMain.isEmpty()) {
            Toast.makeText(this, msgFromMain, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No message received", Toast.LENGTH_SHORT).show();
        }


        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendBack = new Intent();
                sendBack.putExtra("data_from_3_to_main", "hello from activity 3");
                setResult(ResultCodes.RESULT_FROM_ACTIVITY_3, sendBack);
                finish();
            }
        });
    }
}
