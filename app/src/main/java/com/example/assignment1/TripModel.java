package com.example.assignment1;

public class TripModel {

    private long id;
    private String tripName;
    private String destination;
    private String budget;
    private String departureDate;
    private String returnDate;

    // Constructor
    public TripModel(String tripName, String destination, String budget, String departureDate, String returnDate) {
        this.tripName = tripName;
        this.destination = destination;
        this.budget = budget;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
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
}
