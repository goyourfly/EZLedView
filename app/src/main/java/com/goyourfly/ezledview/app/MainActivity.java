package com.goyourfly.ezledview.app;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.goyourfly.ezledview.EZLedView;

public class MainActivity extends AppCompatActivity {
    private EZLedView ledView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ledView = (EZLedView) findViewById(R.id.ledView);

        SeekBar circleRadius = (SeekBar) findViewById(R.id.seekbarCircle);
        circleRadius.setProgress(ledView.getLedRadius());
        circleRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress < 2)
                    return;
                ledView.setLedRadius(progress);
                ledView.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar ledSpace = (SeekBar) findViewById(R.id.seekbarSpace);
        ledSpace.setProgress(ledView.getLedSpace());
        ledSpace.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ledView.setLedSpace(progress);
                ledView.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar textSize = (SeekBar) findViewById(R.id.seekbarTextSize);
        textSize.setProgress(ledView.getLedTextSize());
        textSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ledView.setLedTextSize(progress);
                ledView.requestLayout();
                ledView.invalidate();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
//
        setRadioCheckListener(R.id.led_type, new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if(checkedId == R.id.rb_text){
                    ledView.setText("HELLO, I LOVE U VERY MUCH!!!");
                }else {
                    ledView.setDrawable(getResources().getDrawable(R.drawable.simpson));
                }
            }
        });
//
        setRadioCheckListener(R.id.point_type, new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if(checkedId == R.id.rb_circle){
                    ledView.setLedType(EZLedView.LED_TYPE_CIRCLE);
                }else if(checkedId == R.id.rb_square){
                    ledView.setLedType(EZLedView.LED_TYPE_SQUARE);
                }else {
                    ledView.setLedLightDrawable(getResources().getDrawable(R.drawable.ic_star_black_24dp));
                    ledView.setLedType(EZLedView.LED_TYPE_DRAWABLE);
                }
                ledView.invalidate();
            }
        });

    }

    private void setRadioCheckListener(int id, RadioGroup.OnCheckedChangeListener listener){
        RadioGroup radioGroup = (RadioGroup) findViewById(id);
        radioGroup.setOnCheckedChangeListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_led)
            startActivity(new Intent(this,LedDisplayActivity.class));
        return super.onOptionsItemSelected(item);
    }
}
