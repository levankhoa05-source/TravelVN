package com.example.travelvn;

/**
 * appliesToType: "ALL" hoặc khớp chính xác loại chuyến: "Máy bay", "Xe khách", "Tàu hỏa"
 */
public class Voucher {
    public String code;
    public String title;
    public String description;
    public String expiry;
    /** 1–100: giảm theo % giá vé */
    public int discountPercent;
    /** "ALL" hoặc "Máy bay" / "Xe khách" / "Tàu hỏa" */
    public String appliesToType;

    public Voucher() {
        this.code = "";
        this.title = "";
        this.description = "";
        this.expiry = "";
        this.discountPercent = 0;
        this.appliesToType = "ALL";
    }

    public Voucher(String code, String title, String description, String expiry, int discountPercent, String appliesToType) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.expiry = expiry;
        this.discountPercent = Math.max(0, Math.min(100, discountPercent));
        this.appliesToType = appliesToType == null || appliesToType.isEmpty() ? "ALL" : appliesToType;
    }

    public boolean appliesToTripType(String tripType) {
        if (tripType == null) tripType = "";
        if (appliesToType == null || appliesToType.isEmpty() || "ALL".equalsIgnoreCase(appliesToType)) {
            return true;
        }
        return tripType.trim().equalsIgnoreCase(appliesToType.trim());
    }

    /** Giá sau giảm (làm tròn đồng) */
    public double discountedPrice(double basePrice) {
        if (discountPercent <= 0) return basePrice;
        double p = basePrice * (100 - discountPercent) / 100.0;
        return Math.round(p);
    }

    public String discountLabel() {
        if (discountPercent <= 0) return "";
        return "-" + discountPercent + "%";
    }
}