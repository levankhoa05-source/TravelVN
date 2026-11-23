package com.example.travelvn;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HotelListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);

        TextView title = findViewById(R.id.tvListTitle);
        title.setText("Khách sạn phù hợp");

        RecyclerView rv = findViewById(R.id.rvListItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new HotelAdapter(LocalStore.getHotels()));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}

class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.VH> {
    List<Hotel> list;
    HotelAdapter(List<Hotel> list) { this.list = list; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        Hotel item = list.get(position);
        holder.name.setText(item.name);
        holder.address.setText(item.address);
        holder.price.setText(String.format("%,.0f đ", item.price));
        holder.rating.setText(String.valueOf(item.rating));
        holder.img.setImageResource(item.imageRes);
    }

    @Override public int getItemCount() { return list.size(); }

    class VH extends RecyclerView.ViewHolder {
        TextView name, address, price, rating;
        ImageView img;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tvHotelName);
            address = v.findViewById(R.id.tvHotelAddress);
            price = v.findViewById(R.id.tvHotelPrice);
            rating = v.findViewById(R.id.tvHotelRating);
            img = v.findViewById(R.id.imgHotel);
        }
    }
}
