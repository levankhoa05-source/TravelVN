package com.example.travelvn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout llRequests;
    private RecyclerView rvProviders;
    private ProviderCompareAdapter providerAdapter;
    private List<Provider> providerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Header Stats
        ((TextView) findViewById(R.id.tvTotalSystemRevenue)).setText(String.format("%,.0f đ", getTotalRevenue()));
        ((TextView) findViewById(R.id.tvTotalSystemTickets)).setText(String.valueOf(LocalStore.getAllBookings().size()));
        ((TextView) findViewById(R.id.tvActiveProviders)).setText(String.valueOf(LocalStore.getProviders().size()));

        llRequests = findViewById(R.id.llRequestsContainer);
        refreshRequests();

        setupProviderComparison();

        findViewById(R.id.btnLogout).setOnClickListener(v -> finish());
    }

    private double getTotalRevenue() {
        double total = 0;
        for (Booking b : LocalStore.getAllBookings()) {
            total += b.price;
        }
        return total;
    }

    private void refreshRequests() {
        llRequests.removeAllViews();
        List<TripRequest> requests = LocalStore.getTripRequests();

        boolean hasPending = false;
        for (final TripRequest req : requests) {
            if (req.status.equals("PENDING")) {
                hasPending = true;
                View card = getLayoutInflater().inflate(R.layout.item_request, llRequests, false);
                ((TextView) card.findViewById(R.id.tvRequestTitle)).setText(req.providerEmail);
                ((TextView) card.findViewById(R.id.tvRequestDetails)).setText(req.trip.getPath() + " | " + req.trip.getPrice());
                
                card.findViewById(R.id.btnApprove).setOnClickListener(v -> {
                    req.status = "APPROVED";
                    LocalStore.addTrip(this, req.trip);
                    Toast.makeText(this, "Đã duyệt chuyến đi!", Toast.LENGTH_SHORT).show();
                    refreshRequests();
                });

                card.findViewById(R.id.btnReject).setOnClickListener(v -> {
                    req.status = "REJECTED";
                    LocalStore.saveData(this);
                    Toast.makeText(this, "Đã từ chối chuyến đi!", Toast.LENGTH_SHORT).show();
                    refreshRequests();
                });

                llRequests.addView(card);
            }
        }

        if (!hasPending) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không có yêu cầu chờ duyệt");
            tvEmpty.setPadding(32, 32, 32, 32);
            llRequests.addView(tvEmpty);
        }
    }

    private void setupProviderComparison() {
        rvProviders = findViewById(R.id.rvProviderCompare);
        rvProviders.setLayoutManager(new LinearLayoutManager(this));
        providerList = new ArrayList<>(LocalStore.getProviders());
        providerAdapter = new ProviderCompareAdapter(providerList);
        rvProviders.setAdapter(providerAdapter);

        findViewById(R.id.btnSortRevenue).setOnClickListener(v -> {
            providerList.sort((p1, p2) -> Double.compare(p2.revenue, p1.revenue));
            providerAdapter.notifyDataSetChanged();
        });

        findViewById(R.id.btnSortRating).setOnClickListener(v -> {
            providerList.sort((p1, p2) -> Double.compare(p2.rating, p1.rating));
            providerAdapter.notifyDataSetChanged();
        });
    }

    class ProviderCompareAdapter extends RecyclerView.Adapter<ProviderCompareAdapter.VH> {
        List<Provider> list;
        ProviderCompareAdapter(List<Provider> list) { this.list = list; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider_compare, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull VH holder, int position) {
            Provider p = list.get(position);
            holder.name.setText(p.name);
            holder.revenue.setText(String.format("%,.0f", p.revenue));
            holder.tickets.setText(String.valueOf(p.ticketsSold));
            holder.rating.setText(String.valueOf(p.rating));
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView name, revenue, tickets, rating;
            VH(View v) {
                super(v);
                name = v.findViewById(R.id.tvCompName);
                revenue = v.findViewById(R.id.tvCompRevenue);
                tickets = v.findViewById(R.id.tvCompTickets);
                rating = v.findViewById(R.id.tvCompRating);
            }
        }
    }
}
