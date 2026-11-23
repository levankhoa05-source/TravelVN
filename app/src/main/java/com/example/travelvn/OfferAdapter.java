package com.example.travelvn;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.VH> {

    public static class OfferItem {
        public final String title;
        public final String subtitle;
        public final int imageRes;

        public OfferItem(String title, String subtitle, int imageRes) {
            this.title = title;
            this.subtitle = subtitle;
            this.imageRes = imageRes;
        }
    }

    private final Context context;
    private final List<OfferItem> items;

    public OfferAdapter(Context context, List<OfferItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OfferItem o = items.get(position);
        h.title.setText(o.title);
        h.sub.setText(o.subtitle);
        if (o.imageRes != 0) {
            h.img.setImageResource(o.imageRes);
        }
        h.itemView.setOnClickListener(v -> context.startActivity(new Intent(context, VoucherActivity.class)));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView title;
        final TextView sub;

        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgOffer);
            title = itemView.findViewById(R.id.tvOfferTitle);
            sub = itemView.findViewById(R.id.tvOfferSub);
        }
    }
}
