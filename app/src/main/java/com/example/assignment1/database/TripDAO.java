package com.example.assignment1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.assignment1.model.TripModel;

import java.util.ArrayList;
import java.util.List;

public class TripDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public boolean isOpen() {
        return database != null && database.isOpen();
    }

    public TripDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Insert a new trip
    public long addTrip(TripModel trip) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TRIP_NAME, trip.getTripName());
        values.put(DatabaseHelper.COLUMN_DESTINATION, trip.getDestination());
        values.put(DatabaseHelper.COLUMN_BUDGET, trip.getBudget());
        values.put(DatabaseHelper.COLUMN_START_DATE, trip.getDepartureDate());
        values.put(DatabaseHelper.COLUMN_END_DATE, trip.getReturnDate());
        values.put(DatabaseHelper.COLUMN_LATITUDE, trip.getLatitude());
        values.put(DatabaseHelper.COLUMN_LONGITUDE, trip.getLongitude());

        return database.insert(DatabaseHelper.TABLE_TRIPS, null, values);
    }

    public TripModel getTripById(int tripId) {
        TripModel trip = null;
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_TRIPS,
                null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(tripId)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            trip = new TripModel(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRIP_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESTINATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE))
            );
            trip.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));

            // Get latitude and longitude if these columns exist
            int latIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE);
            int longIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LONGITUDE);

            if (latIndex != -1 && longIndex != -1) {
                trip.setLatitude(cursor.getDouble(latIndex));
                trip.setLongitude(cursor.getDouble(longIndex));
            }

            cursor.close();
        }

        return trip;
    }

    // Get all trips
    public List<TripModel> getAllTrips() {
        List<TripModel> trips = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_TRIPS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                TripModel trip = new TripModel(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRIP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESTINATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE))
                );
                trip.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));

                // Get latitude and longitude if these columns exist
                int latIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE);
                int longIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LONGITUDE);

                if (latIndex != -1 && longIndex != -1) {
                    trip.setLatitude(cursor.getDouble(latIndex));
                    trip.setLongitude(cursor.getDouble(longIndex));
                }

                trips.add(trip);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return trips;
    }

    // Delete a trip
    public void deleteTrip(long id) {
        database.delete(DatabaseHelper.TABLE_TRIPS, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Update a trip
    public int updateTrip(TripModel trip) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TRIP_NAME, trip.getTripName());
        values.put(DatabaseHelper.COLUMN_DESTINATION, trip.getDestination());
        values.put(DatabaseHelper.COLUMN_BUDGET, trip.getBudget());
        values.put(DatabaseHelper.COLUMN_START_DATE, trip.getDepartureDate());
        values.put(DatabaseHelper.COLUMN_END_DATE, trip.getReturnDate());
        values.put(DatabaseHelper.COLUMN_LATITUDE, trip.getLatitude());
        values.put(DatabaseHelper.COLUMN_LONGITUDE, trip.getLongitude());

        return database.update(
                DatabaseHelper.TABLE_TRIPS,
                values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(trip.getId())}
        );
    }
}