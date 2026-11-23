package com.example.travelvn;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etName = findViewById(R.id.etFullName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPass = findViewById(R.id.etRegisterPassword);
        EditText etConfirmPass = findViewById(R.id.etConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegisterSubmit);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String pass = etPass.getText().toString();
            String confirmPass = etConfirmPass.getText().toString();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = email.trim().toLowerCase().replace("/", "_");
            User user = new User(email.trim(), name.trim(), "user");
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            auth.createUserWithEmailAndPassword(email.trim(), pass)
                    .addOnSuccessListener(result -> db.collection("users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                LocalStore.setCurrentUser(RegisterActivity.this, email.trim());
                                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                if (!isOnline()) {
                                    Toast.makeText(RegisterActivity.this, "Bạn đang ở chế độ Offline", Toast.LENGTH_SHORT).show();
                                }
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(
                                    RegisterActivity.this,
                                    "Không thể lưu hồ sơ: " + (e.getMessage() == null ? "Lỗi không xác định" : e.getMessage()),
                                    Toast.LENGTH_SHORT
                            ).show()))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "register failed", e);
                        String message = "Đăng ký thất bại.";
                        if (e instanceof FirebaseAuthException) {
                            String code = ((FirebaseAuthException) e).getErrorCode();
                            if ("ERROR_CONFIGURATION_NOT_FOUND".equals(code)) {
                                message = "Firebase Auth chưa bật Email/Password trong Console.";
                            } else if ("ERROR_NETWORK_REQUEST_FAILED".equals(code)) {
                                message = "Lỗi mạng. Kiểm tra Internet rồi thử lại.";
                            } else if ("ERROR_EMAIL_ALREADY_IN_USE".equals(code)) {
                                message = "Email này đã được đăng ký.";
                            } else if ("ERROR_INVALID_EMAIL".equals(code)) {
                                message = "Email không hợp lệ.";
                            } else {
                                message = "Đăng ký lỗi (" + code + ").";
                            }
                        } else if (e.getMessage() != null) {
                            message = "Đăng ký thất bại: " + e.getMessage();
                        }
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                    });
        });

        findViewById(R.id.btnLoginRedirect).setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private boolean isOnline() {
        ConnectivityManager cm = getSystemService(ConnectivityManager.class);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities cap = cm.getNetworkCapabilities(network);
        return cap != null && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
