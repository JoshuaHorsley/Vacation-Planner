package com.example.assignment1.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import com.example.assignment1.R;
import com.example.assignment1.database.TripDAO;
import com.example.assignment1.model.TripModel;
import com.example.assignment1.utils.WidgetUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import android.app.AlarmManager;
import android.app.PendingIntent;
import com.example.assignment1.receivers.TripReminderReceiver;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TripDetailsActivity extends ComponentActivity implements OnMapReadyCallback {
    private EditText tripNameInput, budgetInput, departureDateInput, returnDateInput;
    private TextView destinationDisplay;
    private Button destinationButton, saveTripButton, goBackButton, newTripButton, weatherButton;
    private Spinner tripSpinner;
    private FrameLayout mapContainer;
    private MapView mapView;
    private GoogleMap googleMap;

    private TripDAO tripDAO;
    private TripModel currentTrip;
    private Map<String, Long> tripNameToIdMap;
    private boolean isEditMode = false;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1001;
    private String selectedPlaceId;
    private String selectedPlaceName;
    private LatLng selectedPlaceLatLng;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Initialize MapView with the saved instance state
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        tripDAO = new TripDAO(this);
        tripDAO.open();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "REMOVED_MAPS_API_KEY");
        }
        PlacesClient placesClient = Places.createClient(this);

        // Initialize views
        tripSpinner = findViewById(R.id.tripSpinner);
        tripNameInput = findViewById(R.id.tripNameInput);
        destinationDisplay = findViewById(R.id.destinationDisplay);
        destinationButton = findViewById(R.id.destinationButton);
        budgetInput = findViewById(R.id.budgetInput);
        departureDateInput = findViewById(R.id.departureDateInput);
        returnDateInput = findViewById(R.id.returnDateInput);
        saveTripButton = findViewById(R.id.saveTripButton);
        goBackButton = findViewById(R.id.goBackButton);
        newTripButton = findViewById(R.id.newTripButton);
        mapContainer = findViewById(R.id.mapContainer);
        mapView = findViewById(R.id.mapView);

        // Initialize MapView
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        departureDateInput.setOnClickListener(v -> showDatePicker(departureDateInput));
        returnDateInput.setOnClickListener(v -> showDatePicker(returnDateInput));

        destinationButton.setOnClickListener(v -> openAutocomplete());

        loadTripsIntoSpinner();

        newTripButton.setOnClickListener(v -> {
            clearForm();
            isEditMode = false;
            saveTripButton.setText("Save Trip");
            tripSpinner.setSelection(0);
        });

        saveTripButton.setOnClickListener(v -> saveTrip());
        goBackButton.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        long tripId = intent.getLongExtra("tripId", -1);
        if (tripId != -1) {
            loadTripForEditing(tripId);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Set default map settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // If a location is already selected, update the map
        if (selectedPlaceLatLng != null) {
            updateMapLocation(selectedPlaceLatLng, selectedPlaceName);
        }
    }

    private void updateMapLocation(LatLng location, String title) {
        if (googleMap != null) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location).title(title));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f));

            // Show the map container
            mapContainer.setVisibility(View.VISIBLE);
        }
    }

    private void loadTripsIntoSpinner() {
        List<TripModel> tripList = tripDAO.getAllTrips();
        tripNameToIdMap = new HashMap<>();

        List<String> tripNames = new ArrayList<>();
        tripNames.add("Create New Trip");

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

        tripSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    clearForm();
                    isEditMode = false;
                    saveTripButton.setText("Save Trip");
                } else {
                    String selectedTripName = parent.getItemAtPosition(position).toString();
                    long tripId = tripNameToIdMap.get(selectedTripName);
                    loadTripForEditing(tripId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void openAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
        );

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
                selectedPlaceId = place.getId();
                selectedPlaceName = place.getName();
                selectedPlaceLatLng = place.getLatLng();

                destinationDisplay.setText(selectedPlaceName);

                if (destinationDisplay.getVisibility() == View.GONE) {
                    destinationDisplay.setVisibility(View.VISIBLE);
                }

                destinationButton.setText("Change Destination");

                // Update map with selected location
                if (selectedPlaceLatLng != null) {
                    updateMapLocation(selectedPlaceLatLng, selectedPlaceName);
                }

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Destination selection canceled", Toast.LENGTH_SHORT).show();
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

            String destination = currentTrip.getDestination();
            if (destination != null && !destination.isEmpty()) {
                destinationDisplay.setText(destination);
                destinationDisplay.setVisibility(View.VISIBLE);
                destinationButton.setText("Change Destination");
                selectedPlaceName = destination;

                // Hide map if we're loading a trip that doesn't have coordinates
                mapContainer.setVisibility(View.GONE);
            } else {
                destinationDisplay.setVisibility(View.GONE);
                destinationButton.setText("Select Destination");
                mapContainer.setVisibility(View.GONE);
            }

            budgetInput.setText(currentTrip.getBudget());
            departureDateInput.setText(currentTrip.getDepartureDate());
            returnDateInput.setText(currentTrip.getReturnDate());

            isEditMode = true;
            saveTripButton.setText("Update Trip");

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
        destinationDisplay.setText("");
        destinationDisplay.setVisibility(View.GONE);
        destinationButton.setText("Select Destination");
        selectedPlaceId = null;
        selectedPlaceName = null;
        selectedPlaceLatLng = null;
        budgetInput.setText("");
        departureDateInput.setText("");
        returnDateInput.setText("");
        currentTrip = null;

        // Hide map container when form is cleared
        mapContainer.setVisibility(View.GONE);
    }

    private void saveTrip() {
        String tripName = tripNameInput.getText().toString();
        String destination = selectedPlaceName;
        String budget = budgetInput.getText().toString();
        String departureDate = departureDateInput.getText().toString();
        String returnDate = returnDateInput.getText().toString();

        if (tripName.isEmpty() || destination == null || destination.isEmpty() || budget.isEmpty()
                || departureDate.isEmpty() || returnDate.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        TripModel trip = new TripModel(tripName, destination, budget, departureDate, returnDate);

        if (selectedPlaceLatLng != null) {
            trip.setLatitude(selectedPlaceLatLng.latitude);
            trip.setLongitude(selectedPlaceLatLng.longitude);
        }

        if (!tripDAO.isOpen()) {
            tripDAO.open();
        }
        if (isEditMode && currentTrip != null) {
            trip.setId(currentTrip.getId());
            tripDAO.updateTrip(trip);
            Toast.makeText(this, "Trip updated successfully", Toast.LENGTH_SHORT).show();
            scheduleTripReminder(tripName, departureDate);
        } else {
            long newTripId = tripDAO.addTrip(trip);
            if (newTripId > 0) {
                Toast.makeText(this, "Trip saved successfully", Toast.LENGTH_SHORT).show();
                scheduleTripReminder(tripName, departureDate);
                WidgetUtils.updateWidgets(TripDetailsActivity.this);

                trip.setId(newTripId);
                currentTrip = trip;
                isEditMode = true;
                saveTripButton.setText("Update Trip");

                loadTripsIntoSpinner();

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

        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    private void scheduleTripReminder(String tripName, String departureDate) {
        String[] parts = departureDate.split("-");
        Calendar departure = Calendar.getInstance();
        departure.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));

        departure.add(Calendar.DAY_OF_MONTH, -1);

        //DEMONSTRATION:
        //long reminderTime = System.currentTimeMillis() + 10 * 1000; // Demo: 10 seconds from now
        //ACTUAL CODE:
        long reminderTime = departure.getTimeInMillis();

        if (reminderTime < System.currentTimeMillis()) return;

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                Toast.makeText(this, "Please allow exact alarms to receive reminders.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Intent intent = new Intent(this, TripReminderReceiver.class);
        intent.putExtra("tripName", tripName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, tripName.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (tripDAO != null && !tripDAO.isOpen()) {
            tripDAO.open();
        }
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tripDAO != null) {
            tripDAO.close();
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}