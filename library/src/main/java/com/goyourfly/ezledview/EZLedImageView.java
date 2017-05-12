package com.goyourfly.ezledview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by gaoyufei on 2017/5/12.
 * TODO Not working
 */

public class EZLedImageView extends android.support.v7.widget.AppCompatImageView {
    private static final int COLORDRAWABLE_DIMENSION = 2;
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    EZLedHelper helper;
    Bitmap ledBitmap;

    public EZLedImageView(Context context) {
        super(context);
        init();
    }

    public EZLedImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EZLedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        if(getMeasuredWidth() == 0)
            return;
        helper = new EZLedHelper();
        Bitmap bitmap = getBitmapFromDrawable(getDrawable());
        if(bitmap == null)
            return;
        int width = getMeasuredWidth();
        int height = width * bitmap.getHeight()/bitmap.getWidth();
        Log.d("TAG",width + "," + height + "," + getMeasuredWidth() + "," + getMeasuredHeight());
        Log.d("TAG","Bound:" + calculateBounds());
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap,width,height,false);
        ledBitmap = helper.getLedBitmap(newBitmap);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(ledBitmap != null) {
            mPaint.setColor(Color.BLACK);
            canvas.drawBitmap(ledBitmap, 0, 0, mPaint);
        }
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private RectF calculateBounds() {
        int availableWidth  = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        int sideLength = Math.min(availableWidth, availableHeight);

        float left = getPaddingLeft() + (availableWidth - sideLength) / 2f;
        float top = getPaddingTop() + (availableHeight - sideLength) / 2f;

        return new RectF(left, top, left + sideLength, top + sideLength);
    }
}
