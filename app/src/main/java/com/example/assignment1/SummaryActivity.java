package com.example.assignment1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import java.util.Map;

public class SummaryActivity extends ComponentActivity {
    private static final String CASHED_OUT_TRIPS = "CashedOutTrips";
    private TextView summaryTextView;
    private Button goBackButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        summaryTextView = findViewById(R.id.summaryTextView);
        goBackButton = findViewById(R.id.goBackButton);

        loadCashedOutTrips();

        goBackButton.setOnClickListener(v -> finish());
    }

    private void loadCashedOutTrips() {
        SharedPreferences sharedPreferences = getSharedPreferences(CASHED_OUT_TRIPS, Context.MODE_PRIVATE);
        Map<String, ?> allTrips = sharedPreferences.getAll();

        if (allTrips.isEmpty()) {
            summaryTextView.setText("No cashed-out trips available.");
            return;
        }

        StringBuilder summaryBuilder = new StringBuilder();
        for (Map.Entry<String, ?> entry : allTrips.entrySet()) {
            summaryBuilder.append(entry.getValue().toString()).append("\n\n");
        }

        summaryTextView.setText(summaryBuilder.toString());
    }
}
