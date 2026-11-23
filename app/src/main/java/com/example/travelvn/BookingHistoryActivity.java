package com.example.travelvn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookingHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);

        TextView title = findViewById(R.id.tvListTitle);
        title.setText("Lịch sử giao dịch");

        RecyclerView rv = findViewById(R.id.rvListItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new BookingAdapter(LocalStore.getAllBookings()));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}

class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.VH> {
    List<Booking> list;
    BookingAdapter(List<Booking> list) { this.list = list; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        Booking item = list.get(position);
        holder.trip.setText(item.tripName);
        holder.customer.setText(item.customerName);
        holder.seat.setText("Ghế: " + item.seat);
        holder.price.setText(String.format("%,.0f đ", item.price));
        holder.date.setText(item.date);
        holder.status.setText(item.status);
    }

    @Override public int getItemCount() { return list.size(); }

    class VH extends RecyclerView.ViewHolder {
        TextView trip, customer, seat, price, date, status;
        VH(View v) {
            super(v);
            trip = v.findViewById(R.id.tvBookingTrip);
            customer = v.findViewById(R.id.tvBookingCustomer);
            seat = v.findViewById(R.id.tvBookingSeat);
            price = v.findViewById(R.id.tvBookingPrice);
            date = v.findViewById(R.id.tvBookingDate);
            status = v.findViewById(R.id.tvBookingStatus);
        }
    }
}
