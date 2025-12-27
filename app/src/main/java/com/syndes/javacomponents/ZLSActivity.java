package com.syndes.javacomponents;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class ZLSActivity extends AppCompatActivity {

    private static final long DOT_INTERVAL_MS = 500L;    // обновление точек
    private static final long TOTAL_DELAY_MS = 5000L;    // 5 секунд до автоматического перехода
    private static final long TAP_RESET_MS = 1500L;      // время для сброса счёта тапов (1.5s)

    private final String BASE_TEXT = "ZLS running";

    private TextView statusView;
    private View rootView;
    private Handler handler;
    private int dotIndex = 0;
    private final String[] dots = {".", "..", "..."};

    // Счётчик тапов и сброс
    private int tapCount = 0;

    private final Runnable dotRunnable = new Runnable() {
        @Override
        public void run() {
            if (statusView != null) {
                String text = BASE_TEXT + dots[dotIndex];
                statusView.setText(text);
                dotIndex = (dotIndex + 1) % dots.length;
            }
            handler.postDelayed(this, DOT_INTERVAL_MS);
        }
    };

    // Переход по таймеру во внутреннюю activity (exported=false)
    private final Runnable navigateRunnable = new Runnable() {
        @Override
        public void run() {
            cancelAllPending(); // аккуратно убираем все callbacks
            Intent intent = new Intent(ZLSActivity.this, LockActivity.class);
            startActivity(intent);
            finish();
        }
    };

    // Сброс счёта тапов (если тапов не было TAP_RESET_MS)
    private final Runnable resetTapRunnable = new Runnable() {
        @Override
        public void run() {
            tapCount = 0;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zls);

        // action bar: заголовок "ZLS" и фиолетовый фон
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle("ZLS");
            ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#9C27B0")));
        }

        statusView = findViewById(R.id.zls_status);
        rootView = findViewById(R.id.zls_root);

        handler = new Handler(Looper.getMainLooper());

        // Запускаем точечную анимацию сразу
        handler.post(dotRunnable);

        // Запланированный переход через TOTAL_DELAY_MS (5 секунд)
        handler.postDelayed(navigateRunnable, TOTAL_DELAY_MS);

        // Обрабатываем тап в любом месте: считаем тап-апы и ждем 4
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // считаем только поднятия пальца, чтобы избежать множественных срабатываний при движении
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    tapCount++;
                    // сбрасываем предыдущий reset и ставим новый
                    handler.removeCallbacks(resetTapRunnable);
                    handler.postDelayed(resetTapRunnable, TAP_RESET_MS);

                    if (tapCount >= 4) {
                        // При 4 тапах: отменяем автоматический переход и идём в LaunchActivity
                        handler.removeCallbacks(navigateRunnable);
                        handler.removeCallbacks(dotRunnable);
                        handler.removeCallbacks(resetTapRunnable);
                        // Можно также сразу показать какой-то визуальный отклик, но по задаче — просто переход
                        Intent i = new Intent(ZLSActivity.this, LaunchActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
                // Возвращаем true — потребляем событие, чтобы не было propagation
                return true;
            }
        });
    }

    private void cancelAllPending() {
        if (handler != null) {
            handler.removeCallbacks(dotRunnable);
            handler.removeCallbacks(navigateRunnable);
            handler.removeCallbacks(resetTapRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelAllPending();
    }
}
