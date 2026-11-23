package com.example.travelvn;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    private final Context context;
    private final List<Trip> tripList;

    public TripAdapter(Context context, List<Trip> tripList) {
        this.context = context;
        this.tripList = tripList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = tripList.get(position);
        holder.tvTime.setText(trip.getTime());
        holder.tvPath.setText(trip.getPath());
        holder.tvType.setText(trip.getType() + " • " + trip.getDate());

        int dh = trip.getDurationHoursEstimate();
        holder.tvDuration.setText(dh > 0 ? ("~ " + dh + " giờ") : "—");

        double base = trip.getPriceValue();
        Voucher voc = LocalStore.getAppliedVoucher(context);
        boolean discounted = voc != null && voc.appliesToTripType(trip.getType()) && voc.discountPercent > 0;
        if (discounted) {
            double fin = voc.discountedPrice(base);
            holder.tvPriceOriginal.setVisibility(View.VISIBLE);
            holder.tvPriceOriginal.setText(MoneyUtils.formatVndDots(base));
            holder.tvPriceOriginal.setPaintFlags(holder.tvPriceOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvPrice.setText(MoneyUtils.formatVndDots(fin));
            holder.tvPrice.setTextColor(ContextCompat.getColor(context, R.color.vivu_blue));
        } else {
            holder.tvPriceOriginal.setVisibility(View.GONE);
            holder.tvPrice.setText(trip.getPrice());
            holder.tvPrice.setTextColor(ContextCompat.getColor(context, R.color.vivu_blue));
        }

        if (trip.getType().equals("Máy bay")) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_send);
        } else if (trip.getType().equals("Tàu hỏa")) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_directions);
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_mapmode);
        }

        holder.btnSelect.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, TripDetailsActivity.class);
            intent.putExtra("TRIP_ID", trip.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvPath, tvPrice, tvType, tvDuration, tvPriceOriginal;
        ImageView ivIcon;
        Button btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTripTime);
            tvPath = itemView.findViewById(R.id.tvTripPath);
            tvPrice = itemView.findViewById(R.id.tvTripPrice);
            tvPriceOriginal = itemView.findViewById(R.id.tvTripPriceOriginal);
            tvType = itemView.findViewById(R.id.tvTripType);
            tvDuration = itemView.findViewById(R.id.tvTripDuration);
            ivIcon = itemView.findViewById(R.id.ivTripTypeIcon);
            btnSelect = itemView.findViewById(R.id.btnSelectTrip);
        }
    }
}
