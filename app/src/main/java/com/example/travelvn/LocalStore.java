package com.example.travelvn;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocalStore {
    private static final int TARGET_SEED_COUNT = 100;
    private static final String PREF_NAME = "TravelVN_Prefs";
    private static final String KEY_USER = "current_user";
    private static final String KEY_BOOKINGS = "all_bookings";
    private static final String KEY_TRIPS = "all_trips";
    private static final String KEY_PROVIDERS = "all_providers";
    private static final String KEY_REQUESTS = "all_requests";
    private static final String KEY_MESSAGES = "all_messages";
    private static final String KEY_VOUCHERS = "all_vouchers";
    private static final String KEY_HOTELS = "all_hotels";
    private static final String KEY_DESTINATIONS = "all_destinations";
    private static final String KEY_USERS = "all_users";
    private static final String KEY_APPLIED_VOUCHER = "applied_voucher_code";

    private static List<Trip> allTrips = new ArrayList<>();
    private static List<Booking> allBookings = new ArrayList<>();
    private static List<TripRequest> tripRequests = new ArrayList<>();
    private static List<Provider> providers = new ArrayList<>();
    private static List<Voucher> vouchers = new ArrayList<>();
    private static List<Hotel> hotels = new ArrayList<>();
    private static List<Message> messages = new ArrayList<>();
    private static List<Destination> destinations = new ArrayList<>();
    private static List<User> users = new ArrayList<>();

    private static final Gson gson = new Gson();

    public static void init(Context context) {
        loadData(context);
        if (providers.isEmpty() && allTrips.isEmpty()) {
            seedData();
            saveData(context);
        }
        ensureRichTripsAndVouchers(context);
        ensureLargeCatalog(context);
        migrateLegacyVouchers();
        if (needsSaveAfterMigrate) {
            saveData(context);
            needsSaveAfterMigrate = false;
        }
    }

    private static boolean needsSaveAfterMigrate = false;

    private static void migrateLegacyVouchers() {
        for (Voucher v : vouchers) {
            if (v == null || v.code == null) continue;
            if (v.appliesToType == null || v.appliesToType.isEmpty()) {
                v.appliesToType = "ALL";
                needsSaveAfterMigrate = true;
            }
            if (v.discountPercent <= 0 && "FLY20".equalsIgnoreCase(v.code)) {
                v.discountPercent = 20;
                v.appliesToType = "Máy bay";
                needsSaveAfterMigrate = true;
            }
        }
    }

    /** Bổ sung chuyến / voucher mẫu nếu thiếu (không xóa dữ liệu người dùng). */
    private static void ensureRichTripsAndVouchers(Context context) {
        boolean changed = false;
        Set<String> ids = new HashSet<>();
        for (Trip t : allTrips) {
            if (t.getId() != null) ids.add(t.getId());
        }
        for (Trip t : catalogTrips()) {
            if (!ids.contains(t.getId())) {
                allTrips.add(t);
                ids.add(t.getId());
                changed = true;
            }
        }
        Set<String> codes = new HashSet<>();
        for (Voucher v : vouchers) {
            if (v != null && v.code != null) codes.add(v.code.toUpperCase(Locale.ROOT));
        }
        for (Voucher v : catalogVouchers()) {
            if (v.code == null) continue;
            String c = v.code.toUpperCase(Locale.ROOT);
            if (!codes.contains(c)) {
                vouchers.add(v);
                codes.add(c);
                changed = true;
            }
        }
        if (changed) {
            saveData(context);
        }
    }

    /** Đảm bảo dữ liệu local đủ lớn để seed Firebase cho chế độ online/offline-first. */
    private static void ensureLargeCatalog(Context context) {
        boolean changed = false;

        if (allTrips.size() < TARGET_SEED_COUNT) {
            Set<String> ids = new HashSet<>();
            for (Trip t : allTrips) {
                if (t != null && t.getId() != null) ids.add(t.getId());
            }
            for (Trip t : catalogTrips()) {
                if (t == null || t.getId() == null || ids.contains(t.getId())) continue;
                allTrips.add(t);
                ids.add(t.getId());
                changed = true;
                if (allTrips.size() >= TARGET_SEED_COUNT) break;
            }
        }

        if (vouchers.size() < TARGET_SEED_COUNT) {
            Set<String> codes = new HashSet<>();
            for (Voucher v : vouchers) {
                if (v != null && v.code != null) codes.add(v.code.toUpperCase(Locale.ROOT));
            }
            for (Voucher v : catalogVouchers()) {
                if (v == null || v.code == null) continue;
                String c = v.code.toUpperCase(Locale.ROOT);
                if (codes.contains(c)) continue;
                vouchers.add(v);
                codes.add(c);
                changed = true;
                if (vouchers.size() >= TARGET_SEED_COUNT) break;
            }
        }

        if (hotels.size() < TARGET_SEED_COUNT) {
            Set<String> names = new HashSet<>();
            for (Hotel h : hotels) {
                if (h != null && h.name != null) names.add(h.name.toUpperCase(Locale.ROOT));
            }
            for (Hotel h : catalogHotels()) {
                if (h == null || h.name == null) continue;
                String n = h.name.toUpperCase(Locale.ROOT);
                if (names.contains(n)) continue;
                hotels.add(h);
                names.add(n);
                changed = true;
                if (hotels.size() >= TARGET_SEED_COUNT) break;
            }
        }

        if (allBookings.size() < TARGET_SEED_COUNT) {
            for (Booking b : catalogBookings(allTrips, catalogUsers())) {
                if (allBookings.size() >= TARGET_SEED_COUNT) break;
                allBookings.add(b);
                changed = true;
            }
        }

        if (messages.size() < TARGET_SEED_COUNT) {
            for (Message m : catalogMessages(catalogUsers())) {
                if (messages.size() >= TARGET_SEED_COUNT) break;
                messages.add(m);
                changed = true;
            }
        }

        if (changed) {
            saveData(context);
        }
    }

    private static List<Trip> catalogTrips() {
        List<Trip> list = new ArrayList<>();
        list.add(new Trip("PT-001", "08:00 (5h)", "TP. Hồ Chí Minh → Đà Lạt", "250.000", "Xe khách", "2026-04-05", "Phương Trang", 30));
        list.add(new Trip("VJ-123", "10:00 (2h)", "TP. Hồ Chí Minh → Hà Nội", "1.200.000", "Máy bay", "2026-04-05", "VietJet Air", 180));
        list.add(new Trip("TB-204", "21:00 (8h)", "TP. Hồ Chí Minh → Đà Lạt", "220.000", "Xe khách", "2026-04-06", "Thành Bưởi", 40));
        list.add(new Trip("PT-002", "19:30 (6h)", "Hà Nội → Đà Nẵng", "380.000", "Xe khách", "2026-04-07", "Phương Trang", 34));
        list.add(new Trip("VJ-456", "14:00 (1h)", "Đà Nẵng → Hà Nội", "990.000", "Máy bay", "2026-04-08", "VietJet Air", 186));
        list.add(new Trip("SE-01", "07:00 (16h)", "Hà Nội → TP. Hồ Chí Minh", "850.000", "Tàu hỏa", "2026-04-09", "Đường sắt VN", 200));
        list.add(new Trip("PT-003", "06:00 (9h)", "TP. Hồ Chí Minh → Nha Trang", "310.000", "Xe khách", "2026-04-10", "Phương Trang", 32));
        list.add(new Trip("TB-301", "08:30 (11h)", "TP. Hồ Chí Minh → Đà Nẵng", "420.000", "Xe khách", "2026-04-11", "Thành Bưởi", 36));
        list.add(new Trip("VJ-789", "16:00 (2h)", "TP. Hồ Chí Minh → Phú Quốc", "1.050.000", "Máy bay", "2026-04-12", "VietJet Air", 220));
        list.add(new Trip("PT-004", "22:00 (7h)", "Đà Lạt → TP. Hồ Chí Minh", "240.000", "Xe khách", "2026-04-13", "Phương Trang", 28));
        list.add(new Trip("TB-102", "13:00 (3h)", "Huế → Đà Nẵng", "120.000", "Xe khách", "2026-04-14", "Thành Bưởi", 25));
        list.add(new Trip("VJ-111", "09:00 (1h)", "Cần Thơ → TP. Hồ Chí Minh", "650.000", "Máy bay", "2026-04-15", "VietJet Air", 150));
        list.add(new Trip("SE-02", "20:00 (12h)", "Nha Trang → Huế", "520.000", "Tàu hỏa", "2026-04-16", "Đường sắt VN", 160));
        list.add(new Trip("PT-005", "11:00 (4h)", "Đà Nẵng → Quy Nhơn", "195.000", "Xe khách", "2026-04-17", "Phương Trang", 30));

        String[] routes = {
                "Hà Nội → Đà Nẵng", "TP. Hồ Chí Minh → Nha Trang", "Đà Nẵng → Huế",
                "Đà Lạt → TP. Hồ Chí Minh", "Hà Nội → Hải Phòng", "TP. Hồ Chí Minh → Phú Quốc",
                "Cần Thơ → Hà Nội", "Quy Nhơn → TP. Hồ Chí Minh", "Nha Trang → Hà Nội",
                "Huế → Đà Nẵng"
        };
        String[] providersPool = {"Phương Trang", "Thành Bưởi", "VietJet Air", "Đường sắt VN", "TravelVN Express"};
        String[] types = {"Xe khách", "Máy bay", "Tàu hỏa"};
        int[] seatsPool = {28, 30, 32, 36, 40, 160, 180, 200};
        int id = 6;
        while (list.size() < TARGET_SEED_COUNT) {
            int i = list.size();
            String type = types[i % types.length];
            String provider = providersPool[i % providersPool.length];
            String route = routes[i % routes.length];
            int hours = 2 + (i % 15);
            int hourStart = 5 + (i % 17);
            String time = String.format(Locale.ROOT, "%02d:%02d (%dh)", hourStart % 24, (i * 7) % 60, hours);
            String date = String.format(Locale.ROOT, "2026-05-%02d", 1 + (i % 28));
            int priceVal = 150000 + (i * 35000);
            String price = String.format(Locale.ROOT, "%,d", priceVal).replace(",", ".");
            int seats = seatsPool[i % seatsPool.length];
            String tripId = String.format(Locale.ROOT, "AUTO-%03d", id++);
            Trip t = new Trip(tripId, time, route, price, type, date, provider, seats);
            t.addTimeline("Check-in", "Làm thủ tục tại bến/ga/sân bay");
            t.addTimeline("Khởi hành", "Bắt đầu hành trình");
            t.addTimeline("Kết thúc", "Đến điểm đến");
            list.add(t);
        }

        for (Trip t : list) {
            if (t.getTimeline().isEmpty()) {
                t.addTimeline("Khởi hành", "Xuất phát theo giờ trên vé");
                t.addTimeline("Đến nơi", "Kết thúc hành trình");
            }
        }
        return list;
    }

    /** Điểm đến mẫu (đồng bộ với MainActivity.setupDestinationsSection). */
    public static List<Destination> catalogDestinations() {
        List<Destination> list = new ArrayList<>();
        list.add(new Destination("Đà Nẵng", R.drawable.img_danang));
        list.add(new Destination("Đà Lạt", R.drawable.img_dalat));
        list.add(new Destination("Hội An", R.drawable.img_hoian));
        list.add(new Destination("Phú Quốc", R.drawable.img_phuquoc));
        String[] provinces = ProvinceData.getList();
        int i = 0;
        while (list.size() < TARGET_SEED_COUNT) {
            String name = provinces[i % provinces.length] + " - Gợi ý " + (i + 1);
            int img = (i % 4 == 0) ? R.drawable.img_danang
                    : (i % 4 == 1) ? R.drawable.img_dalat
                    : (i % 4 == 2) ? R.drawable.img_hoian
                    : R.drawable.img_phuquoc;
            list.add(new Destination(name, img));
            i++;
        }
        return list;
    }

    /** Tài khoản mẫu để seed collection users. */
    public static List<User> catalogUsers() {
        List<User> u = new ArrayList<>();
        u.add(new User("admin@gmail.com", "Quản trị", "admin"));
        u.add(new User("partner1@gmail.com", "Phương Trang", "partner"));
        u.add(new User("partner2@gmail.com", "Thành Bưởi", "partner"));
        u.add(new User("partner3@gmail.com", "VietJet Air", "partner"));
        u.add(new User("support@travelvn.com", "Hỗ trợ TravelVN", "support"));
        u.add(new User("guest@travelvn.local", "Khách", "guest"));
        for (int i = 1; i <= TARGET_SEED_COUNT - 6; i++) {
            u.add(new User(
                    String.format(Locale.ROOT, "user%03d@travelvn.com", i),
                    String.format(Locale.ROOT, "Khách hàng %03d", i),
                    "user"
            ));
        }
        return u;
    }

    private static List<Voucher> catalogVouchers() {
        List<Voucher> v = new ArrayList<>();
        v.add(new Voucher("FLY20", "Giảm 20% vé máy bay", "Áp dụng chuyến bay nội địa", "30/04/2026", 20, "Máy bay"));
        v.add(new Voucher("BUS15", "Giảm 15% vé xe khách", "Cho mọi tuyến xe liên tỉnh", "31/05/2026", 15, "Xe khách"));
        v.add(new Voucher("TRAIN10", "Giảm 10% vé tàu", "Tàu hỏa Thống Nhất", "30/06/2026", 10, "Tàu hỏa"));
        v.add(new Voucher("SUMMER10", "Giảm 10% mọi phương tiện", "Ưu đãi mùa hè", "15/08/2026", 10, "ALL"));
        v.add(new Voucher("NEWUSER5", "Giảm 5% chào mừng", "Khách mới — tất cả loại vé", "31/12/2026", 5, "ALL"));
        v.add(new Voucher("VIP12", "Giảm 12% vé cao cấp", "Xe giường nằm / hạng business", "01/07/2026", 12, "Xe khách"));
        String[] applies = {"ALL", "Máy bay", "Xe khách", "Tàu hỏa"};
        for (int i = 1; i <= TARGET_SEED_COUNT - 6; i++) {
            int percent = 5 + (i % 26);
            v.add(new Voucher(
                    String.format(Locale.ROOT, "AUTO%03d", i),
                    String.format(Locale.ROOT, "Ưu đãi %d%% - Mã %03d", percent, i),
                    "Giảm giá tự động cho khách đặt vé online",
                    String.format(Locale.ROOT, "%02d/12/2026", (i % 28) + 1),
                    percent,
                    applies[i % applies.length]
            ));
        }
        return v;
    }

    private static List<Hotel> catalogHotels() {
        List<Hotel> list = new ArrayList<>();
        String[] cities = ProvinceData.getList();
        for (int i = 1; i <= TARGET_SEED_COUNT; i++) {
            String city = cities[(i - 1) % cities.length];
            int img = (i % 4 == 0) ? R.drawable.img_danang
                    : (i % 4 == 1) ? R.drawable.img_dalat
                    : (i % 4 == 2) ? R.drawable.img_hoian
                    : R.drawable.img_phuquoc;
            list.add(new Hotel(
                    String.format(Locale.ROOT, "TravelVN Hotel %03d", i),
                    String.format(Locale.ROOT, "%d Đường Trung Tâm, %s", 10 + i, city),
                    350000 + (i * 12000L),
                    3.8 + ((i % 13) * 0.1),
                    img
            ));
        }
        return list;
    }

    private static List<Booking> catalogBookings(List<Trip> tripSource, List<User> userSource) {
        List<Booking> list = new ArrayList<>();
        if (tripSource == null || tripSource.isEmpty()) return list;
        if (userSource == null || userSource.isEmpty()) userSource = catalogUsers();

        for (int i = 1; i <= TARGET_SEED_COUNT; i++) {
            Trip t = tripSource.get((i - 1) % tripSource.size());
            User u = userSource.get((i - 1) % userSource.size());
            String customer = u.displayName == null || u.displayName.isEmpty() ? ("Khách " + i) : u.displayName;
            String phone = String.format(Locale.ROOT, "0%09d", 100000000 + i);
            String seat = String.format(Locale.ROOT, "G%02d", 1 + (i % 30));
            String date = String.format(Locale.ROOT, "%02d/05/2026 1%01d:%02d:00", (i % 28) + 1, i % 9, i % 60);
            Booking b = new Booking(t.getName(), customer, phone, seat, "PAID", t.getPriceValue(), date);
            list.add(b);
        }
        return list;
    }

    private static List<Message> catalogMessages(List<User> users) {
        List<Message> list = new ArrayList<>();
        if (users == null || users.isEmpty()) users = catalogUsers();
        for (int i = 1; i <= TARGET_SEED_COUNT; i++) {
            User from = users.get((i - 1) % users.size());
            User to = users.get((i + 5) % users.size());
            String fromId = from.email == null ? ("from" + i + "@travelvn.com") : from.email;
            String toId = to.email == null ? ("to" + i + "@travelvn.com") : to.email;
            Message m = new Message(fromId, toId, "Xin chao, toi muon xac nhan lich trinh chuyen " + i);
            m.timestamp = System.currentTimeMillis() - (long) i * 60_000L;
            list.add(m);
        }
        return list;
    }

    private static void seedData() {
        providers.add(new Provider("partner1@gmail.com", "Phương Trang", 4.5, 15000000, 120));
        providers.add(new Provider("partner2@gmail.com", "Thành Bưởi", 4.8, 22000000, 180));
        providers.add(new Provider("partner3@gmail.com", "VietJet Air", 4.2, 50000000, 300));

        allTrips.addAll(catalogTrips());
        vouchers.addAll(catalogVouchers());
        hotels.addAll(catalogHotels());
        destinations.addAll(catalogDestinations());
        users.addAll(catalogUsers());
        allBookings.addAll(catalogBookings(allTrips, catalogUsers()));
        messages.addAll(catalogMessages(catalogUsers()));
    }

    public static void saveData(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_BOOKINGS, gson.toJson(allBookings));
        editor.putString(KEY_TRIPS, gson.toJson(allTrips));
        editor.putString(KEY_PROVIDERS, gson.toJson(providers));
        editor.putString(KEY_REQUESTS, gson.toJson(tripRequests));
        editor.putString(KEY_MESSAGES, gson.toJson(messages));
        editor.putString(KEY_VOUCHERS, gson.toJson(vouchers));
        editor.putString(KEY_HOTELS, gson.toJson(hotels));
        editor.putString(KEY_DESTINATIONS, gson.toJson(destinations));
        editor.putString(KEY_USERS, gson.toJson(users));
        editor.apply();
    }

    private static void loadData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        allBookings = loadList(prefs, KEY_BOOKINGS, new TypeToken<ArrayList<Booking>>(){}.getType());
        allTrips = loadList(prefs, KEY_TRIPS, new TypeToken<ArrayList<Trip>>(){}.getType());
        providers = loadList(prefs, KEY_PROVIDERS, new TypeToken<ArrayList<Provider>>(){}.getType());
        tripRequests = loadList(prefs, KEY_REQUESTS, new TypeToken<ArrayList<TripRequest>>(){}.getType());
        messages = loadList(prefs, KEY_MESSAGES, new TypeToken<ArrayList<Message>>(){}.getType());
        vouchers = loadList(prefs, KEY_VOUCHERS, new TypeToken<ArrayList<Voucher>>(){}.getType());
        hotels = loadList(prefs, KEY_HOTELS, new TypeToken<ArrayList<Hotel>>(){}.getType());
        destinations = loadList(prefs, KEY_DESTINATIONS, new TypeToken<ArrayList<Destination>>(){}.getType());
        users = loadList(prefs, KEY_USERS, new TypeToken<ArrayList<User>>(){}.getType());
    }

    private static <T> List<T> loadList(SharedPreferences prefs, String key, Type type) {
        String json = prefs.getString(key, null);
        return (json == null) ? new ArrayList<T>() : gson.fromJson(json, type);
    }

    public static void setCurrentUser(Context context, String email) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_USER, email).apply();
    }
    public static String getCurrentUser(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_USER, null);
    }
    public static void logout(Context context) {
        setCurrentUser(context, null);
        FirebaseAuth.getInstance().signOut();
    }

    public static List<Trip> getAllTrips() { return allTrips; }
    public static List<Booking> getAllBookings() { return allBookings; }
    public static List<TripRequest> getTripRequests() { return tripRequests; }
    public static List<Provider> getProviders() { return providers; }
    public static List<Voucher> getVouchers() { return vouchers; }
    public static List<Hotel> getHotels() { return hotels; }
    public static List<Message> getMessages() { return messages; }
    public static List<Destination> getDestinations() { return destinations; }
    public static List<User> getUsers() { return users; }

    public static void replaceTrips(Context context, List<Trip> list) {
        allTrips = list == null ? new ArrayList<>() : new ArrayList<>(list);
        saveData(context);
    }

    public static void replaceHotels(Context context, List<Hotel> list) {
        hotels = list == null ? new ArrayList<>() : new ArrayList<>(list);
        saveData(context);
    }

    public static void replaceVouchers(Context context, List<Voucher> list) {
        vouchers = list == null ? new ArrayList<>() : new ArrayList<>(list);
        saveData(context);
    }

    public static void replaceBookings(Context context, List<Booking> list) {
        allBookings = list == null ? new ArrayList<>() : new ArrayList<>(list);
        saveData(context);
    }

    public static void replaceMessages(Context context, List<Message> list) {
        messages = list == null ? new ArrayList<>() : new ArrayList<>(list);
        saveData(context);
    }

    public static void replaceDestinations(Context context, List<Destination> list) {
        destinations = list == null ? new ArrayList<>() : new ArrayList<>(list);
        saveData(context);
    }

    public static void replaceUsers(Context context, List<User> list) {
        users = list == null ? new ArrayList<>() : new ArrayList<>(list);
        saveData(context);
    }

    public static void addTrip(Context context, Trip trip) {
        allTrips.add(trip);
        saveData(context);
        if (trip != null) {
            String docId = (trip.getId() != null && !trip.getId().trim().isEmpty())
                    ? trip.getId()
                    : ("TRIP-" + System.currentTimeMillis());
            FirebaseFirestore.getInstance().collection("trips").document(docId).set(trip);
        }
    }
    public static void addBooking(Context context, Booking booking) {
        allBookings.add(booking);
        saveData(context);
        if (booking != null) {
            String docId = (booking.qrData != null && !booking.qrData.trim().isEmpty())
                    ? String.valueOf(Math.abs(booking.qrData.hashCode()))
                    : ("BOOK-" + System.currentTimeMillis());
            FirebaseFirestore.getInstance().collection("bookings").document(docId).set(booking);
        }
    }

    public static void addRequest(Context context, TripRequest request) {
        tripRequests.add(request);
        saveData(context);
        if (request != null) {
            String reqId = "REQ-" + System.currentTimeMillis();
            FirebaseFirestore.getInstance().collection("trip_requests").document(reqId).set(request);
        }
    }

    public static void sendMessage(Context context, Message msg) {
        messages.add(msg);
        saveData(context);
        if (msg != null) {
            String docId = "MSG-" + msg.timestamp + "-" + Math.abs((msg.from + "_" + msg.to).hashCode());
            FirebaseFirestore.getInstance().collection("messages").document(docId).set(msg);
        }
    }

    public static boolean isAdminEmail(String email) {
        return email != null && email.equalsIgnoreCase("admin@gmail.com");
    }

    public static boolean isPartnerEmail(String email) {
        return email != null && email.toLowerCase().startsWith("partner");
    }

    public static void setAppliedVoucherCode(Context context, String code) {
        if (code == null || code.trim().isEmpty()) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().remove(KEY_APPLIED_VOUCHER).apply();
        } else {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_APPLIED_VOUCHER, code.trim()).apply();
        }
    }

    public static String getAppliedVoucherCode(Context context) {
        String c = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_APPLIED_VOUCHER, null);
        return (c == null || c.isEmpty()) ? null : c;
    }

    public static Voucher findVoucherByCode(String code) {
        if (code == null) return null;
        for (Voucher v : vouchers) {
            if (v != null && v.code != null && v.code.equalsIgnoreCase(code.trim())) {
                return v;
            }
        }
        return null;
    }

    public static Voucher getAppliedVoucher(Context context) {
        return findVoucherByCode(getAppliedVoucherCode(context));
    }

    public static void clearAppliedVoucher(Context context) {
        setAppliedVoucherCode(context, null);
    }

    /** Giá sau voucher (nếu có và khớp loại chuyến), ngược lại giá gốc */
    public static double finalPriceForTrip(Context context, Trip trip) {
        if (trip == null) return 0;
        double base = trip.getPriceValue();
        Voucher v = getAppliedVoucher(context);
        if (v != null && v.appliesToTripType(trip.getType())) {
            return v.discountedPrice(base);
        }
        return base;
    }
}
