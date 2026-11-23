package com.example.travelvn;

public class Message {
    public String from;
    public String to;
    public String content;
    public long timestamp;

    public Message() {
        this.timestamp = 0L;
    }

    public Message(String from, String to, String content) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
}