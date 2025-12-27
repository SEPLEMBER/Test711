package com.syndes.javacomponents;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ZLSActivity extends AppCompatActivity {

    private static final long DOT_INTERVAL_MS = 500L;   // обновление точек
    private static final long TOTAL_DELAY_MS = 2000L;   // время до перехода
    private final String BASE_TEXT = "ZER0LESS running";

    private TextView statusView;
    private Handler handler;
    private int dotIndex = 0;
    private final String[] dots = {".", "..", "..."};

    private final Runnable dotRunnable = new Runnable() {
        @Override
        public void run() {
            // показываем базовый текст + текущие точки
            String text = BASE_TEXT + dots[dotIndex];
            statusView.setText(text);

            // следующий индекс
            dotIndex = (dotIndex + 1) % dots.length;

            // запланировать следующий тик
            handler.postDelayed(this, DOT_INTERVAL_MS);
        }
    };

    private final Runnable navigateRunnable = new Runnable() {
        @Override
        public void run() {
            // Переходим в ZLShelloActivity (явный Intent, внутренняя activity, exported=false)
            Intent intent = new Intent(ZLSActivity.this, LockActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zls);

        statusView = findViewById(R.id.zls_status);

        handler = new Handler(Looper.getMainLooper());

        // стартуем анимацию точек (немедленно)
        handler.post(dotRunnable);

        // через TOTAL_DELAY_MS — навигация в ZLShelloActivity
        handler.postDelayed(navigateRunnable, TOTAL_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // обязательно убрать все callbacks чтобы не было утечек
        if (handler != null) {
            handler.removeCallbacks(dotRunnable);
            handler.removeCallbacks(navigateRunnable);
        }
    }
}
