package com.example.travelvn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {

    private List<Destination> destinations;
    private Context context;

    public DestinationAdapter(Context context, List<Destination> destinations) {
        this.context = context;
        this.destinations = destinations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_destination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination item = destinations.get(position);

        holder.txtName.setText(item.getName());

        if (item.getImageRes() != 0) {
            holder.imgPhoto.setImageResource(item.getImageRes());
        } else {
            // Dùng ảnh mặc định nếu không tìm thấy resource
            holder.imgPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Ép kiểu context về MainActivity để gọi hàm selectDestination
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                if (item.getName().toLowerCase().contains("voucher") || item.getName().toLowerCase().contains("ưu đãi")) {
                    context.startActivity(new android.content.Intent(context, VoucherActivity.class));
                } else {
                    ((MainActivity) context).selectDestination(item.getName());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (destinations != null) ? destinations.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;
        TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgDestination);
            txtName = itemView.findViewById(R.id.txtDestName);
        }
    }
}