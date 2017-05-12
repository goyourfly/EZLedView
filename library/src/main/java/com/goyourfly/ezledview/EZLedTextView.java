package com.goyourfly.ezledview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by gaoyufei on 2017/5/12.
 */

public class EZLedTextView extends android.support.v7.widget.AppCompatTextView{
    EZLedHelper helper;
    Bitmap ledBitmap;
    Bitmap orignBitmap;

    public EZLedTextView(Context context) {
        super(context);
        init();
    }

    public EZLedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EZLedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        Paint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        helper = new EZLedHelper();
        Bitmap bitmap = generate(getText().toString());
        orignBitmap = bitmap;
        ledBitmap = helper.getLedBitmap(bitmap);
    }

    public void setLED(CharSequence text){
        super.setText(text);
        init();
        invalidate();
    }



    public Bitmap generate(CharSequence text) {
        Log.d("T",text.toString());
        Paint paint = getPaint();
        Paint.FontMetrics fm = paint.getFontMetrics();
        float height = fm.bottom - fm.top + fm.leading;
        int width = (int) paint.measureText(text.toString());

        Bitmap bitmap = Bitmap.createBitmap(width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        StaticLayout layout = new StaticLayout(text, getPaint(),width , Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        layout.draw(canvas);

//        canvas.drawText(text, 0, -fm.top, paint);

        return bitmap;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(ledBitmap,0,0,getPaint());
    }
}
