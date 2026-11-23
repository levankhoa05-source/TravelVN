package com.example.travelvn;

import android.content.Intent; // Thêm dòng này để sửa lỗi 'Intent'
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

// Đảm bảo bạn đã có các class này trong project
// import com.example.travelvn.LocalStore;
// import com.example.travelvn.TripComparisonActivity;

public class TripSearchActivity extends AppCompatActivity {

    private RecyclerView rvTrips;
    private TripAdapter adapter;
    private List<Trip> tripList;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_search);

        // Lấy dữ liệu từ Intent
        String source = getIntent().getStringExtra("SOURCE");
        String destination = getIntent().getStringExtra("DESTINATION");
        type = getIntent().getStringExtra("TYPE");

        if (source == null) source = "";
        if (destination == null) destination = "";
        if (type == null || type.isEmpty()) type = "Tất cả";
        final String finalSource = source;
        final String finalDestination = destination;

        TextView tvTitle = findViewById(R.id.tvSearchTitle);
        if (tvTitle != null) {
            tvTitle.setText(source + " → " + destination);
        }

        rvTrips = findViewById(R.id.rvTrips);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));

        tripList = new ArrayList<>();
        adapter = new TripAdapter(this, tripList);
        rvTrips.setAdapter(adapter);
        loadDataFromCache(finalSource, finalDestination);

        findViewById(R.id.btnCompare).setOnClickListener(v -> {
            if (tripList.size() < 2) {
                Toast.makeText(this, "Cần ít nhất 2 chuyến để so sánh", Toast.LENGTH_SHORT).show();
                return;
            }
            // LƯU Ý: Phải đảm bảo class TripComparisonActivity đã được tạo
            Intent intent = new Intent(TripSearchActivity.this, TripComparisonActivity.class);
            startActivity(intent);
        });

        if (findViewById(R.id.toolbar) != null) {
            findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        }
    }

    private void loadDataFromCache(String source, String destination) {
        tripList.clear();
        List<Trip> loadedTrips = LocalStore.getAllTrips();
        for (Trip trip : loadedTrips) {
            boolean matchesType = type.equals("Tất cả")
                    || (trip.getType() != null && trip.getType().equalsIgnoreCase(type));

            String tripPath = trip.getPath() == null ? "" : trip.getPath();
            String pathLower = tripPath.toLowerCase();
            boolean matchesPath;

            if (source.isEmpty() && destination.isEmpty()) {
                matchesPath = true;
            } else if (source.isEmpty()) {
                matchesPath = pathLower.contains(destination.toLowerCase());
            } else if (destination.isEmpty()) {
                matchesPath = pathLower.contains(source.toLowerCase());
            } else {
                matchesPath = pathLower.contains(source.toLowerCase())
                        && pathLower.contains(destination.toLowerCase());
            }

            if (matchesType && matchesPath) {
                tripList.add(trip);
            }
        }
        adapter.notifyDataSetChanged();
        if (tripList.isEmpty()) {
            Toast.makeText(TripSearchActivity.this, "Không có chuyến nào khớp với tìm kiếm của bạn.", Toast.LENGTH_LONG).show();
        }
    }
}