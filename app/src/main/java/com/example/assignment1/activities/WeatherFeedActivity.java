package com.example.assignment1.activities;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.activity.ComponentActivity;

import com.example.assignment1.BuildConfig;
import com.example.assignment1.utils.ConnectivityUtils;
import com.example.assignment1.receivers.NetworkChangeReceiver;
import com.example.assignment1.R;

import java.io.FileOutputStream;

public class WeatherFeedActivity extends ComponentActivity implements NetworkChangeReceiver.NetworkChangeListener {
    private TextView statusText;
    private TextView weatherResultsView;
    private Button downloadButton;
    private Button backButton;
    private ProgressBar progressBar;
    private EditText destinationInput;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_feed);

        statusText = findViewById(R.id.statusText);
        weatherResultsView = findViewById(R.id.weatherResultsView);
        downloadButton = findViewById(R.id.downloadButton);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);
        destinationInput = findViewById(R.id.destinationInput);

        networkChangeReceiver = new NetworkChangeReceiver(this);

        downloadButton.setOnClickListener(v -> {
            if (ConnectivityUtils.isNetworkConnected(this)) {
                String destination = destinationInput.getText().toString().trim();
                if (!destination.isEmpty()) {
                    new WeatherFeedDownloadTask().execute(destination);
                } else {
                    Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No network connection available. Please connect to the internet.",
                        Toast.LENGTH_LONG).show();
                statusText.setText("Error: No network connection. Cannot fetch weather data.");
            }
        });

        backButton.setOnClickListener(v -> finish());

        String destination = getIntent().getStringExtra("destination");
        if (destination != null && !destination.isEmpty()) {
            destinationInput.setText(destination);
        }

        // Set initial network status
        updateNetworkStatus(ConnectivityUtils.isNetworkConnected(this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register the NetworkChangeReceiver
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the NetworkChangeReceiver to avoid memory leaks
        try {
            unregisterReceiver(networkChangeReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered exception
            e.printStackTrace();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        updateNetworkStatus(isConnected);
    }

    private void updateNetworkStatus(boolean isConnected) {
        if (isConnected) {
            statusText.setText("Ready to download weather data");
            downloadButton.setEnabled(true);
            if (ConnectivityUtils.isWifiConnected(this)) {
                statusText.setText("Connected via WiFi. Ready to download weather data.");
            } else {
                statusText.setText("Connected via mobile data. Ready to download weather data.");
            }
        } else {
            statusText.setText("No network connection available. Please connect to the internet.");
            downloadButton.setEnabled(false);
        }
    }

    private class WeatherFeedDownloadTask extends AsyncTask<String, Integer, String> {
        private static final String API_KEY = BuildConfig.WEATHER_API_KEY;
        private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=";

        @Override
        protected void onPreExecute() {
            statusText.setText("Fetching weather data...");
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            weatherResultsView.setText("");
        }

        @Override
        protected String doInBackground(String... params) {
            if (!ConnectivityUtils.isNetworkConnected(WeatherFeedActivity.this)) {
                return "Network error: No connection available";
            }

            String destination = params[0];
            String urlString = BASE_URL + destination + "&appid=" + API_KEY + "&units=metric";

            try {
                // Simulate progress updates
                publishProgress(20);
                Thread.sleep(300);

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                publishProgress(40);
                Thread.sleep(200);

                connection.connect();

                publishProgress(60);
                Thread.sleep(200);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    publishProgress(80);
                    Thread.sleep(200);

                    return parseWeatherData(result.toString());
                } else {
                    return "Failed to get weather data (Code: " + responseCode + ")";
                }
            } catch (Exception e) {
                return "Error fetching weather: " + e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setProgress(100);

            if (result.startsWith("Network error") || result.startsWith("Error fetching") || result.startsWith("Failed to get")) {
                statusText.setText(result);
            } else {
                statusText.setText("Weather data received successfully!");
                weatherResultsView.setText(result);

                // Save the weather data in a background thread
                new SaveWeatherDataTask().execute(result);
            }

            // Hide progress bar after a delay
            progressBar.postDelayed(() -> progressBar.setVisibility(View.INVISIBLE), 1000);
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

                JSONObject wind = jsonObject.getJSONObject("wind");
                double windSpeed = wind.getDouble("speed");

                return "Weather in " + cityName + ":\n\n" +
                        "Temperature: " + temp + "°C\n" +
                        "Min/Max: " + tempMin + "°C / " + tempMax + "°C\n" +
                        "Humidity: " + humidity + "%\n" +
                        "Wind Speed: " + windSpeed + " m/s\n" +
                        "Condition: " + weather;
            } catch (JSONException e) {
                return "Error parsing weather data";
            }
        }
    }

    private class SaveWeatherDataTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            statusText.setText("Saving weather data locally...");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String weatherData = params[0];
            try {
                // Make the progress bar actually look like it saved something
                Thread.sleep(1000);

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
                statusText.setText("Weather data saved successfully!");
                Toast.makeText(WeatherFeedActivity.this,
                        "Weather data saved to local storage", Toast.LENGTH_SHORT).show();
            } else {
                statusText.setText("Failed to save weather data");
            }
        }
    }
}