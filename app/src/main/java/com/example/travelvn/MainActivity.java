package com.example.travelvn;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.util.Calendar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvDestinations, rvOffers;
    private NestedScrollView mainScroll;
    private TextInputEditText etDeparture, etDestination;
    private BottomNavigationView bottomNav;
    private AutoCompleteTextView typeView;
    private FirebaseDataSyncManager dataSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableFirestoreOfflinePersistence();
        LocalStore.init(this);
        dataSyncManager = new FirebaseDataSyncManager();
        dataSyncManager.syncAll(this, this::refreshUiAfterSync);
        dataSyncManager.startRealtimeCache(this);
        notifyIfOffline();

        mainScroll = findViewById(R.id.mainScroll);
        bottomNav = findViewById(R.id.bottomNav);
        typeView = findViewById(R.id.autoCompleteType);

        setupBottomNavigation();
        setupSearchSection();
        setupOffersSection();
        setupDestinationsSection();
        setupContentActions();
    }

    private void enableFirestoreOfflinePersistence() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
        } catch (Exception ignored) {
            // Nếu Firestore đã khởi tạo trước đó, giữ nguyên settings hiện tại.
        }
    }

    private void notifyIfOffline() {
        ConnectivityManager cm = getSystemService(ConnectivityManager.class);
        if (cm == null) return;
        Network network = cm.getActiveNetwork();
        if (network == null) {
            Toast.makeText(this, "Bạn đang ở chế độ Offline", Toast.LENGTH_SHORT).show();
            return;
        }
        NetworkCapabilities cap = cm.getNetworkCapabilities(network);
        boolean hasInternet = cap != null && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        if (!hasInternet) {
            Toast.makeText(this, "Bạn đang ở chế độ Offline", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshUiAfterSync() {
        runOnUiThread(this::setupDestinationsSection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onDestroy() {
        if (dataSyncManager != null) {
            dataSyncManager.stopRealtimeCache();
        }
        super.onDestroy();
    }

    public void selectDestination(String name) {
        if (etDestination != null) {
            etDestination.setText(name);
            Toast.makeText(this, "Đã chọn điểm đến: " + name, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                if (mainScroll != null) {
                    mainScroll.smoothScrollTo(0, 0);
                }
                return true;
            }
            if (id == R.id.nav_deals) {
                startActivity(new Intent(this, VoucherActivity.class));
                return true;
            }
            if (id == R.id.nav_tickets) {
                startActivity(new Intent(this, BookingHistoryActivity.class));
                return true;
            }
            if (id == R.id.nav_services) {
                openServicesSheet();
                bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.nav_home));
                return true;
            }
            if (id == R.id.nav_account) {
                openAccountSheet();
                bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.nav_home));
                return true;
            }
            return false;
        });
    }

    private void openServicesSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        sheet.setContentView(R.layout.bottom_sheet_services);
        sheet.findViewById(R.id.rowCompare).setOnClickListener(v -> {
            sheet.dismiss();
            startActivity(new Intent(this, TripComparisonActivity.class));
        });
        sheet.findViewById(R.id.rowHotels).setOnClickListener(v -> {
            sheet.dismiss();
            startActivity(new Intent(this, HotelListActivity.class));
        });
        sheet.findViewById(R.id.rowChat).setOnClickListener(v -> {
            sheet.dismiss();
            openSupportChat();
        });
        sheet.findViewById(R.id.rowExplore).setOnClickListener(v -> {
            sheet.dismiss();
            startActivity(new Intent(this, DestinationsExploreActivity.class));
        });
        sheet.show();
    }

    private void openSupportChat() {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("RECEIVER_ID", "support@travelvn.com");
        i.putExtra("RECEIVER_NAME", getString(R.string.support_display_name));
        startActivity(i);
    }

    private void openAccountSheet() {
        String user = LocalStore.getCurrentUser(this);
        if (user == null) {
            BottomSheetDialog sheet = new BottomSheetDialog(this);
            sheet.setContentView(R.layout.bottom_sheet_account_guest);
            MaterialButton login = sheet.findViewById(R.id.btnSheetLogin);
            MaterialButton reg = sheet.findViewById(R.id.btnSheetRegister);
            if (login != null) {
                login.setOnClickListener(v -> {
                    sheet.dismiss();
                    startActivity(new Intent(this, LoginActivity.class));
                });
            }
            if (reg != null) {
                reg.setOnClickListener(v -> {
                    sheet.dismiss();
                    startActivity(new Intent(this, RegisterActivity.class));
                });
            }
            sheet.show();
        } else {
            BottomSheetDialog sheet = new BottomSheetDialog(this);
            sheet.setContentView(R.layout.bottom_sheet_account_user);
            TextView tvEmail = sheet.findViewById(R.id.tvSheetUserEmail);
            if (tvEmail != null) {
                tvEmail.setText(user);
            }
            View rowTickets = sheet.findViewById(R.id.rowSheetTickets);
            View rowHotels = sheet.findViewById(R.id.rowSheetHotels);
            View rowCompare = sheet.findViewById(R.id.rowSheetCompare);
            View rowChat = sheet.findViewById(R.id.rowSheetChat);
            View rowAdmin = sheet.findViewById(R.id.rowSheetAdmin);
            View rowPartner = sheet.findViewById(R.id.rowSheetPartner);
            View btnLogout = sheet.findViewById(R.id.btnSheetLogout);

            if (rowTickets != null) {
                rowTickets.setOnClickListener(v -> {
                    sheet.dismiss();
                    startActivity(new Intent(this, BookingHistoryActivity.class));
                });
            }
            if (rowHotels != null) {
                rowHotels.setOnClickListener(v -> {
                    sheet.dismiss();
                    startActivity(new Intent(this, HotelListActivity.class));
                });
            }
            if (rowCompare != null) {
                rowCompare.setOnClickListener(v -> {
                    sheet.dismiss();
                    startActivity(new Intent(this, TripComparisonActivity.class));
                });
            }
            if (rowChat != null) {
                rowChat.setOnClickListener(v -> {
                    sheet.dismiss();
                    openSupportChat();
                });
            }
            if (rowAdmin != null) {
                if (LocalStore.isAdminEmail(user)) {
                    rowAdmin.setVisibility(View.VISIBLE);
                    rowAdmin.setOnClickListener(v -> {
                        sheet.dismiss();
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                    });
                } else {
                    rowAdmin.setVisibility(View.GONE);
                }
            }
            if (rowPartner != null) {
                if (LocalStore.isPartnerEmail(user)) {
                    rowPartner.setVisibility(View.VISIBLE);
                    rowPartner.setOnClickListener(v -> {
                        sheet.dismiss();
                        startActivity(new Intent(this, ProviderDashboardActivity.class));
                    });
                } else {
                    rowPartner.setVisibility(View.GONE);
                }
            }
            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    sheet.dismiss();
                    new MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.account_sign_out)
                            .setMessage(R.string.account_sign_out_confirm)
                            .setNegativeButton(R.string.account_cancel, null)
                            .setPositiveButton(R.string.account_sign_out, (d, w) -> {
                                LocalStore.logout(this);
                                bottomNav.setSelectedItemId(R.id.nav_home);
                                recreate();
                            })
                            .show();
                });
            }
            sheet.show();
        }
    }

    private void setupSearchSection() {
        String[] provinces = ProvinceData.getList();

        etDeparture = findViewById(R.id.etDeparture);
        etDestination = findViewById(R.id.etDestination);
        TextInputLayout tilDeparture = findViewById(R.id.tilDeparture);
        TextInputLayout tilDestination = findViewById(R.id.tilDestination);

        View.OnClickListener pickFrom = v -> showProvincePicker(etDeparture, R.string.pick_departure_title, provinces);
        View.OnClickListener pickTo = v -> showProvincePicker(etDestination, R.string.pick_destination_title, provinces);

        if (etDeparture != null) {
            if (etDeparture.getText() == null || etDeparture.getText().length() == 0) {
                etDeparture.setText("Hà Nội");
            }
            etDeparture.setOnClickListener(pickFrom);
        }
        if (tilDeparture != null) {
            tilDeparture.setStartIconOnClickListener(v -> showProvincePicker(etDeparture, R.string.pick_departure_title, provinces));
        }

        if (etDestination != null) {
            if (etDestination.getText() == null || etDestination.getText().length() == 0) {
                etDestination.setText("Đà Nẵng");
            }
            etDestination.setOnClickListener(pickTo);
        }
        if (tilDestination != null) {
            tilDestination.setStartIconOnClickListener(v -> showProvincePicker(etDestination, R.string.pick_destination_title, provinces));
        }

        EditText dateView = findViewById(R.id.etSearchDate);

        String[] types = {"Tất cả", "Máy bay", "Xe khách", "Tàu hỏa"};
        if (typeView != null) {
            typeView.setThreshold(1);
            typeView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types));
            typeView.setText("Tất cả", false);
            typeView.setOnClickListener(v -> typeView.showDropDown());
            typeView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) typeView.showDropDown();
            });
        }

        AutoCompleteTextView timeView = findViewById(R.id.autoCompleteTime);
        if (timeView != null) {
            timeView.setThreshold(1);
            String[] times = {"Tất cả khung giờ", "00:00 - 06:00", "06:00 - 12:00", "12:00 - 18:00", "18:00 - 24:00"};
            timeView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, times));
            timeView.setText("Tất cả khung giờ", false);
            timeView.setOnClickListener(v -> timeView.showDropDown());
            timeView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) timeView.showDropDown();
            });
        }

        if (dateView != null) {
            dateView.setText("05/04/2026");
            dateView.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                    String d = (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "/" +
                            ((month + 1) < 10 ? "0" + (month + 1) : (month + 1)) + "/" + year;
                    dateView.setText(d);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        TextView tabHotel = findViewById(R.id.tabHotel);
        if (tabHotel != null) {
            tabHotel.setOnClickListener(v -> startActivity(new Intent(this, HotelListActivity.class)));
        }

        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Intent intent = new Intent(this, TripSearchActivity.class);
            intent.putExtra("SOURCE", etDeparture != null ? etDeparture.getText().toString() : "");
            intent.putExtra("DESTINATION", etDestination != null ? etDestination.getText().toString() : "");
            String typeStr = (typeView != null && typeView.getText() != null)
                    ? typeView.getText().toString().trim() : "Tất cả";
            if (typeStr.isEmpty()) typeStr = "Tất cả";
            intent.putExtra("TYPE", typeStr);
            startActivity(intent);
        });
    }

    private void showProvincePicker(TextInputEditText field, int titleRes, String[] provinces) {
        if (field == null) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle(titleRes)
                .setItems(provinces, (dialog, which) -> field.setText(provinces[which]))
                .show();
    }

    private void setupOffersSection() {
        rvOffers = findViewById(R.id.rvOffers);
        if (rvOffers != null) {
            List<OfferAdapter.OfferItem> offerList = new ArrayList<>();
            offerList.add(new OfferAdapter.OfferItem("Giảm 20% vé xe", "Cuối tuần — còn 3 ngày", R.drawable.bg_travel_header));
            offerList.add(new OfferAdapter.OfferItem("Combo Phú Quốc", "Vé + khách sạn từ 2.990k", R.drawable.img_phuquoc));
            offerList.add(new OfferAdapter.OfferItem("Đà Lạt mùa se lạnh", "Ưu đãi độc quyền", R.drawable.img_dalat));

            rvOffers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvOffers.setAdapter(new OfferAdapter(this, offerList));
        }
    }

    private void setupDestinationsSection() {
        rvDestinations = findViewById(R.id.rvDestinations);
        if (rvDestinations != null) {
            List<Destination> list = new ArrayList<>(LocalStore.getDestinations());
            if (list.isEmpty()) {
                list.addAll(LocalStore.catalogDestinations());
            }

            rvDestinations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvDestinations.setAdapter(new DestinationAdapter(this, list));
        }
    }

    private void setupContentActions() {
        View tvOffersTitle = findViewById(R.id.tvOffersTitle);
        if (tvOffersTitle != null) {
            tvOffersTitle.setOnClickListener(v -> startActivity(new Intent(this, VoucherActivity.class)));
        }
        View btnSeeMore = findViewById(R.id.btnSeeMore);
        if (btnSeeMore != null) {
            btnSeeMore.setOnClickListener(v ->
                    startActivity(new Intent(this, DestinationsExploreActivity.class)));
        }
    }
}
