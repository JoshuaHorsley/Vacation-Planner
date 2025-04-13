package com.example.assignment1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.example.assignment1.R;
import com.example.assignment1.database.TripDAO;
import com.example.assignment1.model.TripModel;

import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends ComponentActivity {
    private ListView tripsListView;
    private TextView noTripsTextView;
    private Button goBackButton;
    private TripDAO tripDAO;
    private List<TripModel> tripList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        tripsListView = findViewById(R.id.tripsListView);
        noTripsTextView = findViewById(R.id.noTripsTextView);
        goBackButton = findViewById(R.id.goBackButton);

        tripDAO = new TripDAO(this);
        tripDAO.open();

        loadTripsList();

        tripsListView.setOnItemClickListener((parent, view, position, id) -> {
            TripModel selectedTrip = tripList.get(position);
            showTripDetails(selectedTrip);
        });

        goBackButton.setOnClickListener(v -> finish());
    }

    private void loadTripsList() {
        tripList = tripDAO.getAllTrips();

        if (tripList.isEmpty()) {
            // No trips found
            tripsListView.setVisibility(View.GONE);
            noTripsTextView.setVisibility(View.VISIBLE);
            noTripsTextView.setText("No trips available. Create a trip first!");
        } else {
            // Display trips in the list view
            tripsListView.setVisibility(View.VISIBLE);
            noTripsTextView.setVisibility(View.GONE);

            List<String> tripNames = new ArrayList<>();
            for (TripModel trip : tripList) {
                tripNames.add(trip.getTripName() + " - " + trip.getDestination());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    R.layout.trip_list_item,
                    android.R.id.text1,
                    tripNames
            );
            tripsListView.setAdapter(adapter);
        }
    }

    private void showTripDetails(TripModel trip) {
        Intent intent = new Intent(SummaryActivity.this, TripDetailViewActivity.class);
        intent.putExtra("tripId", trip.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!tripDAO.isOpen()) {
            tripDAO.open();
        }
        loadTripsList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tripDAO != null) {
            tripDAO.close();
        }
    }
}