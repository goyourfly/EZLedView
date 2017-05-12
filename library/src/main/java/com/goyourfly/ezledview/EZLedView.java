package com.goyourfly.ezledview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * Created by gaoyufei on 2017/5/12.
 */

public class EZLedView extends View{
    private static final String TAG = "EZLedLayout";
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap tempBitmap;
    private EZLedHelper helper;
    public EZLedView(@NonNull Context context) {
        super(context);
        init();
    }

    public EZLedView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EZLedView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        helper = new EZLedHelper();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(tempBitmap != null){
            Log.d(TAG,"Width:" + tempBitmap.getWidth());
            setMeasuredDimension(tempBitmap.getWidth(),tempBitmap.getHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(tempBitmap != null){
            canvas.drawBitmap(tempBitmap,0,0,mPaint);
        }
    }

    public void setBitmap(Bitmap bitmap){
        this.tempBitmap = bitmap;
        if(tempBitmap != null) {
            tempBitmap = helper.getLedBitmap(tempBitmap);
            requestLayout();
            invalidate();
        }
    }

    public void setLEDView(View view){
        setBitmap(loadBitmapFromView(view));
    }

    public static Bitmap loadBitmapFromView(View v) {
        if (v.getMeasuredHeight() <= 0) {
            v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
            v.draw(c);
            return b;
        }
        Bitmap b = Bitmap.createBitmap(v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }
}
