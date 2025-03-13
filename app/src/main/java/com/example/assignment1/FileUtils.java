package com.example.assignment1;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    // Method to save trip details to a file
    public static void saveTripDetailsToFile(Context context, String tripDetails) {
        // Define the file name
        String fileName = "trip_details.txt";

        // Get the directory for saving the file (external storage, Documents folder)
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        // Create the file object
        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Write the trip details to the file
            fos.write(tripDetails.getBytes());
            // Notify the user that the file was saved successfully
            Toast.makeText(context, "Trip details saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Notify the user if there was an error
            Toast.makeText(context, "Failed to save trip details", Toast.LENGTH_SHORT).show();
        }
    }
}