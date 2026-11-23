package com.example.travelvn;

public class Destination {
    private String name;
    private int imageRes;

    public Destination() {
    }

    public Destination(String name, int imageRes) {
        this.name = name;
        this.imageRes = imageRes;
    }

    public String getName() { return name != null ? name : ""; }
    public int getImageRes() { return imageRes; }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }
}