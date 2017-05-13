package com.goyourfly.ezledview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyufei on 2017/5/12.
 */

public class EZLedView extends View {
    private static final String TAG = "EZLedLayout";

    public static final String LED_TYPE_CIRCLE = "1";
    public static final String LED_TYPE_SQUARE = "2";
    public static final String LED_TYPE_DRAWABLE = "3";

    public static final String CONTENT_TYPE_TEXT = "1";
    public static final String CONTENT_TYPE_IMAGE = "2";

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Drawable ledDrawable;
    private int ledSpace = 4;
    private int ledRadius;
    private int ledColor;
    private int ledTextSize = 50;
    private String ledType;
    private Drawable ledLightDrawable;
    private List<Point> circlePoint = new ArrayList<>();
    private CharSequence ledText;


    private Drawable ledImage;
    private int mDrawableWidth;
    private int mDrawableHeight;
    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;
    private boolean sCompatAdjustViewBounds;

    private int mPaddingLeft = 0;
    private int mPaddingRight = 0;
    private int mPaddingTop = 0;
    private int mPaddingBottom = 0;

    private String contentType = CONTENT_TYPE_TEXT;


    public EZLedView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public EZLedView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EZLedView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        final int targetSdkVersion = getContext().getApplicationInfo().targetSdkVersion;
        sCompatAdjustViewBounds = targetSdkVersion <= Build.VERSION_CODES.JELLY_BEAN_MR1;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);

        if (attrs == null)
            return;
        TypedArray attributes = getContext().obtainStyledAttributes(attrs,
                R.styleable.EZLedView);
        ledRadius = attributes.getDimensionPixelSize(R.styleable.EZLedView_led_radius, 10);
        ledSpace = attributes.getDimensionPixelOffset(R.styleable.EZLedView_led_space, 5);
        ledTextSize = attributes.getDimensionPixelOffset(R.styleable.EZLedView_text_size, 100);

        ledColor = attributes.getColor(R.styleable.EZLedView_led_color, 0);
        ledType = attributes.getString(R.styleable.EZLedView_led_type);
        if (TextUtils.isEmpty(ledText))
            ledType = LED_TYPE_CIRCLE;

        if (ledType.equals(LED_TYPE_DRAWABLE)) {
            int ledLightId = attributes.getResourceId(R.styleable.EZLedView_led_light, 0);
            if (ledLightId != 0) {
                ledLightDrawable = getResources().getDrawable(ledLightId);
            }
            if (ledLightDrawable == null)
                throw new RuntimeException("Drawable type need you set a image");
        }

        contentType = attributes.getString(R.styleable.EZLedView_content_type);
        if(TextUtils.isEmpty(contentType)){
            contentType = CONTENT_TYPE_TEXT;
        }

        int ledImageId = attributes.getResourceId(R.styleable.EZLedView_image, 0);
        if (ledImageId != 0) {
            ledImage = getResources().getDrawable(ledImageId);
        }
        ledText = attributes.getText(R.styleable.EZLedView_text);

        paint.setColor(ledColor);
        paint.setTextSize(ledTextSize);

        if (!TextUtils.isEmpty(ledText)) {
            setText(ledText);
        } else if (ledImage != null) {
            setDrawable(ledImage);
        } else {
            throw new NullPointerException("Neither ledImage nor ledText is available");
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w;
        int h;

        final int pleft = mPaddingLeft;
        final int pright = mPaddingRight;
        final int ptop = mPaddingTop;
        final int pbottom = mPaddingBottom;
        // Desired aspect ratio of the view's contents (not including padding)
        float desiredAspect = 0.0f;

        // We are allowed to change the view's mWidth
        boolean resizeWidth = false;

        // We are allowed to change the view's mHeight
        boolean resizeHeight = false;


        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);


        w = mDrawableWidth;
        h = mDrawableHeight;
        if (w <= 0) w = 1;
        if (h <= 0) h = 1;

        // We are supposed to adjust view bounds to match the aspect
        // ratio of our drawable. See if that is possible.
        resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
        resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;

        desiredAspect = (float) w / (float) h;

        int widthSize;
        int heightSize;


        if (resizeWidth || resizeHeight) {

            // Get the max possible mWidth given our constraints
            widthSize = resolveAdjustedSize(w + pleft + pright, mMaxWidth, widthMeasureSpec);

            // Get the max possible mHeight given our constraints
            heightSize = resolveAdjustedSize(h + ptop + pbottom, mMaxHeight, heightMeasureSpec);

            // See what our actual aspect ratio is
            final float actualAspect = (float) (widthSize - pleft - pright) /
                    (heightSize - ptop - pbottom);
            if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {
                boolean done = false;

                // Try adjusting mWidth to be proportional to mHeight
                if (resizeWidth) {
                    int newWidth = (int) (desiredAspect * (heightSize - ptop - pbottom)) +
                            pleft + pright;

                    // Allow the mWidth to outgrow its original estimate if mHeight is fixed.
                    if (!resizeHeight && !sCompatAdjustViewBounds) {
                        widthSize = resolveAdjustedSize(newWidth, mMaxWidth, widthMeasureSpec);
                    }

                    if (newWidth <= widthSize) {
                        widthSize = newWidth;
                        done = true;
                    }
                }

                // Try adjusting mHeight to be proportional to mWidth
                if (!done && resizeHeight) {
                    int newHeight = (int) ((widthSize - pleft - pright) / desiredAspect) +
                            ptop + pbottom;

                    // Allow the mHeight to outgrow its original estimate if mWidth is fixed.
                    if (!resizeWidth && !sCompatAdjustViewBounds) {
                        heightSize = resolveAdjustedSize(newHeight, mMaxHeight,
                                heightMeasureSpec);
                    }

                    if (newHeight <= heightSize) {
                        heightSize = newHeight;
                    }
                }
            }
        } else {
            /* We are either don't want to preserve the drawables aspect ratio,
               or we are not allowed to change view dimensions. Just measure in
               the normal way.
            */
            w += pleft + pright;
            h += ptop + pbottom;

            w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());

            widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
            heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    private int resolveAdjustedSize(int desiredSize, int maxSize,
                                    int measureSpec) {
        int result = desiredSize;
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                /* Parent says we can be as big as we want. Just don't be larger
                   than max size imposed on ourselves.
                */
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.AT_MOST:
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(Math.min(desiredSize, specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = null;
        if(contentType.equals(CONTENT_TYPE_TEXT)){
            drawable = getDrawable(loadBitmapFromText(ledText,paint));
        }else if(contentType.equals(CONTENT_TYPE_IMAGE)){
            drawable = getDrawable(loadBitmapFromDrawable(ledImage,getWidth(),getHeight()));
        }

        if (drawable != null) {
            drawable.draw(canvas);
        }
    }


    @Deprecated
    public void setLEDView(View view) {

    }

    public void setText(CharSequence text) {
        this.contentType = CONTENT_TYPE_TEXT;
        this.ledText = text;
        Rect rect = new Rect();
        paint.getTextBounds(text.toString(), 0, text.length(), rect);

        Paint.FontMetrics m = paint.getFontMetrics();
        mDrawableWidth = rect.width();
        mDrawableHeight = (int) (m.bottom - m.ascent);
        requestLayout();
        invalidate();
    }

    public void setDrawable(Drawable drawable) {
        this.contentType = CONTENT_TYPE_IMAGE;
        this.ledImage = drawable;
        mDrawableWidth = drawable.getIntrinsicWidth();
        mDrawableHeight = drawable.getIntrinsicHeight();
        requestLayout();
        invalidate();
    }

    private Drawable getDrawable(Bitmap bitmap) {
        if (bitmap != null) {
            release();
            measureBitmap(bitmap);
            Bitmap ledBitmap = generateLedBitmap(bitmap);
            ledDrawable = new BitmapDrawable(getResources(), ledBitmap);
            ledDrawable.setBounds(0,0,ledBitmap.getWidth(),ledBitmap.getHeight());
            Log.d(TAG, "Bound:" + ledDrawable.getBounds());
            return ledDrawable;
        }
        return null;
    }

    private static Bitmap loadBitmapFromText(CharSequence text, Paint paint) {
        int width = (int) paint.measureText(text.toString());
        DynamicLayout dynamicLayout = new DynamicLayout(
                text,
                new TextPaint(paint),
                width,
                Layout.Alignment.ALIGN_CENTER,
                0,
                0,
                false
        );
        int height = dynamicLayout.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        dynamicLayout.draw(canvas);
        return bitmap;
    }

    private static Bitmap loadBitmapFromDrawable(Drawable drawable,int width,int height) {
        Bitmap bitmap = getBitmapFromDrawable(drawable);
        return Bitmap.createScaledBitmap(bitmap,width,height,true);
    }

    private static Bitmap loadBitmapFromView(View v) {
        if (v.getMeasuredHeight() <= 0 || v.getLayoutParams() == null) {
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


    private Bitmap generateLedBitmap(Bitmap src) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (Point point : circlePoint) {
            int color = isInCircle(src, point.x, point.y);
            if (color != 0) {
                if (ledColor != 0) {
                    color = ledColor;
                }
                paint.setColor(color);

                if (LED_TYPE_CIRCLE.equals(ledType)) {
                    canvas.drawCircle(point.x, point.y, ledRadius, paint);
                } else if (LED_TYPE_SQUARE.equals(ledType)) {
                    canvas.drawRect(point.x - ledRadius, point.y - ledRadius, point.x + ledRadius, point.y + ledRadius, paint);
                } else if (LED_TYPE_DRAWABLE.equals(ledType)) {
                    ledLightDrawable.setBounds(point.x - ledRadius, point.y - ledRadius, point.x + ledRadius, point.y + ledRadius);
                    ledLightDrawable.draw(canvas);
                }
            }
        }
        return bitmap;
    }

    public void release() {
        ledDrawable = null;
        circlePoint.clear();
    }


    private void measureBitmap(Bitmap bitmap) {
        measurePoint(bitmap.getWidth(),bitmap.getHeight());
    }


    /**
     * Calculate the need drawing point
     */
    private void measurePoint(int width,int height) {
        int halfBound = ledRadius + ledSpace / 2;
        int x = halfBound;
        int y = halfBound;
        for (; ; ) {
            for (; ; ) {
                circlePoint.add(new Point(x, y));
                y += halfBound * 2;
                if (y > height) {
                    y = halfBound;
                    break;
                }
            }
            x += halfBound * 2;
            if (x > width) {
                break;
            }
        }
    }

    private int isInCircleLeft(Bitmap bitmap, int x, int y) {
        if (x - ledRadius > 0 && x - ledRadius < bitmap.getWidth()
                && y > 0 && y < bitmap.getHeight()) {
            int pxL = bitmap.getPixel(x - ledRadius, y);
            if (pxL != 0)
                return pxL;
        }
        return 0;
    }

    private int isInCircleTop(Bitmap bitmap, int x, int y) {
        if (y - ledRadius > 0 && y - ledRadius < bitmap.getHeight()
                && x > 0 && x < bitmap.getWidth()) {
            int pxT = bitmap.getPixel(x, y - ledRadius);
            if (pxT != 0)
                return pxT;
        }
        return 0;
    }

    private int isInCircleRight(Bitmap bitmap, int x, int y) {
        if (x + ledRadius > 0 && x + ledRadius < bitmap.getWidth()
                && y > 0 && y < bitmap.getHeight()) {
            int pxR = bitmap.getPixel(x + ledRadius, y);
            if (pxR != 0)
                return pxR;
        }
        return 0;
    }


    private int isInCircleBottom(Bitmap bitmap, int x, int y) {
        if (y + ledRadius > 0 && y + ledRadius < bitmap.getHeight()
                && x > 0 && x < bitmap.getWidth()) {
            int pxB = bitmap.getPixel(x, y + ledRadius);
            if (pxB != 0)
                return pxB;
        }
        return 0;
    }

    private int isInCircleCenter(Bitmap bitmap, int x, int y) {
        if (y > 0 && y < bitmap.getHeight()
                && x > 0 && x < bitmap.getWidth()) {
            int pxC = bitmap.getPixel(x, y);
            if (pxC != 0)
                return pxC;
        }
        return 0;
    }

    private int isInCircle(Bitmap bitmap, int x, int y) {
        if (bitmap == null)
            return 0;
        int pxL = isInCircleLeft(bitmap, x, y);
        int pxT = isInCircleTop(bitmap, x, y);
        int pxR = isInCircleRight(bitmap, x, y);
        int pxB = isInCircleBottom(bitmap, x, y);
        int pxC = isInCircleCenter(bitmap, x, y);

        int num = 0;
        if (pxL != 0) {
            num++;
        }
        if (pxT != 0) {
            num++;
        }
        if (pxR != 0) {
            num++;
        }
        if (pxB != 0) {
            num++;
        }
        if (pxC != 0) {
            num++;
        }
        if (num >= 2) {
            int a = Color.alpha(pxL) + Color.alpha(pxT) + Color.alpha(pxR) + Color.alpha(pxB) + Color.alpha(pxC);
            int r = Color.red(pxL) + Color.red(pxT) + Color.red(pxR) + Color.red(pxB) + Color.red(pxC);
            int g = Color.green(pxL) + Color.green(pxT) + Color.green(pxR) + Color.green(pxB) + Color.green(pxC);
            int b = Color.blue(pxL) + Color.blue(pxT) + Color.blue(pxR) + Color.blue(pxB) + Color.blue(pxC);
            return Color.argb(a / 5, r / 5, g / 5, b / 5);
        }

        return 0;
    }

    private static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ALPHA_8);
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

    public int getLedSpace() {
        return ledSpace;
    }

    public void setLedSpace(int ledSpace) {
        this.ledSpace = ledSpace;
    }

    public int getLedRadius() {
        return ledRadius;
    }

    public void setLedRadius(int ledRadius) {
        this.ledRadius = ledRadius;
    }

    public int getLedColor() {
        return ledColor;
    }

    public void setLedColor(int ledColor) {
        this.ledColor = ledColor;
    }

    public int getLedTextSize() {
        return ledTextSize;
    }

    public void setLedTextSize(int ledTextSize) {
        this.ledTextSize = ledTextSize;
    }

    public String getLedType() {
        return ledType;
    }

    public void setLedType(String ledType) {
        this.ledType = ledType;
    }

    public Drawable getLedLightDrawable() {
        return ledLightDrawable;
    }

    public void setLedLightDrawable(Drawable ledLightDrawable) {
        this.ledLightDrawable = ledLightDrawable;
    }

    public CharSequence getLedText() {
        return ledText;
    }

    public void setLedText(CharSequence ledText) {
        this.ledText = ledText;
    }
}
