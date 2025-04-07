package com.example.assignment1.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.assignment1.TripContentProvider;
import com.example.assignment1.database.DatabaseHelper;
import com.example.assignment1.utils.PermissionsUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with the Contacts Content Provider
 */
public class ContactsUtility {
    private static final String TAG = "ContactsUtility";

    /**
     * Get a list of contacts from the device
     * @param context Application context
     * @return List of contact names
     */
    public static List<String> getContacts(Context context) {
        List<String> contactsList = new ArrayList<>();

        // Check permission before querying contacts
        if (!PermissionsUtility.hasContactsPermission(context)) {
            Log.e(TAG, "No permission to read contacts");
            return contactsList;
        }

        try {
            ContentResolver contentResolver = context.getContentResolver();

            // Define the columns to retrieve
            String[] projection = new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            };

            // Query the contacts table
            Cursor cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    projection,
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            );

            if (cursor != null) {
                // Get the column indexes
                int nameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);

                // Loop through all contacts
                while (cursor.moveToNext()) {
                    String contactName = cursor.getString(nameColumnIndex);
                    if (contactName != null && !contactName.isEmpty()) {
                        contactsList.add(contactName);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching contacts: " + e.getMessage());
            e.printStackTrace();
        }

        return contactsList;
    }

    /**
     * Add a traveler from contacts to a specific trip
     * @param context Application context
     * @param contactName Contact name
     * @param tripId Trip ID
     * @return true if successful, false otherwise
     */
    public static boolean addContactAsTraveler(Context context, String contactName, long tripId) {
        try {
            // Insert contact into people table using content provider
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PERSON_NAME, contactName);
            values.put(DatabaseHelper.COLUMN_TRIP_ID, tripId);

            Uri resultUri = context.getContentResolver().insert(TripContentProvider.PEOPLE_CONTENT_URI, values);
            return resultUri != null;
        } catch (Exception e) {
            Log.e(TAG, "Error adding contact as traveler: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all travelers for a trip using the content provider
     * @param context Application context
     * @param tripId Trip ID
     * @return List of traveler names
     */
    public static List<String> getTravelersForTrip(Context context, long tripId) {
        List<String> travelers = new ArrayList<>();

        try {
            // Build URI for querying travelers for a specific trip
            Uri tripTravelersUri = Uri.withAppendedPath(
                    Uri.withAppendedPath(TripContentProvider.TRIPS_CONTENT_URI, String.valueOf(tripId)),
                    TripContentProvider.PEOPLE_PATH
            );

            // Columns to retrieve
            String[] projection = {DatabaseHelper.COLUMN_PERSON_NAME};

            // Query for travelers
            Cursor cursor = context.getContentResolver().query(
                    tripTravelersUri,
                    projection,
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PERSON_NAME);

                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameIndex);
                    travelers.add(name);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting travelers: " + e.getMessage());
            e.printStackTrace();
        }

        return travelers;
    }
}