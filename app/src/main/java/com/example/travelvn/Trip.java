package com.example.travelvn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Trip {
    private String id;
    private String time;
    private String path;
    private String price;
    private String type;
    private String date;
    private String provider; // Tên nhà xe/hãng bay
    private int seats;
    private List<String> occupiedSeats = new ArrayList<>();
    private List<TimelineItem> timelines = new ArrayList<>();

    // Constructor rỗng để Firebase Firestore parse dữ liệu
    public Trip() {
        this.occupiedSeats = new ArrayList<>();
        this.timelines = new ArrayList<>();
    }

    // Constructor chính: Nhận đầy đủ 8 tham số
    public Trip(String id, String time, String path, String price, String type, String date, String provider, int seats) {
        this.id = id;
        this.time = time;
        this.path = path;
        this.price = price;
        this.type = type;
        this.date = date;
        this.provider = provider;
        this.seats = seats;
    }

    // Constructor phụ để tương thích với các phần code cũ
    public Trip(String time, String path, String price, String type) {
        this("T-TEMP", time, path, price, type, "2026-04-05", "Vivu Travel", 30);
    }

    // --- CÁC PHƯƠNG THỨC BỔ SUNG ĐỂ SỬA LỖI ---

    // Sửa lỗi: "Cannot resolve method 'getProviderName'"
    public String getProviderName() {
        return provider;
    }

    // Sửa lỗi: "Cannot resolve method 'getName'" trong TripDetailsActivity
    public String getName() {
        return provider + " (" + type + ")";
    }

    // Sửa lỗi: "Cannot resolve method 'getAvailableSeats'"
    public int getAvailableSeats() {
        if (occupiedSeats == null) {
            occupiedSeats = new ArrayList<>();
        }
        return seats - occupiedSeats.size();
    }

    // Sửa lỗi: "Cannot resolve method 'getTimeline'"
    public List<TimelineItem> getTimeline() {
        if (timelines == null) {
            timelines = new ArrayList<>();
        }
        return timelines;
    }

    public void addTimeline(String time, String description) {
        getTimeline().add(new TimelineItem(time, description));
    }

    // --- CÁC GETTER CHUẨN ---
    public String getId() { return id; }
    public String getTime() { return time; }
    public String getPath() { return path; }
    public String getPrice() { return price; }
    public String getType() { return type; }
    public String getDate() { return date; }
    public String getProvider() { return provider; }
    public int getSeats() { return seats; }
    public List<String> getOccupiedSeats() {
        if (occupiedSeats == null) {
            occupiedSeats = new ArrayList<>();
        }
        return occupiedSeats;
    }

    // Xử lý logic ghế ngồi
    public void addOccupiedSeat(String seat) {
        if (!getOccupiedSeats().contains(seat)) {
            occupiedSeats.add(seat);
        }
    }

    // Chuyển đổi giá tiền từ chuỗi (String) sang số (double) để tính toán
    public double getPriceValue() {
        try {
            String cleanPrice = price.replace(".", "")
                    .replace("đ", "")
                    .replace("k", "000")
                    .replaceAll("[^0-9]", "");
            return Double.parseDouble(cleanPrice);
        } catch (Exception e) {
            return 0;
        }
    }

    /** Ước lượng giờ di chuyển từ chuỗi dạng "08:00 (5h)" hoặc "(1.5h)" */
    public int getDurationHoursEstimate() {
        if (time == null) return 0;
        Matcher m = Pattern.compile("\\((\\d+(?:\\.\\d+)?)h\\)").matcher(time);
        if (m.find()) {
            try {
                return (int) Math.round(Double.parseDouble(m.group(1)));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }
}