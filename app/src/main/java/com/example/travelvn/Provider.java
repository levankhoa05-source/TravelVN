package com.example.travelvn;

public class Provider {
    public String email, name;
    public double rating, revenue;
    public int ticketsSold;

    public Provider(String email, String name, double rating, double revenue, int ticketsSold) {
        this.email = email;
        this.name = name;
        this.rating = rating;
        this.revenue = revenue;
        this.ticketsSold = ticketsSold;
    }
}