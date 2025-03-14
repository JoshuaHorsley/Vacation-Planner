package com.example.assignment1;

import android.util.Log;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class PeopleDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public boolean isOpen() {
        return database != null && database.isOpen();
    }

    public PeopleDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Insert a new person
    public long addPerson(PeopleModel person) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PERSON_NAME, person.getName());
        values.put(DatabaseHelper.COLUMN_TRIP_ID, person.getTripId());

        return database.insert(DatabaseHelper.TABLE_PEOPLE, null, values);
    }

    // Get a person by ID
    public PeopleModel getPersonById(long personId) {
        PeopleModel person = null;
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_PEOPLE,
                null,
                DatabaseHelper.COLUMN_PEOPLE_ID + " = ?",
                new String[]{String.valueOf(personId)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            person = new PeopleModel(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON_NAME)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRIP_ID))
            );
            person.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEOPLE_ID)));
            cursor.close();
        }

        return person;
    }

    // Get all people for a specific trip
    public List<PeopleModel> getPeopleByTripId(long tripId) {
        List<PeopleModel> people = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_PEOPLE,
                null,
                DatabaseHelper.COLUMN_TRIP_ID + " = ?",
                new String[]{String.valueOf(tripId)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                PeopleModel person = new PeopleModel(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON_NAME)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRIP_ID))
                );
                person.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEOPLE_ID)));
                people.add(person);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return people;
    }

    // Get all people
    public List<PeopleModel> getAllPeople() {
        List<PeopleModel> people = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_PEOPLE, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                PeopleModel person = new PeopleModel(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PERSON_NAME)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRIP_ID))
                );
                person.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PEOPLE_ID)));
                people.add(person);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return people;
    }

    // Update a person
    public int updatePerson(PeopleModel person) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PERSON_NAME, person.getName());
        values.put(DatabaseHelper.COLUMN_TRIP_ID, person.getTripId());

        return database.update(
                DatabaseHelper.TABLE_PEOPLE,
                values,
                DatabaseHelper.COLUMN_PEOPLE_ID + " = ?",
                new String[]{String.valueOf(person.getId())}
        );
    }

    // Delete a person
    public boolean deletePerson(long personId) {
        try {
            int rowsDeleted = database.delete(
                    DatabaseHelper.TABLE_PEOPLE,
                    DatabaseHelper.COLUMN_PEOPLE_ID + " = ?",
                    new String[]{String.valueOf(personId)}
            );
            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e("PeopleDAO", "Error deleting person: " + e.getMessage());
            return false;
        }
    }

    // Delete all people for a specific trip
    public void deletePeopleByTripId(long tripId) {
        database.delete(
                DatabaseHelper.TABLE_PEOPLE,
                DatabaseHelper.COLUMN_TRIP_ID + " = ?",
                new String[]{String.valueOf(tripId)}
        );
    }
}