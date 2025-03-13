package com.example.assignment1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tripPlanner.db";
    private static final int DATABASE_VERSION = 1;

    // Trip table
    public static final String TABLE_TRIPS = "trips";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TRIP_NAME = "trip_name";
    public static final String COLUMN_BUDGET = "budget";
    public static final String COLUMN_DESTINATION = "destination";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";

    // People table
    public static final String TABLE_PEOPLE = "people";
    public static final String COLUMN_PEOPLE_ID = "id";
    public static final String COLUMN_PERSON_NAME = "name";
    public static final String COLUMN_TRIP_ID = "trip_id"; // Foreign key to trips table

    private static final String TRIPS_TABLE_CREATE =
            "CREATE TABLE " + TABLE_TRIPS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TRIP_NAME + " TEXT, " +
                    COLUMN_BUDGET + " TEXT, " +
                    COLUMN_DESTINATION + " TEXT, " +
                    COLUMN_START_DATE + " TEXT, " +
                    COLUMN_END_DATE + " TEXT);";

    private static final String PEOPLE_TABLE_CREATE =
            "CREATE TABLE " + TABLE_PEOPLE + " (" +
                    COLUMN_PEOPLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PERSON_NAME + " TEXT, " +
                    COLUMN_TRIP_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_TRIP_ID + ") REFERENCES " +
                    TABLE_TRIPS + "(" + COLUMN_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TRIPS_TABLE_CREATE);
        db.execSQL(PEOPLE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        onCreate(db);
    }
}
