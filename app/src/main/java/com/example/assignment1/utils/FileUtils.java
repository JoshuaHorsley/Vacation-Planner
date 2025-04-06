package com.example.assignment1.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {
    private static final String TAG = "FileUtils";

    // Save trip details as a text file
    public static void saveTripDetailsToFile(Context context, String tripDetails, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            // Create a default filename if none provided
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            fileName = "trip_details_" + timestamp + ".txt";
        }

        // Make sure the filename has .txt extension
        if (!fileName.endsWith(".txt")) {
            fileName += ".txt";
        }

        // Get the directory for saving the file
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Create the file object
        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            // Write the trip details to the file
            writer.write(tripDetails);
            writer.flush();

            // Notify the user that the file was saved successfully
            Toast.makeText(context, "Trip details saved to " + file.getName(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "File saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            // Notify the user if there was an error
            Toast.makeText(context, "Failed to save trip details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Read text from a file
    public static String readTextFile(Context context, String fileName) {
        StringBuilder text = new StringBuilder();

        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

            if (!file.exists()) {
                return null;
            }

            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    text.append(line).append('\n');
                }
            }

            return text.toString();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading file: " + e.getMessage());
            return null;
        }
    }

    // Rename a file
    public static boolean renameFile(Context context, String oldFileName, String newFileName) {
        // Make sure the new filename has .txt extension
        if (!newFileName.endsWith(".txt")) {
            newFileName += ".txt";
        }

        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File oldFile = new File(directory, oldFileName);
        File newFile = new File(directory, newFileName);

        // Check if the old file exists and the new filename doesn't already exist
        if (!oldFile.exists()) {
            Toast.makeText(context, "File not found: " + oldFileName, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newFile.exists()) {
            Toast.makeText(context, "A file with that name already exists", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Attempt to rename the file
        boolean success = oldFile.renameTo(newFile);
        if (success) {
            Toast.makeText(context, "File renamed to " + newFileName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to rename file", Toast.LENGTH_SHORT).show();
        }

        return success;
    }

    // Delete a file
    public static boolean deleteFile(Context context, String fileName) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Toast.makeText(context, "File deleted: " + fileName, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "File deleted: " + fileName);
            } else {
                Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to delete file: " + fileName);
            }
            return deleted;
        } else {
            Toast.makeText(context, "File not found: " + fileName, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // List all saved files
    public static File[] listSavedFiles(Context context) {
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (directory != null && directory.exists()) {
            return directory.listFiles(file -> file.isFile() && file.getName().endsWith(".txt"));
        }
        return new File[0];
    }
}