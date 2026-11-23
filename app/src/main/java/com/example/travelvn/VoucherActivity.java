package com.example.travelvn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class VoucherActivity extends AppCompatActivity {

    private TextView banner;
    private VoucherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vouchers);

        TextView title = findViewById(R.id.tvListTitle);
        title.setText(R.string.voucher_screen_title);

        banner = findViewById(R.id.tvVoucherAppliedBanner);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnClearVoucher).setOnClickListener(v -> {
            LocalStore.clearAppliedVoucher(this);
            updateBanner();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            Toast.makeText(this, R.string.voucher_cleared_toast, Toast.LENGTH_SHORT).show();
        });

        RecyclerView rv = findViewById(R.id.rvListItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VoucherAdapter(LocalStore.getVouchers());
        rv.setAdapter(adapter);
        updateBanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBanner();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void updateBanner() {
        Voucher v = LocalStore.getAppliedVoucher(this);
        if (v == null) {
            banner.setVisibility(View.GONE);
        } else {
            banner.setVisibility(View.VISIBLE);
            banner.setText(getString(R.string.voucher_applied_banner, v.code, v.discountLabel()));
        }
    }

    private class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VH> {
        private final List<Voucher> list;

        VoucherAdapter(List<Voucher> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Voucher item = list.get(position);
            holder.title.setText(item.title);
            holder.desc.setText(item.description);
            holder.code.setText(item.code);
            holder.expiry.setText(getString(R.string.voucher_expiry_fmt, item.expiry));
            holder.discount.setText(item.discountPercent > 0
                    ? getString(R.string.voucher_discount_fmt, item.discountPercent)
                    : "");

            String scope = "ALL".equalsIgnoreCase(item.appliesToType)
                    ? getString(R.string.voucher_scope_all)
                    : getString(R.string.voucher_scope_type, item.appliesToType);
            holder.desc.setText(item.description + "\n" + scope);

            String applied = LocalStore.getAppliedVoucherCode(VoucherActivity.this);
            boolean isApplied = applied != null && applied.equalsIgnoreCase(item.code);
            holder.btnApply.setText(isApplied ? getString(R.string.voucher_applied_btn) : getString(R.string.voucher_apply));
            holder.btnApply.setEnabled(!isApplied);
            holder.btnApply.setAlpha(isApplied ? 0.5f : 1f);

            holder.btnApply.setOnClickListener(v -> {
                LocalStore.setAppliedVoucherCode(VoucherActivity.this, item.code);
                updateBanner();
                notifyDataSetChanged();
                Toast.makeText(VoucherActivity.this,
                        getString(R.string.voucher_applied_toast, item.code, item.discountLabel()),
                        Toast.LENGTH_LONG).show();
            });
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView title, desc, code, expiry, discount;
            final MaterialButton btnApply;

            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tvVoucherTitle);
                desc = v.findViewById(R.id.tvVoucherDesc);
                code = v.findViewById(R.id.tvVoucherCode);
                expiry = v.findViewById(R.id.tvVoucherExpiry);
                discount = v.findViewById(R.id.tvVoucherDiscount);
                btnApply = v.findViewById(R.id.btnApplyVoucher);
            }
        }
    }
}
