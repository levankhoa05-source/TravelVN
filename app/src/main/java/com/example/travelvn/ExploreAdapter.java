package com.example.travelvn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.VH> {

    public interface OnPickListener {
        void onPick(ExploreDestination d);
    }

    private final List<ExploreDestination> list;
    private final OnPickListener listener;

    public ExploreAdapter(List<ExploreDestination> list, OnPickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_explore_destination, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ExploreDestination d = list.get(position);
        h.name.setText(d.name);
        if (d.imageRes != 0) {
            h.img.setImageResource(d.imageRes);
        }
        h.itemView.setOnClickListener(v -> listener.onPick(d));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView name;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgExplore);
            name = v.findViewById(R.id.tvExploreName);
        }
    }
}
