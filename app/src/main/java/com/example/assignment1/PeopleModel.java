package com.example.assignment1;

public class PeopleModel {
    private long id;
    private String name;
    private long tripId;

    // Constructor
    public PeopleModel(String name, long tripId) {
        this.name = name;
        this.tripId = tripId;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }
}