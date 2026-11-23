package com.example.travelvn;

public class TripRequest {
    public Trip trip;
    public String action, status, providerEmail;

    public TripRequest(Trip trip, String action, String providerEmail) {
        this.trip = trip;
        this.action = action;
        this.status = "PENDING";
        this.providerEmail = providerEmail;
    }
}