package com.example.travelvn;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class TripComparisonActivity extends AppCompatActivity {

    private List<Trip> trips;
    private Spinner spA, spB;
    private View barA, barB;
    private TextView tvHighlight, tvBarLabelA, tvBarLabelB;
    private TableLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_comparison);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        trips = new ArrayList<>(LocalStore.getAllTrips());

        spA = findViewById(R.id.spinnerTripA);
        spB = findViewById(R.id.spinnerTripB);
        barA = findViewById(R.id.barPriceA);
        barB = findViewById(R.id.barPriceB);
        tvHighlight = findViewById(R.id.tvHighlight);
        tvBarLabelA = findViewById(R.id.tvBarLabelA);
        tvBarLabelB = findViewById(R.id.tvBarLabelB);
        table = findViewById(R.id.tableCompare);

        bindComparisonData();
    }

    private void bindComparisonData() {
        List<String> labels = new ArrayList<>();
        for (Trip t : trips) {
            labels.add(shortLabel(t));
        }

        if (labels.isEmpty()) {
            tvHighlight.setText(R.string.compare_no_trips);
            findViewById(R.id.btnCompare).setEnabled(false);
            return;
        }

        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spA.setAdapter(ad);
        spB.setAdapter(ad);
        if (labels.size() > 1) {
            spB.setSelection(1);
        }

        findViewById(R.id.btnCompare).setEnabled(true);
        findViewById(R.id.btnCompare).setOnClickListener(v -> runCompare());
        runCompare();
    }

    private String shortLabel(Trip t) {
        return t.getId() + " · " + t.getPath() + " · " + t.getPrice();
    }

    private Trip tripAt(int spinnerIndex) {
        if (spinnerIndex < 0 || spinnerIndex >= trips.size()) return null;
        return trips.get(spinnerIndex);
    }

    private void runCompare() {
        int ia = spA.getSelectedItemPosition();
        int ib = spB.getSelectedItemPosition();
        Trip a = tripAt(ia);
        Trip b = tripAt(ib);
        if (a == null || b == null) {
            return;
        }
        if (a.getId().equals(b.getId())) {
            Toast.makeText(this, R.string.compare_same_trip, Toast.LENGTH_SHORT).show();
            tvHighlight.setText(R.string.compare_same_trip);
            return;
        }

        tvBarLabelA.setText("A\n" + a.getId());
        tvBarLabelB.setText("B\n" + b.getId());

        double pa = LocalStore.finalPriceForTrip(this, a);
        double pb = LocalStore.finalPriceForTrip(this, b);
        double max = Math.max(pa, pb);
        int hMax = 160;
        int ha = max > 0 ? (int) (hMax * pa / max) : 40;
        int hb = max > 0 ? (int) (hMax * pb / max) : 40;
        ha = Math.max(ha, 24);
        hb = Math.max(hb, 24);
        setBarHeight(barA, ha);
        setBarHeight(barB, hb);

        int da = a.getDurationHoursEstimate();
        int db = b.getDurationHoursEstimate();

        StringBuilder hint = new StringBuilder();
        if (pa < pb) {
            hint.append(getString(R.string.compare_cheaper_a, MoneyUtils.formatVndDots(pb - pa)));
        } else if (pb < pa) {
            hint.append(getString(R.string.compare_cheaper_b, MoneyUtils.formatVndDots(pa - pb)));
        } else {
            hint.append(getString(R.string.compare_same_price));
        }
        hint.append("\n");
        if (da > 0 && db > 0) {
            if (da < db) {
                hint.append(getString(R.string.compare_faster_a, db - da));
            } else if (db < da) {
                hint.append(getString(R.string.compare_faster_b, da - db));
            } else {
                hint.append(getString(R.string.compare_same_duration));
            }
        }
        if (LocalStore.getAppliedVoucher(this) != null) {
            hint.append("\n").append(getString(R.string.compare_voucher_note));
        }
        tvHighlight.setText(hint.toString().trim());

        buildTable(a, b, pa, pb, da, db);
    }

    private void setBarHeight(View bar, int heightDp) {
        ViewGroup.LayoutParams lp = bar.getLayoutParams();
        lp.height = (int) (heightDp * getResources().getDisplayMetrics().density + 0.5f);
        bar.setLayoutParams(lp);
    }

    private void buildTable(Trip a, Trip b, double pa, double pb, int da, int db) {
        table.removeAllViews();
        table.addView(rowHeader());
        addRow(getString(R.string.compare_row_route), a.getPath(), b.getPath(), false, false);
        addRow(getString(R.string.compare_row_price),
                MoneyUtils.formatVndDots(pa),
                MoneyUtils.formatVndDots(pb),
                pa <= pb,
                pb <= pa);
        addRow(getString(R.string.compare_row_duration),
                da > 0 ? da + " giờ" : "—",
                db > 0 ? db + " giờ" : "—",
                da > 0 && db > 0 && da <= db,
                da > 0 && db > 0 && db <= da);
        addRow(getString(R.string.compare_row_type), a.getType(), b.getType(), false, false);
        addRow(getString(R.string.compare_row_provider), a.getProviderName(), b.getProviderName(), false, false);
        addRow(getString(R.string.compare_row_seats),
                String.valueOf(a.getAvailableSeats()),
                String.valueOf(b.getAvailableSeats()),
                a.getAvailableSeats() >= b.getAvailableSeats(),
                b.getAvailableSeats() >= a.getAvailableSeats());
        addRow(getString(R.string.compare_row_time), a.getTime(), b.getTime(), false, false);
    }

    private TableRow rowHeader() {
        TableRow row = new TableRow(this);
        int p = dp(8);
        row.setPadding(p, p, p, p);
        row.setBackgroundColor(ContextCompat.getColor(this, R.color.vivu_tab_track));
        row.addView(cell(getString(R.string.compare_col_criteria), true, false));
        row.addView(cell(getString(R.string.compare_col_a), true, true));
        row.addView(cell(getString(R.string.compare_col_b), true, true));
        return row;
    }

    private void addRow(String label, String va, String vb, boolean boldA, boolean boldB) {
        TableRow row = new TableRow(this);
        int p = dp(8);
        row.setPadding(p, p, p, p);
        row.addView(cell(label, true, false));
        TextView ca = cell(va, boldA, true);
        TextView cb = cell(vb, boldB, true);
        if (boldA && !boldB) {
            ca.setTextColor(ContextCompat.getColor(this, R.color.vivu_blue));
        }
        if (boldB && !boldA) {
            cb.setTextColor(ContextCompat.getColor(this, R.color.vivu_blue));
        }
        row.addView(ca);
        row.addView(cb);
        table.addView(row);
    }

    private TextView cell(String text, boolean bold, boolean center) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setPadding(dp(4), dp(4), dp(4), dp(4));
        if (bold) {
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        }
        if (center) {
            tv.setGravity(Gravity.CENTER);
        }
        return tv;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

}
