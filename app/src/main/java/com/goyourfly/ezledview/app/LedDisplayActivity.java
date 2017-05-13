package com.goyourfly.ezledview.app;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;

import com.goyourfly.ezledview.EZLedView;

public class LedDisplayActivity extends AppCompatActivity {
    Handler handler = new Handler();
    int scrollX = 0;
    int direct = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_led_display);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        final HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.scrollView);
        final EZLedView ledView = (EZLedView) findViewById(R.id.ledView);

        handler.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(scrollX, 0);
                scrollX += (ledView.getLedRadius() + ledView.getLedSpace()) * direct;
                if (scrollX <= 0 || scrollX >= ledView.getWidth() - scrollView.getWidth()) {
                    direct = -direct;
                }
                handler.postDelayed(this, 10);
            }
        });

    }
}
