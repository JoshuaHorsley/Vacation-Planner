package com.example.assignment1.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.example.assignment1.utils.FileUtils;
import com.example.assignment1.receivers.NetworkChangeReceiver;
import com.example.assignment1.R;
import com.example.assignment1.database.PeopleDAO;
import com.example.assignment1.database.TripDAO;
import com.example.assignment1.model.PeopleModel;
import com.example.assignment1.model.TripModel;
import com.example.assignment1.utils.WidgetUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TripDetailViewActivity extends ComponentActivity implements OnMapReadyCallback, NetworkChangeReceiver.NetworkChangeListener {
    private static final String TAG = "TripDetailViewActivity";
    private TextView tripDetailsText, peopleListText;
    private Button editTripButton, addTravelersButton, saveToFileButton, deleteTripButton, goBackButton, weatherButton;

    private FrameLayout mapContainer;
    private MapView mapView;
    private GoogleMap googleMap;

    private LinearLayout weatherWidgetContainer;
    private ProgressBar weatherProgressBar;
    private TextView weatherStatusText, weatherResultsText;

    private TripDAO tripDAO;
    private PeopleDAO peopleDAO;
    private long tripId;
    private TripModel currentTrip;

    private NetworkChangeReceiver networkChangeReceiver;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail_view);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        tripDetailsText = findViewById(R.id.tripDetailsText);
        peopleListText = findViewById(R.id.peopleListText);
        editTripButton = findViewById(R.id.editTripButton);
        addTravelersButton = findViewById(R.id.addTravelersButton);
        saveToFileButton = findViewById(R.id.saveToFileButton);
        deleteTripButton = findViewById(R.id.deleteTripButton);
        goBackButton = findViewById(R.id.goBackButton);
        weatherButton = findViewById(R.id.weatherButton);

        mapContainer = findViewById(R.id.mapContainer);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        weatherWidgetContainer = findViewById(R.id.weatherWidgetContainer);
        weatherProgressBar = findViewById(R.id.weatherProgressBar);
        weatherStatusText = findViewById(R.id.weatherStatusText);
        weatherResultsText = findViewById(R.id.weatherResultsText);

        networkChangeReceiver = new NetworkChangeReceiver(this);

        tripDAO = new TripDAO(this);
        peopleDAO = new PeopleDAO(this);

        tripDAO.open();
        peopleDAO.open();

        Intent intent = getIntent();
        tripId = intent.getLongExtra("tripId", -1);

        if (tripId == -1) {
            Toast.makeText(this, "Error: No trip selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTripDetails();
        loadPeopleList();

        editTripButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(TripDetailViewActivity.this, TripDetailsActivity.class);
            editIntent.putExtra("tripId", tripId);
            startActivity(editIntent);
        });

        addTravelersButton.setOnClickListener(v -> {
            Intent peopleIntent = new Intent(TripDetailViewActivity.this, PeopleActivity.class);
            peopleIntent.putExtra("tripId", tripId);
            startActivity(peopleIntent);
        });

        saveToFileButton.setOnClickListener(v -> {
            final EditText input = new EditText(this);
            input.setHint("Enter file name (optional)");

            new AlertDialog.Builder(this)
                    .setTitle("Save Trip Details")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String fileName = input.getText().toString().trim();

                        // Generate the content
                        String tripContent = tripDetailsText.getText().toString();
                        String peopleContent = peopleListText.getText().toString();
                        String weatherContent = weatherResultsText.getText().toString();

                        StringBuilder fullContent = new StringBuilder();
                        fullContent.append(tripContent).append("\n\n");

                        // Add weather information if available
                        if (!weatherContent.isEmpty()) {
                            fullContent.append("Weather Information:\n").append(weatherContent).append("\n\n");
                        }

                        fullContent.append(peopleContent);

                        // Save to file
                        FileUtils.saveTripDetailsToFile(this, fullContent.toString(), fileName);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        weatherButton.setOnClickListener(v -> {
            if (NetworkChangeReceiver.isNetworkConnected(this)) {
                if (currentTrip != null && currentTrip.getDestination() != null && !currentTrip.getDestination().trim().isEmpty()) {
                    // Show the weather widget container
                    weatherWidgetContainer.setVisibility(View.VISIBLE);

                    // Fetch weather data
                    new WeatherDownloadTask().execute(currentTrip.getDestination());
                } else {
                    Toast.makeText(TripDetailViewActivity.this,
                            "No destination set for this trip", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(TripDetailViewActivity.this,
                        "Cannot fetch weather: No network connection", Toast.LENGTH_LONG).show();

                // Show widget container with network error message
                weatherWidgetContainer.setVisibility(View.VISIBLE);
                weatherStatusText.setVisibility(View.VISIBLE);
                weatherProgressBar.setVisibility(View.GONE);
                weatherStatusText.setText("Network error: Please check your internet connection");
            }
        });

        deleteTripButton.setOnClickListener(v -> {
            showDeleteTripConfirmation();
        });

        goBackButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Set default map settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // If trip has coordinates, show the map
        if (currentTrip != null && currentTrip.getLatitude() != 0 && currentTrip.getLongitude() != 0) {
            showTripLocationOnMap();
        } else {
            // Hide the map container if no valid coordinates
            mapContainer.setVisibility(View.GONE);
        }
    }

    private void showTripLocationOnMap() {
        if (googleMap != null && currentTrip != null) {
            // Create LatLng from the trip coordinates
            LatLng location = new LatLng(currentTrip.getLatitude(), currentTrip.getLongitude());

            // Clear any existing markers
            googleMap.clear();

            // Add marker for the trip destination
            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(currentTrip.getDestination()));

            // Move camera to the location with zoom
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f));

            // Make the map visible
            mapContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        // Enable or disable the weather button based on network availability
        if (weatherButton != null) {
            if (!isConnected) {
                Log.d(TAG, "Network disconnected, updating UI");

                if (weatherWidgetContainer.getVisibility() == View.VISIBLE) {
                    weatherStatusText.setVisibility(View.VISIBLE);
                    weatherStatusText.setText("Network disconnected: Weather updates paused");
                }
            } else {
                Log.d(TAG, "Network connected, updating UI");

                if (weatherWidgetContainer.getVisibility() == View.VISIBLE &&
                        weatherStatusText.getText().toString().contains("Network")) {
                    // If we previously showed a network error, offer to retry
                    weatherStatusText.setText("Network connection restored. Tap 'Check Weather' to retry.");
                }
            }
        }
    }


    private void showDeleteTripConfirmation() {
        if (currentTrip == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Trip")
                .setMessage("Are you sure you want to delete the trip \"" + currentTrip.getTripName() + "\"? " +
                        "This will delete all travelers associated with this trip and cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTrip();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void deleteTrip() {
        List<PeopleModel> peopleList = peopleDAO.getPeopleByTripId(tripId);
        for (PeopleModel person : peopleList) {
            peopleDAO.deletePerson(person.getId());
        }

        tripDAO.deleteTrip(tripId);

        Toast.makeText(this, "Trip deleted successfully", Toast.LENGTH_SHORT).show();
        WidgetUtils.updateWidgets(TripDetailViewActivity.this);

        finish();
    }

    private void loadTripDetails() {
        currentTrip = tripDAO.getTripById((int)tripId);

        if (currentTrip != null) {
            StringBuilder details = new StringBuilder();
            details.append("Trip: ").append(currentTrip.getTripName()).append("\n\n");
            details.append("Destination: ").append(currentTrip.getDestination()).append("\n\n");
            details.append("Budget: $").append(currentTrip.getBudget()).append("\n\n");
            details.append("Departure Date: ").append(currentTrip.getDepartureDate()).append("\n\n");
            details.append("Return Date: ").append(currentTrip.getReturnDate());

            tripDetailsText.setText(details.toString());

            // Show map if coordinates are available
            if (googleMap != null && currentTrip.getLatitude() != 0 && currentTrip.getLongitude() != 0) {
                showTripLocationOnMap();
            }
        } else {
            tripDetailsText.setText("Trip details not available");
            mapContainer.setVisibility(View.GONE);
        }
    }

    private void loadPeopleList() {
        List<PeopleModel> peopleList = peopleDAO.getPeopleByTripId(tripId);

        if (peopleList.isEmpty()) {
            peopleListText.setText("No travelers added to this trip yet.\nClick 'Add Travelers' to add people.");
        } else {
            StringBuilder peopleDisplay = new StringBuilder("Travelers:\n\n");
            for (PeopleModel person : peopleList) {
                peopleDisplay.append("• ").append(person.getName()).append("\n");
            }
            peopleListText.setText(peopleDisplay.toString());
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        if (!tripDAO.isOpen()) {
            tripDAO.open();
        }
        if (!peopleDAO.isOpen()) {
            peopleDAO.open();
        }

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);

        loadTripDetails();
        loadPeopleList();

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

        try {
            unregisterReceiver(networkChangeReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error unregistering network receiver: " + e.getMessage());
        }

        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tripDAO != null) {
            tripDAO.close();
        }
        if (peopleDAO != null) {
            peopleDAO.close();
        }

        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    private class WeatherDownloadTask extends AsyncTask<String, Integer, String> {
        private static final String API_KEY = "REMOVED_WEATHER_API_KEY";
        private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=";

        @Override
        protected void onPreExecute() {
            weatherResultsText.setText("");
            weatherStatusText.setText("Fetching weather data...");
            weatherStatusText.setVisibility(View.VISIBLE);
            weatherProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            if (!NetworkChangeReceiver.isNetworkConnected(TripDetailViewActivity.this)) {
                return "Network error: No connection available";
            }

            String destination = params[0];
            String urlString = BASE_URL + destination + "&appid=" + API_KEY + "&units=metric";

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    return parseWeatherData(result.toString());
                } else {
                    return "Failed to get weather data (Code: " + responseCode + ")";
                }
            } catch (Exception e) {
                return "Error fetching weather: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            weatherProgressBar.setVisibility(View.GONE);

            if (result.startsWith("Network error") || result.startsWith("Error fetching") || result.startsWith("Failed to get")) {
                weatherStatusText.setText(result);
            } else {
                weatherStatusText.setText("Weather data received!");
                weatherResultsText.setText(result);

                // Save the weather data in a background thread
                new SaveWeatherDataTask().execute(result);
            }
        }

        private String parseWeatherData(String jsonData) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                String cityName = jsonObject.getString("name");
                JSONObject main = jsonObject.getJSONObject("main");
                double temp = main.getDouble("temp");
                double tempMin = main.getDouble("temp_min");
                double tempMax = main.getDouble("temp_max");
                int humidity = main.getInt("humidity");
                String weather = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

                return "Weather in " + cityName + ":\n" +
                        "Temperature: " + temp + "°C\n" +
                        "Min/Max: " + tempMin + "°C / " + tempMax + "°C\n" +
                        "Humidity: " + humidity + "%\n" +
                        "Condition: " + weather;
            } catch (JSONException e) {
                return "Error parsing weather data";
            }
        }
    }


    private class SaveWeatherDataTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            weatherStatusText.setText("Saving weather data locally...");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String weatherData = params[0];
            try {
                // Make the progress bar actually look like it saved something
                Thread.sleep(500);

                String filename = "weather_data.txt";
                try {
                    FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
                    fos.write(weatherData.getBytes());
                    fos.close();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (InterruptedException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                weatherStatusText.setText("Weather data saved successfully!");
                Toast.makeText(TripDetailViewActivity.this,
                        "Weather data saved to local storage", Toast.LENGTH_SHORT).show();

                // Hide status text after a short delay
                weatherStatusText.postDelayed(() -> {
                    weatherStatusText.setVisibility(View.GONE);
                }, 3000);
            } else {
                weatherStatusText.setText("Failed to save weather data");
            }
        }
    }
}