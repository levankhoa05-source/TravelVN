package com.example.travelvn;

import java.util.Locale;

public final class MoneyUtils {

    private MoneyUtils() {
    }

    /** Định dạng kiểu 1.250.000 đ */
    public static String formatVndDots(double amount) {
        long v = Math.round(amount);
        String s = String.format(Locale.US, "%d", v);
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                sb.append('.');
            }
            sb.append(s.charAt(i));
        }
        return sb + " đ";
    }
}
