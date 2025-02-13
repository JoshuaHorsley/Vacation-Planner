package  com.example.assignment1;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PeopleActivity extends ComponentActivity {
    private static final String TRIP_DATA = "TripData";
    private SharedPreferences sharedPreferences;
    private TextView tripDetailsText, peopleListText;
    private EditText personNameInput;
    private Button addPersonButton, saveButton, goBackButton;

    private List<String> peopleList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_activity);

        sharedPreferences = getSharedPreferences(TRIP_DATA, Context.MODE_PRIVATE);
        tripDetailsText = findViewById(R.id.tripDetailsText);
        personNameInput = findViewById(R.id.personNameInput);
        peopleListText = findViewById(R.id.peopleListText);
        addPersonButton = findViewById(R.id.addPersonButton);
        saveButton = findViewById(R.id.saveButton);
        goBackButton = findViewById(R.id.goBackButton);

        // Load existing trip details and people list
        String tripDetails = sharedPreferences.getString("trip_summary", "No trip details available");
        tripDetailsText.setText(tripDetails);

        loadPeopleList(); // Load saved people list

        addPersonButton.setOnClickListener(view -> {
            String personName = personNameInput.getText().toString().trim();
            if (personName.isEmpty()) {
                Toast.makeText(this, "Please enter a person's name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add name to list
            peopleList.add(personName);
            savePeopleList();  // Save updated list
            updatePeopleListText();  // Update UI

            // Clear input
            personNameInput.setText("");
            Toast.makeText(this, personName + " added to the trip!", Toast.LENGTH_SHORT).show();
        });
        saveButton.setOnClickListener(view -> savePeopleList());
        goBackButton.setOnClickListener(view -> finish());
    }

    private void loadPeopleList() {
        peopleList = new ArrayList<>();
        String peopleJson = sharedPreferences.getString("people_list", "[]");

        try {
            JSONArray jsonArray = new JSONArray(peopleJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                peopleList.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        updatePeopleListText();
    }

    private void savePeopleList() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray(peopleList);
        editor.putString("people_list", jsonArray.toString());
        editor.apply();
        Toast.makeText(this, "People list saved!", Toast.LENGTH_SHORT).show();
    }

    private void updatePeopleListText() {
        if (peopleList.isEmpty()) {
            peopleListText.setText("No people added.");
        } else {
            StringBuilder peopleDisplay = new StringBuilder("People in trip:\n");
            for (String person : peopleList) {
                peopleDisplay.append("- ").append(person).append("\n");
            }
            peopleListText.setText(peopleDisplay.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPeopleList();
    }
}