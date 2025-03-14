package com.example.assignment1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagerActivity extends ComponentActivity {

    private ListView fileListView;
    private TextView fileContentView;
    private Button renameButton;
    private Button deleteButton;
    private Button goBackButton;

    private List<String> fileNames = new ArrayList<>();
    private File[] files;
    private int selectedFilePosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);

        // Initialize views
        fileListView = findViewById(R.id.fileListView);
        fileContentView = findViewById(R.id.fileContentView);
        renameButton = findViewById(R.id.renameButton);
        deleteButton = findViewById(R.id.deleteButton);
        goBackButton = findViewById(R.id.goBackButton);

        // Load saved files
        loadSavedFiles();

        // Set up list item click listener
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (files != null && position < files.length) {
                    selectedFilePosition = position;
                    displayFileContent(files[position].getName());
                }
            }
        });

        // Set up rename button click listener
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFilePosition != -1 && files != null && selectedFilePosition < files.length) {
                    showRenameDialog(files[selectedFilePosition].getName());
                } else {
                    Toast.makeText(FileManagerActivity.this, "Please select a file first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up delete button click listener
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFilePosition != -1 && files != null && selectedFilePosition < files.length) {
                    showDeleteConfirmationDialog(files[selectedFilePosition].getName());
                } else {
                    Toast.makeText(FileManagerActivity.this, "Please select a file first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up go back button click listener
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadSavedFiles() {
        // Get all saved files
        files = FileUtils.listSavedFiles(this);
        fileNames.clear();

        if (files != null && files.length > 0) {
            for (File file : files) {
                fileNames.add(file.getName());
            }

            // Display the file names in the ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    fileNames
            );
            fileListView.setAdapter(adapter);
            fileListView.setEnabled(true);
        } else {
            // No files found
            fileNames.add("No saved trip files found");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    fileNames
            );
            fileListView.setAdapter(adapter);
            fileListView.setEnabled(false);
            fileContentView.setText("");
        }
    }

    private void displayFileContent(String fileName) {
        // Read the file content
        String content = FileUtils.readTextFile(this, fileName);

        if (content != null) {
            fileContentView.setText(content);
        } else {
            fileContentView.setText("Error reading file");
        }
    }

    private void showRenameDialog(final String oldFileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename File");

        // Set up the input
        final EditText input = new EditText(this);
        // Remove the .txt extension for user input
        String nameWithoutExt = oldFileName.endsWith(".txt")
                ? oldFileName.substring(0, oldFileName.length() - 4)
                : oldFileName;
        input.setText(nameWithoutExt);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newFileName = input.getText().toString().trim();
            if (newFileName.isEmpty()) {
                Toast.makeText(FileManagerActivity.this, "File name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = FileUtils.renameFile(FileManagerActivity.this, oldFileName, newFileName);
            if (success) {
                selectedFilePosition = -1;
                fileContentView.setText("");
                loadSavedFiles();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmationDialog(final String fileName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete File")
                .setMessage("Are you sure you want to delete " + fileName + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = FileUtils.deleteFile(FileManagerActivity.this, fileName);
                    if (deleted) {
                        selectedFilePosition = -1;
                        fileContentView.setText("");
                        loadSavedFiles();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedFiles();
    }
}