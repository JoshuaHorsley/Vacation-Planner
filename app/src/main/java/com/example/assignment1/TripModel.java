package com.example.assignment1;

public class TripModel {

    private long id;
    private String tripName;
    private String destination;
    private String budget;
    private String departureDate;
    private String returnDate;
    private double latitude;
    private double longitude;

    // Constructor
    public TripModel(String tripName, String destination, String budget, String departureDate, String returnDate) {
        this.tripName = tripName;
        this.destination = destination;
        this.budget = budget;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
    }

    // New constructor with coordinates
    public TripModel(String tripName, String destination, String budget, String departureDate, String returnDate, double latitude, double longitude) {
        this.tripName = tripName;
        this.destination = destination;
        this.budget = budget;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}