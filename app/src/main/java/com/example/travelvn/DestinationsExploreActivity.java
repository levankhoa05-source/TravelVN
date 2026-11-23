package com.example.travelvn;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class DestinationsExploreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_destinations);

        MaterialToolbar tb = findViewById(R.id.toolbarExplore);
        tb.setNavigationOnClickListener(v -> finish());

        String[] provinces = ProvinceData.getList();
        List<ExploreDestination> data = new ArrayList<>();
        for (String p : provinces) {
            data.add(new ExploreDestination(p, imageForProvince(p)));
        }

        RecyclerView rv = findViewById(R.id.rvExplore);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setAdapter(new ExploreAdapter(data, d -> {
            Intent i = new Intent(this, TripSearchActivity.class);
            i.putExtra("SOURCE", "");
            i.putExtra("DESTINATION", d.name);
            i.putExtra("TYPE", "Tất cả");
            startActivity(i);
        }));
    }

    private static int imageForProvince(String name) {
        if (name == null) return R.drawable.bg_travel_header;
        switch (name) {
            case "Đà Nẵng":
                return R.drawable.img_danang;
            case "Đà Lạt":
                return R.drawable.img_dalat;
            case "Hội An":
                return R.drawable.img_hoian;
            case "Phú Quốc":
                return R.drawable.img_phuquoc;
            case "Nha Trang":
                return R.drawable.bg_travel_header;
            case "Hạ Long":
                return R.drawable.bg_travel_header;
            default:
                return R.drawable.bg_travel_header;
        }
    }
}
