package com.example.travelvn;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private boolean isPartnerTab = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khai báo các view
        TextView tabCustomer = findViewById(R.id.tabCustomer);
        TextView tabPartner = findViewById(R.id.tabPartner);
        EditText etUser = findViewById(R.id.etUsername);
        EditText etPass = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLoginSubmit);

        // Xử lý tab Khách hàng
        tabCustomer.setOnClickListener(v -> {
            isPartnerTab = false;
            tabCustomer.setBackgroundResource(R.drawable.white_button_bg);
            tabCustomer.setTextColor(ContextCompat.getColor(this, R.color.vivu_blue));
            tabPartner.setBackground(null);
            tabPartner.setTextColor(ContextCompat.getColor(this, R.color.vivu_text_gray));
            btnLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.vivu_blue)));
            btnLogin.setText("Đăng nhập");
        });

        // Xử lý tab Đối tác
        tabPartner.setOnClickListener(v -> {
            isPartnerTab = true;
            tabPartner.setBackgroundResource(R.drawable.white_button_bg);
            tabPartner.setTextColor(ContextCompat.getColor(this, R.color.vivu_purple));
            tabCustomer.setBackground(null);
            tabCustomer.setTextColor(ContextCompat.getColor(this, R.color.vivu_text_gray));
            btnLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.vivu_purple)));
            btnLogin.setText("Đăng nhập Đối tác");
        });

        // Xử lý nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String user = etUser.getText().toString().trim();
            String pass = etPass.getText().toString();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(user, pass)
                    .addOnSuccessListener(result -> {
                        LocalStore.setCurrentUser(LoginActivity.this, user);
                        if (user.equalsIgnoreCase("admin@gmail.com")) {
                            Toast.makeText(LoginActivity.this, "Đăng nhập Admin thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                        } else if (isPartnerTab || user.toLowerCase().startsWith("partner")) {
                            Toast.makeText(LoginActivity.this, "Đăng nhập Đối tác thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, ProviderDashboardActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Đăng nhập Khách hàng thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(
                            LoginActivity.this,
                            "Sai tài khoản hoặc mật khẩu: " + (e.getMessage() == null ? "Lỗi không xác định" : e.getMessage()),
                            Toast.LENGTH_SHORT
                    ).show());
        });

        // Nút Đăng ký
        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // Nút quay lại
        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }
}