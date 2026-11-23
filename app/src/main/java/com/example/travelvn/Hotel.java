package com.example.travelvn;

public class Hotel {
    public String name, address;
    public double price, rating;
    public int imageRes;

    public Hotel() {
    }

    public Hotel(String name, String address, double price, double rating, int imageRes) {
        this.name = name;
        this.address = address;
        this.price = price;
        this.rating = rating;
        this.imageRes = imageRes;
    }
}