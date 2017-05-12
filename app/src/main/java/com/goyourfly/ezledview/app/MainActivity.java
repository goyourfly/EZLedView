package com.goyourfly.ezledview.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.goyourfly.ezledview.EZLedView;

public class MainActivity extends AppCompatActivity {
    private EZLedView ledLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ledLayout = (EZLedView) findViewById(R.id.ledLayout);

        TextView textView = new TextView(this);
        textView.setTextSize(200);
        textView.setText("哈哈哈哈");
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(200 * 4,200);
        textView.setLayoutParams(layoutParams);
//        ledLayout.setLEDView(textView);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth() * 4,bitmap.getHeight() * 4,false);
        ledLayout.setBitmap(newBitmap);
    }

    public void onSubmitClick(View view){
//        EZLedTextView t = (EZLedTextView) findViewById(R.id.text);
//        EditText text = (EditText) findViewById(R.id.edit_text);
//        t.setLED(text.getText());
    }
}
