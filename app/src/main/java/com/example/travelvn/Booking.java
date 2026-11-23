package com.example.travelvn;

public class Booking {
    public String tripName, customerName, phone, seat, status, date, qrData;
    public double price;

    public Booking() {
        this.qrData = "";
    }

    public Booking(String tripName, String customerName, String phone, String seat, String status, double price, String date) {
        this.tripName = tripName;
        this.customerName = customerName;
        this.phone = phone;
        this.seat = seat;
        this.status = status;
        this.price = price;
        this.date = date;
        this.qrData = "TRAVELVN-" + System.currentTimeMillis() + "-" + seat;
    }
}