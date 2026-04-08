package com.example.androidcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class BookingActivity extends AppCompatActivity {

    private static final String TAG = "BookingActivity";
    private TextView tvMovieName;
    private Spinner spinnerShowtime;
    private EditText etSeat;
    private Button btnConfirm;
    private String movieId, movieTitle;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // 1. Yêu cầu quyền thông báo cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvMovieName = findViewById(R.id.tvMovieName);
        spinnerShowtime = findViewById(R.id.spinnerShowtime);
        etSeat = findViewById(R.id.etSeat);
        btnConfirm = findViewById(R.id.btnConfirmBooking);

        movieId = getIntent().getStringExtra("movie_id");
        movieTitle = getIntent().getStringExtra("movie_title");
        tvMovieName.setText("Phim: " + movieTitle);

        // Dummy showtimes
        String[] showtimes = {"19:00 - 20/10/2023", "21:30 - 20/10/2023", "15:20 - 08/04/2026"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, showtimes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShowtime.setAdapter(adapter);

        btnConfirm.setOnClickListener(v -> saveTicket());
    }

    private void saveTicket() {
        String seat = etSeat.getText().toString().trim();
        String showtime = spinnerShowtime.getSelectedItem().toString();

        if (TextUtils.isEmpty(seat)) {
            Toast.makeText(this, "Vui lòng nhập số ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang xử lý...");

        Ticket ticket = new Ticket();
        ticket.setTicketId(UUID.randomUUID().toString());
        ticket.setUserId(mAuth.getCurrentUser().getUid());
        ticket.setMovieTitle(movieTitle);
        ticket.setShowtime(showtime);
        ticket.setSeatNumber(seat);
        ticket.setTimestamp(System.currentTimeMillis());

        db.collection("tickets").document(ticket.getTicketId())
                .set(ticket)
                .addOnSuccessListener(aVoid -> {
                    scheduleReminder(movieTitle, showtime);
                    Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Xác nhận đặt vé");
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void scheduleReminder(String title, String showtimeString) {
        try {
            // Đặt thông báo sau 5 giây kể từ lúc nhấn đặt vé để kiểm tra ngay
            long triggerTime = System.currentTimeMillis() + 5000; 

            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("movie_title", title);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 
                    (int) System.currentTimeMillis(), 
                    intent, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // SỬA TẠI ĐÂY: Dùng setAndAllowWhileIdle thay vì setExact...
                    // Phương thức này không yêu cầu quyền SCHEDULE_EXACT_ALARM
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            }
            Log.d(TAG, "Đã đặt lịch nhắc ổn định sau 5 giây cho: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đặt lịch nhắc: " + e.getMessage());
        }
    }
}
