package com.example.assignment1.activities;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.example.assignment1.utils.FileUtils;
import com.example.assignment1.R;

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

        fileListView = findViewById(R.id.fileListView);
        fileContentView = findViewById(R.id.fileContentView);
        renameButton = findViewById(R.id.renameButton);
        deleteButton = findViewById(R.id.deleteButton);
        goBackButton = findViewById(R.id.goBackButton);

        // Load saved files asynchronously
        loadSavedFiles();

        fileListView.setVerticalScrollBarEnabled(true);
        fileListView.setScrollbarFadingEnabled(false);

        fileListView.setOnItemClickListener((parent, view, position, id) -> {
            if (files != null && position < files.length) {
                selectedFilePosition = position;
                displayFileContent(files[position].getName());
            }
        });

        renameButton.setOnClickListener(v -> {
            if (selectedFilePosition != -1 && files != null && selectedFilePosition < files.length) {
                showRenameDialog(files[selectedFilePosition].getName());
            } else {
                Toast.makeText(FileManagerActivity.this, "Please select a file first", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (selectedFilePosition != -1 && files != null && selectedFilePosition < files.length) {
                showDeleteConfirmationDialog(files[selectedFilePosition].getName());
            } else {
                Toast.makeText(FileManagerActivity.this, "Please select a file first", Toast.LENGTH_SHORT).show();
            }
        });

        goBackButton.setOnClickListener(v -> finish());
    }

    private void loadSavedFiles() {
        new LoadFilesTask().execute();
    }

    private void displayFileContent(String fileName) {
        String content = FileUtils.readTextFile(this, fileName);
        fileContentView.setText(content != null ? content : "Error reading file");
    }

    private void showRenameDialog(final String oldFileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename File");

        final EditText input = new EditText(this);
        String nameWithoutExt = oldFileName.endsWith(".txt") ? oldFileName.substring(0, oldFileName.length() - 4) : oldFileName;
        input.setText(nameWithoutExt);
        builder.setView(input);

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

    private class LoadFilesTask extends AsyncTask<Void, Void, File[]> {
        @Override
        protected File[] doInBackground(Void... voids) {
            return FileUtils.listSavedFiles(FileManagerActivity.this);
        }

        @Override
        protected void onPostExecute(File[] result) {
            files = result;
            fileNames.clear();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    fileNames.add(file.getName());
                }
                // Start the second AsyncTask to load the content of the first file
                new ReadFileContentTask(files[0].getName()).execute();
            } else {
                fileNames.add("No saved trip files found");
            }

            // Updated to use custom trip_list_item layout
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    FileManagerActivity.this,
                    R.layout.trip_list_item,
                    android.R.id.text1,
                    fileNames
            );
            fileListView.setAdapter(adapter);
            fileListView.setEnabled(files != null && files.length > 0);
            fileContentView.setText("");
        }
    }

    // Second AsyncTask for reading file content
    private class ReadFileContentTask extends AsyncTask<Void, Void, String> {
        private String fileName;

        public ReadFileContentTask(String fileName) {
            this.fileName = fileName;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return FileUtils.readTextFile(FileManagerActivity.this, fileName);
        }

        @Override
        protected void onPostExecute(String result) {
            fileContentView.setText(result != null ? result : "Error reading file");
        }
    }
}