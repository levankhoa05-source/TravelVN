package com.example.travelvn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View; // Sửa lỗi: Cannot resolve symbol 'View'
import android.view.ViewGroup;
import android.widget.Button; // Sửa lỗi: Cannot resolve symbol 'Button'
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProviderDashboardActivity extends AppCompatActivity {

    private String providerEmail;
    private Provider currentProvider;
    private final List<Trip> providerTrips = new ArrayList<>();
    private ProviderTripAdapter providerTripAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_dashboard);

        providerEmail = LocalStore.getCurrentUser(this);
        if (providerEmail == null) {
            finish();
            return;
        }

        currentProvider = resolveProvider(providerEmail);
        if (currentProvider == null) {
            Toast.makeText(this, R.string.provider_resolve_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        updateStats();

        // Quản lý danh sách chuyến đi của nhà xe
        RecyclerView rvTrips = findViewById(R.id.rvProviderTrips);
        if (rvTrips != null) {
            rvTrips.setLayoutManager(new LinearLayoutManager(this));
            providerTripAdapter = new ProviderTripAdapter(providerTrips);
            rvTrips.setAdapter(providerTripAdapter);
            loadProviderTrips();
        }

        // Quản lý danh sách vé đã bán
        RecyclerView rvTickets = findViewById(R.id.rvSoldTickets);
        if (rvTickets != null) {
            rvTickets.setLayoutManager(new LinearLayoutManager(this));
            List<Booking> providerBookings = new ArrayList<>();
            for (Booking b : LocalStore.getAllBookings()) {
                if (b.tripName != null && b.tripName.contains(currentProvider.name)) {
                    providerBookings.add(b);
                }
            }
            rvTickets.setAdapter(new ProviderTicketAdapter(providerBookings));
        }

        // Xử lý gửi yêu cầu thêm chuyến đi mới
        Button btnAddTrip = findViewById(R.id.btnAddTrip);
        if (btnAddTrip != null) {
            btnAddTrip.setOnClickListener(v -> {
                // SỬA LỖI: Truyền đúng 8 tham số khớp với Constructor của Trip.java
                Trip newTrip = new Trip(
                        "T-" + System.currentTimeMillis(), // 1. id
                        "20:00 (10h)",                      // 2. time
                        "Sài Gòn → Hà Nội",                 // 3. path
                        "1.500.000",                        // 4. price
                        "Xe khách",                         // 5. type
                        "2026-04-15",                       // 6. date
                        currentProvider.name,               // 7. provider
                        40                                  // 8. seats
                );

                LocalStore.addRequest(this, new TripRequest(newTrip, "ADD", providerEmail));
                Toast.makeText(this, "Yêu cầu thêm chuyến đã gửi tới Admin", Toast.LENGTH_LONG).show();
            });
        }

        // Nút đăng xuất
        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                LocalStore.logout(this);
                finish();
            });
        }
    }

    private void loadProviderTrips() {
        providerTrips.clear();
        fillProviderTrips(LocalStore.getAllTrips());
        providerTripAdapter.notifyDataSetChanged();
    }

    private void fillProviderTrips(List<Trip> source) {
        if (source == null) return;
        for (Trip t : source) {
            if (t != null && t.getProviderName() != null
                    && t.getProviderName().equalsIgnoreCase(currentProvider.name)) {
                providerTrips.add(t);
            }
        }
    }

    /**
     * Khớp email đăng nhập với bảng đối tác; nếu user nhập "partner@gmail.com" vẫn map được nhà xe mẫu.
     */
    private Provider resolveProvider(String email) {
        if (email == null) return null;
        for (Provider p : LocalStore.getProviders()) {
            if (p.email != null && p.email.equalsIgnoreCase(email)) {
                return p;
            }
        }
        String lower = email.toLowerCase();
        if (!lower.startsWith("partner")) {
            return null;
        }
        List<Provider> pl = LocalStore.getProviders();
        if (pl.isEmpty()) {
            return new Provider(email, "Đối tác", 4.5, 0, 0);
        }
        if (lower.contains("3") || lower.contains("vietjet") || lower.contains("air")) {
            return pl.get(Math.min(2, pl.size() - 1));
        }
        if (lower.contains("2") || lower.contains("buoi") || lower.contains("thanh")) {
            return pl.get(Math.min(1, pl.size() - 1));
        }
        return pl.get(0);
    }

    private void updateStats() {
        if (currentProvider != null) {
            TextView tvTickets = findViewById(R.id.tvTotalTickets);
            TextView tvRevenue = findViewById(R.id.tvTotalRevenue);

            if (tvTickets != null) tvTickets.setText(String.valueOf(currentProvider.ticketsSold));
            if (tvRevenue != null) tvRevenue.setText(String.format("%,.0f đ", currentProvider.revenue));
        }
    }

    // Adapter hiển thị danh sách chuyến đi của nhà xe
    class ProviderTripAdapter extends RecyclerView.Adapter<ProviderTripAdapter.VH> {
        List<Trip> list;
        ProviderTripAdapter(List<Trip> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Trip t = list.get(position);
            holder.t1.setText(t.getPath());
            holder.t2.setText(t.getTime() + " | " + t.getPrice());
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;
            VH(View v) {
                super(v);
                t1 = v.findViewById(android.R.id.text1);
                t2 = v.findViewById(android.R.id.text2);
            }
        }
    }

    // Adapter hiển thị danh sách vé đã được đặt
    class ProviderTicketAdapter extends RecyclerView.Adapter<ProviderTicketAdapter.VH> {
        List<Booking> list;
        ProviderTicketAdapter(List<Booking> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Booking b = list.get(position);
            holder.t1.setText(b.customerName + " - Ghế: " + b.seat);
            holder.t2.setText(b.phone + " | " + b.date);
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;
            VH(View v) {
                super(v);
                t1 = v.findViewById(android.R.id.text1);
                t2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}