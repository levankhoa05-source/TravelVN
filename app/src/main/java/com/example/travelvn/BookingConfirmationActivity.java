package com.example.travelvn;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BookingConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        String qrData = getIntent().getStringExtra("QR_DATA");
        String tripName = getIntent().getStringExtra("TRIP_NAME");
        String seat = getIntent().getStringExtra("SEAT");

        ((TextView) findViewById(R.id.tvConfirmTrip)).setText(tripName);
        ((TextView) findViewById(R.id.tvConfirmSeat)).setText("Số ghế: " + seat);
        ((TextView) findViewById(R.id.tvQrData)).setText(qrData);

        ImageView ivQr = findViewById(R.id.ivTicketQr);
        ivQr.setImageBitmap(generateFakeQr(qrData));

        findViewById(R.id.btnDone).setOnClickListener(v -> finish());
    }

    private Bitmap generateFakeQr(String data) {
        // Simple logic to draw a 10x10 fake QR grid
        int size = 500;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        int cellSize = size / 20;
        
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                int color = (Math.abs(data.hashCode() * (x + 1) * (y + 1)) % 2 == 0) ? Color.BLACK : Color.WHITE;
                // Add borders for standard QR look
                if (x < 5 && y < 5) color = Color.BLACK;
                if (x > 14 && y < 5) color = Color.BLACK;
                if (x < 5 && y > 14) color = Color.BLACK;
                
                for (int i = 0; i < cellSize; i++) {
                    for (int j = 0; j < cellSize; j++) {
                        bitmap.setPixel(x * cellSize + i, y * cellSize + j, color);
                    }
                }
            }
        }
        return bitmap;
    }
}
