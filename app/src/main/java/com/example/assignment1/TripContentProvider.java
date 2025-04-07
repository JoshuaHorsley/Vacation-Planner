package com.example.assignment1;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.assignment1.database.DatabaseHelper;

/**
 * Content Provider for trip planner application
 * Provides access to trips and people data through a standardized URI interface
 */
public class TripContentProvider extends ContentProvider {
    private static final String TAG = "TripContentProvider";

    // Authority for this provider (should match in manifest)
    public static final String AUTHORITY = "com.example.assignment1.provider";

    // Base path for trips table
    public static final String TRIPS_PATH = "trips";

    // Base path for people table
    public static final String PEOPLE_PATH = "people";

    // URIs for accessing the provider
    public static final Uri TRIPS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TRIPS_PATH);
    public static final Uri PEOPLE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PEOPLE_PATH);

    // MIME types for returning multiple items
    public static final String TRIPS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.example.trips";
    public static final String PEOPLE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.example.people";

    // MIME types for returning a single item
    public static final String TRIP_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.example.trip";
    public static final String PERSON_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.example.person";

    // Integer constants for the URI matcher
    private static final int TRIPS = 1;
    private static final int TRIP_ID = 2;
    private static final int PEOPLE = 3;
    private static final int PERSON_ID = 4;
    private static final int TRIP_PEOPLE = 5;

    // URI Matcher for routing URIs
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, TRIPS_PATH, TRIPS);
        sUriMatcher.addURI(AUTHORITY, TRIPS_PATH + "/#", TRIP_ID);
        sUriMatcher.addURI(AUTHORITY, PEOPLE_PATH, PEOPLE);
        sUriMatcher.addURI(AUTHORITY, PEOPLE_PATH + "/#", PERSON_ID);
        sUriMatcher.addURI(AUTHORITY, TRIPS_PATH + "/#/" + PEOPLE_PATH, TRIP_PEOPLE);
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case TRIPS:
                // Query all trips
                cursor = db.query(
                        DatabaseHelper.TABLE_TRIPS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case TRIP_ID:
                // Query a specific trip by ID
                String tripId = uri.getLastPathSegment();
                cursor = db.query(
                        DatabaseHelper.TABLE_TRIPS,
                        projection,
                        DatabaseHelper.COLUMN_ID + "=?",
                        new String[]{tripId},
                        null,
                        null,
                        sortOrder);
                break;

            case PEOPLE:
                // Query all people
                cursor = db.query(
                        DatabaseHelper.TABLE_PEOPLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case PERSON_ID:
                // Query a specific person by ID
                String personId = uri.getLastPathSegment();
                cursor = db.query(
                        DatabaseHelper.TABLE_PEOPLE,
                        projection,
                        DatabaseHelper.COLUMN_PEOPLE_ID + "=?",
                        new String[]{personId},
                        null,
                        null,
                        sortOrder);
                break;

            case TRIP_PEOPLE:
                // Query all people for a specific trip
                String tripIdForPeople = uri.getPathSegments().get(1);
                cursor = db.query(
                        DatabaseHelper.TABLE_PEOPLE,
                        projection,
                        DatabaseHelper.COLUMN_TRIP_ID + "=?",
                        new String[]{tripIdForPeople},
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Set notification URI on the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case TRIPS:
                return TRIPS_CONTENT_TYPE;

            case TRIP_ID:
                return TRIP_CONTENT_ITEM_TYPE;

            case PEOPLE:
                return PEOPLE_CONTENT_TYPE;

            case PERSON_ID:
                return PERSON_CONTENT_ITEM_TYPE;

            case TRIP_PEOPLE:
                return PEOPLE_CONTENT_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        Uri resultUri;

        switch (sUriMatcher.match(uri)) {
            case TRIPS:
                id = db.insert(DatabaseHelper.TABLE_TRIPS, null, values);
                resultUri = ContentUris.withAppendedId(TRIPS_CONTENT_URI, id);
                break;

            case PEOPLE:
                id = db.insert(DatabaseHelper.TABLE_PEOPLE, null, values);
                resultUri = ContentUris.withAppendedId(PEOPLE_CONTENT_URI, id);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (id > 0) {
            // Notify any observers of the change
            getContext().getContentResolver().notifyChange(uri, null);
            return resultUri;
        }

        throw new android.database.SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        String id;

        switch (sUriMatcher.match(uri)) {
            case TRIPS:
                count = db.delete(DatabaseHelper.TABLE_TRIPS, selection, selectionArgs);
                break;

            case TRIP_ID:
                id = uri.getLastPathSegment();
                count = db.delete(
                        DatabaseHelper.TABLE_TRIPS,
                        DatabaseHelper.COLUMN_ID + "=?",
                        new String[]{id});
                break;

            case PEOPLE:
                count = db.delete(DatabaseHelper.TABLE_PEOPLE, selection, selectionArgs);
                break;

            case PERSON_ID:
                id = uri.getLastPathSegment();
                count = db.delete(
                        DatabaseHelper.TABLE_PEOPLE,
                        DatabaseHelper.COLUMN_PEOPLE_ID + "=?",
                        new String[]{id});
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (count > 0) {
            // Notify any observers of the change
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        String id;

        switch (sUriMatcher.match(uri)) {
            case TRIPS:
                count = db.update(DatabaseHelper.TABLE_TRIPS, values, selection, selectionArgs);
                break;

            case TRIP_ID:
                id = uri.getLastPathSegment();
                count = db.update(
                        DatabaseHelper.TABLE_TRIPS,
                        values,
                        DatabaseHelper.COLUMN_ID + "=?",
                        new String[]{id});
                break;

            case PEOPLE:
                count = db.update(DatabaseHelper.TABLE_PEOPLE, values, selection, selectionArgs);
                break;

            case PERSON_ID:
                id = uri.getLastPathSegment();
                count = db.update(
                        DatabaseHelper.TABLE_PEOPLE,
                        values,
                        DatabaseHelper.COLUMN_PEOPLE_ID + "=?",
                        new String[]{id});
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (count > 0) {
            // Notify any observers of the change
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }
}