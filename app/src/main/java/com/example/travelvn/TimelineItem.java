package com.example.travelvn;

// BẮT BUỘC PHẢI CÓ CHỮ "public"
public class TimelineItem {
    public String time;
    public String description;
    public String title;

    public TimelineItem(String time, String description) {
        this.time = time;
        this.description = description;
        this.title = description;
    }

    public String getTime() { return time; }
    public String getDescription() { return description; }
    public String getTitle() { return title; }
}