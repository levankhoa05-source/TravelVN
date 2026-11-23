package com.example.travelvn;

import android.content.Intent; 
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View; 
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout; 
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.List;

public class TripDetailsActivity extends AppCompatActivity {

    private String tripId;
    private Trip trip;
    private String selectedSeat = "";
    private List<TextView> seatViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        tripId = getIntent().getStringExtra("TRIP_ID");
        loadTripData();
    }

    private void loadTripData() {
        if (tripId == null || tripId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin chuyến", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fallbackFromLocal();
    }

    private void fallbackFromLocal() {
        for (Trip t : LocalStore.getAllTrips()) {
            if (t.getId() != null && t.getId().equals(tripId)) {
                trip = t;
                break;
            }
        }
        if (trip == null) {
            Toast.makeText(this, "Không tìm thấy thông tin chuyến", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setupView();
    }

    private void setupView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ((TextView) findViewById(R.id.tvTripProvider)).setText(trip.getProviderName());
        ((TextView) findViewById(R.id.tvTripPath)).setText(trip.getPath());
        ((TextView) findViewById(R.id.tvTripTime)).setText(trip.getTime() + " - " + trip.getDate());
        ((TextView) findViewById(R.id.tvTripSeats)).setText("Số ghế trống: " + trip.getAvailableSeats());

        TextView tvOrig = findViewById(R.id.tvTripPriceOriginal);
        TextView tvFinal = findViewById(R.id.tvTripPriceFinal);
        TextView tvVoucherNote = findViewById(R.id.tvTripVoucherNote);
        double basePrice = trip.getPriceValue();
        double payPrice = LocalStore.finalPriceForTrip(this, trip);
        Voucher appliedV = LocalStore.getAppliedVoucher(this);
        tvFinal.setText(MoneyUtils.formatVndDots(payPrice));
        if (appliedV != null && appliedV.appliesToTripType(trip.getType()) && payPrice < basePrice) {
            tvOrig.setVisibility(View.VISIBLE);
            tvOrig.setText(MoneyUtils.formatVndDots(basePrice));
            tvOrig.setPaintFlags(tvOrig.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvVoucherNote.setVisibility(View.VISIBLE);
            tvVoucherNote.setText(getString(R.string.details_voucher_applied, appliedV.code, appliedV.discountLabel()));
        } else {
            tvOrig.setVisibility(View.GONE);
            tvVoucherNote.setVisibility(View.GONE);
        }

        GridLayout gridSeats = findViewById(R.id.gridSeats);
        int totalSeats = 30;
        for (int i = 1; i <= totalSeats; i++) {
            final String seatNum = "G" + (i < 10 ? "0" + i : i);
            TextView seat = new TextView(this);
            seat.setText(seatNum);
            seat.setGravity(Gravity.CENTER);
            seat.setPadding(30, 30, 30, 30);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(10, 10, 10, 10);
            seat.setLayoutParams(params);

            if (trip.getOccupiedSeats().contains(seatNum)) {
                seat.setBackgroundColor(Color.LTGRAY);
                seat.setTextColor(Color.WHITE);
                seat.setEnabled(false);
            } else {
                seat.setBackgroundColor(Color.WHITE);
                seat.setTextColor(Color.BLACK);
                seat.setOnClickListener(v -> {
                    for (TextView sv : seatViews) {
                        if (sv.isEnabled()) {
                            sv.setBackgroundColor(Color.WHITE);
                            sv.setTextColor(Color.BLACK);
                        }
                    }
                    seat.setBackgroundColor(Color.parseColor("#007BFF")); 
                    seat.setTextColor(Color.WHITE);
                    selectedSeat = seatNum;
                    ((TextView) findViewById(R.id.tvSelectedSeat)).setText("Ghế đã chọn: " + selectedSeat);
                });
            }
            seatViews.add(seat);
            gridSeats.addView(seat);
        }

        LinearLayout llTimeline = findViewById(R.id.llTimeline);
        llTimeline.removeAllViews();
        List<TimelineItem> timeline = trip.getTimeline();

        if (timeline.isEmpty()) {
            trip.addTimeline("08:00", "Ra bến xe / ga tàu");
            trip.addTimeline("09:00", "Xuất phát");
            trip.addTimeline("13:00", "Đến nơi");
            timeline = trip.getTimeline();
        }

        for (TimelineItem item : timeline) {
            View view = getLayoutInflater().inflate(R.layout.item_timeline, llTimeline, false);
            ((TextView) view.findViewById(R.id.tvTimeLineTime)).setText(item.time);
            ((TextView) view.findViewById(R.id.tvTimeLineTitle)).setText(item.title);
            llTimeline.addView(view);
        }

        EditText etName = findViewById(R.id.etName);
        EditText etPhone = findViewById(R.id.etPhone);
        Button btnBook = findViewById(R.id.btnBook);

        btnBook.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();

            if (name.isEmpty() || phone.isEmpty() || selectedSeat.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin và chọn ghế", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phone.matches("^0\\d{9}$")) {
                Toast.makeText(this, "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và có 10 chữ số)", Toast.LENGTH_SHORT).show();
                return;
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault());
            String currentDate = sdf.format(new java.util.Date());

            double pay = LocalStore.finalPriceForTrip(this, trip);
            Booking booking = new Booking(trip.getName(), name, phone, selectedSeat, "PAID", pay, currentDate);
            LocalStore.addBooking(this, booking);
            trip.addOccupiedSeat(selectedSeat);
            LocalStore.saveData(this);

            Toast.makeText(this, "🎉 Đặt vé thành công!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, BookingConfirmationActivity.class);
            intent.putExtra("QR_DATA", booking.qrData);
            intent.putExtra("TRIP_NAME", trip.getName());
            intent.putExtra("SEAT", selectedSeat);
            startActivity(intent);
            finish();
        });

        if (findViewById(R.id.btnChat) != null) {
            findViewById(R.id.btnChat).setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("RECEIVER_ID", trip.getProviderName() + "@gmail.com");
                intent.putExtra("RECEIVER_NAME", trip.getProviderName());
                startActivity(intent);
            });
        }
    }
}