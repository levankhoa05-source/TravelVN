package com.example.travelvn;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.List;

/**
 * Đẩy dữ liệu local (mock) lên Firestore một lần khi collection còn trống.
 * Tên trường trên Firestore khớp tên biến trong các class Java.
 */
public class FirebaseLocalDataSeeder {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Gọi tuần tự tất cả các hàm push (mỗi collection chỉ seed khi rỗng). */
    public void pushAll(Runnable onComplete) {
        pushHotels(() -> pushVouchers(() -> pushDestinations(() -> pushBookings(() -> pushMessages(() -> pushUsers(onComplete))))));
    }

    public void pushHotels(Runnable onComplete) {
        db.collection("hotels")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    List<Hotel> list = LocalStore.getHotels();
                    if (list == null || list.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    WriteBatch batch = db.batch();
                    for (Hotel h : list) {
                        if (h == null) continue;
                        DocumentReference ref = db.collection("hotels").document();
                        batch.set(ref, h);
                    }
                    batch.commit().addOnCompleteListener(t -> onComplete.run());
                })
                .addOnFailureListener(e -> onComplete.run());
    }

    public void pushVouchers(Runnable onComplete) {
        db.collection("vouchers")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    List<Voucher> list = LocalStore.getVouchers();
                    if (list == null || list.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    WriteBatch batch = db.batch();
                    for (Voucher v : list) {
                        if (v == null || v.code == null || v.code.trim().isEmpty()) continue;
                        String docId = v.code.trim();
                        batch.set(db.collection("vouchers").document(docId), v);
                    }
                    batch.commit().addOnCompleteListener(t -> onComplete.run());
                })
                .addOnFailureListener(e -> onComplete.run());
    }

    public void pushDestinations(Runnable onComplete) {
        db.collection("destinations")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    List<Destination> list = LocalStore.catalogDestinations();
                    if (list == null || list.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    WriteBatch batch = db.batch();
                    int i = 0;
                    for (Destination d : list) {
                        if (d == null) continue;
                        String docId = d.getName() != null && !d.getName().isEmpty()
                                ? d.getName().replace("/", "-") + "_" + (i++)
                                : db.collection("destinations").document().getId();
                        batch.set(db.collection("destinations").document(docId), d);
                    }
                    batch.commit().addOnCompleteListener(t -> onComplete.run());
                })
                .addOnFailureListener(e -> onComplete.run());
    }

    public void pushBookings(Runnable onComplete) {
        db.collection("bookings")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    List<Booking> list = LocalStore.getAllBookings();
                    if (list == null || list.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    WriteBatch batch = db.batch();
                    for (Booking b : list) {
                        if (b == null) continue;
                        String docId;
                        if (b.qrData != null && !b.qrData.trim().isEmpty()) {
                            docId = String.valueOf(Math.abs(b.qrData.hashCode()));
                        } else {
                            docId = db.collection("bookings").document().getId();
                        }
                        batch.set(db.collection("bookings").document(docId), b);
                    }
                    batch.commit().addOnCompleteListener(t -> onComplete.run());
                })
                .addOnFailureListener(e -> onComplete.run());
    }

    public void pushMessages(Runnable onComplete) {
        db.collection("messages")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    List<Message> list = LocalStore.getMessages();
                    if (list == null || list.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    WriteBatch batch = db.batch();
                    for (Message m : list) {
                        if (m == null) continue;
                        DocumentReference ref = db.collection("messages").document();
                        batch.set(ref, m);
                    }
                    batch.commit().addOnCompleteListener(t -> onComplete.run());
                })
                .addOnFailureListener(e -> onComplete.run());
    }

    public void pushUsers(Runnable onComplete) {
        db.collection("users")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    List<User> list = LocalStore.catalogUsers();
                    if (list == null || list.isEmpty()) {
                        onComplete.run();
                        return;
                    }
                    WriteBatch batch = db.batch();
                    for (User u : list) {
                        if (u == null || u.email == null || u.email.trim().isEmpty()) continue;
                        String docId = u.email.trim().replace("/", "_");
                        batch.set(db.collection("users").document(docId), u);
                    }
                    batch.commit().addOnCompleteListener(t -> onComplete.run());
                })
                .addOnFailureListener(e -> onComplete.run());
    }
}
