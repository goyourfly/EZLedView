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
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Created by gaoyufei on 2017/5/12.
 */

public class EZLedGroupLayout extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener{
    private static final String TAG = "EZLedLayout";
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap tempBitmap;
    private View child;
    private EZLedHelper helper;
    public EZLedGroupLayout(@NonNull Context context) {
        super(context);
    }

    public EZLedGroupLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        helper = new EZLedHelper();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public EZLedGroupLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Log.d(TAG,"dispatchDraw:" + child.getDrawingCache());
        if(tempBitmap != null){
            mPaint.setColor(Color.BLACK);
            canvas.drawBitmap(tempBitmap,0,0,mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        child = getChildAt(0);
    }


    @Override
    public void onGlobalLayout() {
        tempBitmap = loadBitmapFromView(child);
        if(tempBitmap != null) {
            tempBitmap = helper.getLedBitmap(tempBitmap);
            child.setVisibility(View.INVISIBLE);
            invalidate();
        }
    }

    public static Bitmap loadBitmapFromView(View v) {
        Log.d(TAG,"Width:" + v.getMeasuredWidth() + "," + v.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap( v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }
}
