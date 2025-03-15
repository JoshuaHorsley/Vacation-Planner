package com.example.assignment1;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TripDetailViewActivity extends ComponentActivity {
    private TextView tripDetailsText, peopleListText;
    private Button editTripButton, addTravelersButton, saveToFileButton, deleteTripButton, goBackButton, weatherButton;

    // Weather widget components
    private LinearLayout weatherWidgetContainer;
    private ProgressBar weatherProgressBar;
    private TextView weatherStatusText, weatherResultsText;

    private TripDAO tripDAO;
    private PeopleDAO peopleDAO;
    private long tripId;
    private TripModel currentTrip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail_view);

        // Initialize views
        tripDetailsText = findViewById(R.id.tripDetailsText);
        peopleListText = findViewById(R.id.peopleListText);
        editTripButton = findViewById(R.id.editTripButton);
        addTravelersButton = findViewById(R.id.addTravelersButton);
        saveToFileButton = findViewById(R.id.saveToFileButton);
        deleteTripButton = findViewById(R.id.deleteTripButton);
        goBackButton = findViewById(R.id.goBackButton);
        weatherButton = findViewById(R.id.weatherButton);

        // Initialize weather widget components
        weatherWidgetContainer = findViewById(R.id.weatherWidgetContainer);
        weatherProgressBar = findViewById(R.id.weatherProgressBar);
        weatherStatusText = findViewById(R.id.weatherStatusText);
        weatherResultsText = findViewById(R.id.weatherResultsText);

        // Initialize DAOs
        tripDAO = new TripDAO(this);
        peopleDAO = new PeopleDAO(this);

        // Open database connections
        tripDAO.open();
        peopleDAO.open();

        // Get tripId from intent
        Intent intent = getIntent();
        tripId = intent.getLongExtra("tripId", -1);

        if (tripId == -1) {
            Toast.makeText(this, "Error: No trip selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load trip details and people list
        loadTripDetails();
        loadPeopleList();

        // Set up button click listeners
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
            // Show a dialog to name the file
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
            if (currentTrip != null && currentTrip.getDestination() != null && !currentTrip.getDestination().trim().isEmpty()) {
                // Show the weather widget container
                weatherWidgetContainer.setVisibility(View.VISIBLE);

                // Fetch weather data
                new WeatherDownloadTask().execute(currentTrip.getDestination());
            } else {
                Toast.makeText(TripDetailViewActivity.this,
                        "No destination set for this trip", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up delete button click listener
        deleteTripButton.setOnClickListener(v -> {
            showDeleteTripConfirmation();
        });

        goBackButton.setOnClickListener(v -> finish());
    }

    /**
     * Show a confirmation dialog before deleting the trip
     */
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

    /**
     * Delete the current trip and all associated travelers
     */
    private void deleteTrip() {
        // First delete all travelers associated with this trip
        List<PeopleModel> peopleList = peopleDAO.getPeopleByTripId(tripId);
        for (PeopleModel person : peopleList) {
            peopleDAO.deletePerson(person.getId());
        }

        // Then delete the trip
        tripDAO.deleteTrip(tripId);

        Toast.makeText(this, "Trip deleted successfully", Toast.LENGTH_SHORT).show();

        // Return to the previous screen
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
        } else {
            tripDetailsText.setText("Trip details not available");
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
    protected void onResume() {
        super.onResume();
        // Make sure database connections are open
        if (!tripDAO.isOpen()) {
            tripDAO.open();
        }
        if (!peopleDAO.isOpen()) {
            peopleDAO.open();
        }

        // Refresh data
        loadTripDetails();
        loadPeopleList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connections
        if (tripDAO != null) {
            tripDAO.close();
        }
        if (peopleDAO != null) {
            peopleDAO.close();
        }
    }

    /**
     * AsyncTask to download weather data
     */
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
            weatherStatusText.setText("Weather data received!");
            weatherResultsText.setText(result);

            // Save the weather data in a background thread
            new SaveWeatherDataTask().execute(result);
        }

        private String parseWeatherData(String jsonData) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                String cityName = jsonObject.getString("name");
                JSONObject main = jsonObject.getJSONObject("main");
                double temp = main.getDouble("temp");
                String weather = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

                return "Weather in " + cityName + ":\n" +
                        "Temperature: " + temp + "°C\n" +
                        "Condition: " + weather;
            } catch (JSONException e) {
                return "Error parsing weather data";
            }
        }
    }

    /**
     * AsyncTask to save weather data to a file
     */
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