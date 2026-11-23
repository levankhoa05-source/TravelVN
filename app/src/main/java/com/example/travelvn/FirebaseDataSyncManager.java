package com.example.travelvn;

import android.content.Context;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

/** Firebase la nguon du lieu chinh, local dong vai tro cache offline-first. */
public class FirebaseDataSyncManager {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseLocalDataSeeder seeder = new FirebaseLocalDataSeeder();
    private final List<ListenerRegistration> registrations = new ArrayList<>();

    public void syncAll(Context context, Runnable onDone) {
        seeder.pushAll(() ->
                syncTrips(context, () ->
                        syncHotels(context, () ->
                                syncVouchers(context, () ->
                                        syncDestinations(context, () ->
                                                syncBookings(context, () ->
                                                        syncMessages(context, () ->
                                                                syncUsers(context, onDone))))))));
    }

    public void startRealtimeCache(Context context) {
        stopRealtimeCache();
        registrations.add(db.collection("trips").addSnapshotListener((s, e) -> {
            if (e != null || s == null) return;
            List<Trip> list = new ArrayList<>();
            s.getDocuments().forEach(d -> {
                Trip x = d.toObject(Trip.class);
                if (x != null) list.add(x);
            });
            LocalStore.replaceTrips(context, list);
        }));
        registrations.add(db.collection("hotels").addSnapshotListener((s, e) -> {
            if (e != null || s == null) return;
            List<Hotel> list = new ArrayList<>();
            s.getDocuments().forEach(d -> {
                Hotel x = d.toObject(Hotel.class);
                if (x != null) list.add(x);
            });
            LocalStore.replaceHotels(context, list);
        }));
        registrations.add(db.collection("vouchers").addSnapshotListener((s, e) -> {
            if (e != null || s == null) return;
            List<Voucher> list = new ArrayList<>();
            s.getDocuments().forEach(d -> {
                Voucher x = d.toObject(Voucher.class);
                if (x != null) list.add(x);
            });
            LocalStore.replaceVouchers(context, list);
        }));
        registrations.add(db.collection("destinations").addSnapshotListener((s, e) -> {
            if (e != null || s == null) return;
            List<Destination> list = new ArrayList<>();
            s.getDocuments().forEach(d -> {
                Destination x = d.toObject(Destination.class);
                if (x != null) list.add(x);
            });
            LocalStore.replaceDestinations(context, list);
        }));
        registrations.add(db.collection("bookings").addSnapshotListener((s, e) -> {
            if (e != null || s == null) return;
            List<Booking> list = new ArrayList<>();
            s.getDocuments().forEach(d -> {
                Booking x = d.toObject(Booking.class);
                if (x != null) list.add(x);
            });
            LocalStore.replaceBookings(context, list);
        }));
        registrations.add(db.collection("messages").addSnapshotListener((s, e) -> {
            if (e != null || s == null) return;
            List<Message> list = new ArrayList<>();
            s.getDocuments().forEach(d -> {
                Message x = d.toObject(Message.class);
                if (x != null) list.add(x);
            });
            LocalStore.replaceMessages(context, list);
        }));
        registrations.add(db.collection("users").addSnapshotListener((s, e) -> {
            if (e != null || s == null) return;
            List<User> list = new ArrayList<>();
            s.getDocuments().forEach(d -> {
                User x = d.toObject(User.class);
                if (x != null) list.add(x);
            });
            LocalStore.replaceUsers(context, list);
        }));
    }

    public void stopRealtimeCache() {
        for (ListenerRegistration r : registrations) {
            if (r != null) r.remove();
        }
        registrations.clear();
    }

    private void syncTrips(Context context, Runnable onDone) {
        db.collection("trips").get()
                .addOnSuccessListener(s -> {
                    List<Trip> list = new ArrayList<>();
                    s.getDocuments().forEach(d -> {
                        Trip x = d.toObject(Trip.class);
                        if (x != null) list.add(x);
                    });
                    LocalStore.replaceTrips(context, list);
                    onDone.run();
                })
                .addOnFailureListener(e -> onDone.run());
    }

    private void syncHotels(Context context, Runnable onDone) {
        db.collection("hotels").get()
                .addOnSuccessListener(s -> {
                    List<Hotel> list = new ArrayList<>();
                    s.getDocuments().forEach(d -> {
                        Hotel x = d.toObject(Hotel.class);
                        if (x != null) list.add(x);
                    });
                    LocalStore.replaceHotels(context, list);
                    onDone.run();
                })
                .addOnFailureListener(e -> onDone.run());
    }

    private void syncVouchers(Context context, Runnable onDone) {
        db.collection("vouchers").get()
                .addOnSuccessListener(s -> {
                    List<Voucher> list = new ArrayList<>();
                    s.getDocuments().forEach(d -> {
                        Voucher x = d.toObject(Voucher.class);
                        if (x != null) list.add(x);
                    });
                    LocalStore.replaceVouchers(context, list);
                    onDone.run();
                })
                .addOnFailureListener(e -> onDone.run());
    }

    private void syncDestinations(Context context, Runnable onDone) {
        db.collection("destinations").get()
                .addOnSuccessListener(s -> {
                    List<Destination> list = new ArrayList<>();
                    s.getDocuments().forEach(d -> {
                        Destination x = d.toObject(Destination.class);
                        if (x != null) list.add(x);
                    });
                    LocalStore.replaceDestinations(context, list);
                    onDone.run();
                })
                .addOnFailureListener(e -> onDone.run());
    }

    private void syncBookings(Context context, Runnable onDone) {
        db.collection("bookings").get()
                .addOnSuccessListener(s -> {
                    List<Booking> list = new ArrayList<>();
                    s.getDocuments().forEach(d -> {
                        Booking x = d.toObject(Booking.class);
                        if (x != null) list.add(x);
                    });
                    LocalStore.replaceBookings(context, list);
                    onDone.run();
                })
                .addOnFailureListener(e -> onDone.run());
    }

    private void syncMessages(Context context, Runnable onDone) {
        db.collection("messages").get()
                .addOnSuccessListener(s -> {
                    List<Message> list = new ArrayList<>();
                    s.getDocuments().forEach(d -> {
                        Message x = d.toObject(Message.class);
                        if (x != null) list.add(x);
                    });
                    LocalStore.replaceMessages(context, list);
                    onDone.run();
                })
                .addOnFailureListener(e -> onDone.run());
    }

    private void syncUsers(Context context, Runnable onDone) {
        db.collection("users").get()
                .addOnSuccessListener(s -> {
                    List<User> list = new ArrayList<>();
                    s.getDocuments().forEach(d -> {
                        User x = d.toObject(User.class);
                        if (x != null) list.add(x);
                    });
                    LocalStore.replaceUsers(context, list);
                    onDone.run();
                })
                .addOnFailureListener(e -> onDone.run());
    }
}
