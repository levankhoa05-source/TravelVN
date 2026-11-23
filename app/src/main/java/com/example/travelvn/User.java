package com.example.travelvn;

/**
 * Người dùng (seed + Firestore). Tên trường khớp với document fields.
 */
public class User {
    public String email;
    public String displayName;
    /** admin | partner | user | support | guest */
    public String role;

    public User() {
    }

    public User(String email, String displayName, String role) {
        this.email = email;
        this.displayName = displayName;
        this.role = role == null ? "user" : role;
    }
}
