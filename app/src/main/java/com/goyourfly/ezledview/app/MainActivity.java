package com.goyourfly.ezledview.app;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.goyourfly.ezledview.EZLedView;

public class MainActivity extends AppCompatActivity {
    private EZLedView ledLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ledLayout = (EZLedView) findViewById(R.id.ledLayout);

        final TextView textView = new TextView(this);
        textView.setTextSize(200);
        textView.setText("我爱你，么么哒");
        ledLayout.setLEDView(textView);
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(200 * 4, 200);
//        textView.setLayoutParams(layoutParams);
//        ledLayout.setLEDView(getLayoutInflater().inflate(R.layout.custom_layout,null));


        SeekBar circleRadius = (SeekBar) findViewById(R.id.seekbarCircle);
        circleRadius.setProgress(ledLayout.getRadius());
        circleRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ledLayout.setRadius(progress);
                ledLayout.setLEDView(textView);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar textSize = (SeekBar) findViewById(R.id.seekbarTextSize);
        textSize.setProgress(200);
        textSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setTextSize(progress);
                ledLayout.setLEDView(textView);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });




//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 4, bitmap.getHeight() * 4, false);
//        ledLayout.setBitmap(newBitmap);

//        ledLayout.setLEDView(getLayoutInflater().inflate(R.layout.custom_imageview,null));
    }

//    public void onSubmitClick(View view) {
//        EditText text = (EditText) findViewById(R.id.edit_text);
//        TextView textView = new TextView(this);
//        textView.setTextSize(200);
//        textView.setText(text.getText());
//        textView.setTextColor(Color.BLUE);
//        ledLayout.setLEDView(textView);
//    }




}
