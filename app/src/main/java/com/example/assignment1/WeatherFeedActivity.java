package com.example.assignment1;

import android.content.Context;
import android.content.Intent;
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

import java.io.FileOutputStream;

public class WeatherFeedActivity extends ComponentActivity {
    private TextView statusText;
    private TextView weatherResultsView;
    private Button downloadButton;
    private Button backButton;
    private ProgressBar progressBar;
    private EditText destinationInput;

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

        downloadButton.setOnClickListener(v -> {
            String destination = destinationInput.getText().toString().trim();
            if (!destination.isEmpty()) {
                new WeatherFeedDownloadTask().execute(destination);
            } else {
                Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());

        String destination = getIntent().getStringExtra("destination");
        if (destination != null && !destination.isEmpty()) {
            destinationInput.setText(destination);
        }
    }

    private class WeatherFeedDownloadTask extends AsyncTask<String, Integer, String> {
        private static final String API_KEY = "REMOVED_WEATHER_API_KEY";
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
            progressBar.setVisibility(View.INVISIBLE);
            statusText.setText("Weather data received!");
            weatherResultsView.setText(result);

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


    private class SaveWeatherDataTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            statusText.setText("Saving weather data locally...");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String weatherData = params[0];
            try {
                //make the progress bar actually look like it saved something
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