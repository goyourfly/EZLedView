package com.goyourfly.ezledview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
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
 * 这是一个类似于模拟LED灯效果的自定义View
 * 可以实现将文本和图片转换成LED效果
 */

public class EZLedView extends View {
    private static final String TAG = "EZLedLayout";

    /**
     * Led light show shape
     * 1.circle shape
     * 2.square shape
     * 3.custom shape
     */
    public static final String LED_TYPE_CIRCLE = "1";
    public static final String LED_TYPE_SQUARE = "2";
    public static final String LED_TYPE_DRAWABLE = "3";

    /**
     * Content type,text or image
     */
    public static final String CONTENT_TYPE_TEXT = "1";
    public static final String CONTENT_TYPE_IMAGE = "2";

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Drawable ledDrawableTemp;


    /**
     * Led light space
     */
    private int ledSpace;

    /**
     * Led light radius
     */
    private int ledRadius;
    /**
     * Led color, if content is image,this param not work
     */
    private int ledColor;

    /**
     * Content text size
     */
    private int ledTextSize;

    /**
     * Content type, text or image
     */
    private String ledType;

    /**
     * Custom led light drawable
     */
    private Drawable customLedLightDrawable;

    /**
     * Store the points of all px
     */
    private List<Point> circlePoint = new ArrayList<>();

    /**
     * Content of text
     */
    private CharSequence ledText;


    /**
     * Content of image
     */
    private Drawable ledImage;

    /**
     * The content width and height
     */
    private int mDrawableWidth;
    private int mDrawableHeight;

    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;
    private boolean sCompatAdjustViewBounds;

    /**
     * Padding have not added , so not working
     */
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

    /**
     * Read the xml config and init
     * @param attrs the xml config
     */
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
        ledSpace = attributes.getDimensionPixelOffset(R.styleable.EZLedView_led_space, 2);
        ledTextSize = attributes.getDimensionPixelOffset(R.styleable.EZLedView_text_size, 100);

        ledColor = attributes.getColor(R.styleable.EZLedView_led_color, 0);
        ledType = attributes.getString(R.styleable.EZLedView_led_type);
        if (TextUtils.isEmpty(ledText))
            ledType = LED_TYPE_CIRCLE;

        if (ledType.equals(LED_TYPE_DRAWABLE)) {
            int ledLightId = attributes.getResourceId(R.styleable.EZLedView_led_light, 0);
            if (ledLightId != 0) {
                customLedLightDrawable = getResources().getDrawable(ledLightId);
            }
            if (customLedLightDrawable == null)
                throw new RuntimeException("Drawable type need you set a image");
        }

        contentType = attributes.getString(R.styleable.EZLedView_content_type);
        if (TextUtils.isEmpty(contentType)) {
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

    /**
     * Resize the view if need
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Text type not need resize
        if(contentType.equals(CONTENT_TYPE_TEXT)){
            setMeasuredDimension(mDrawableWidth,mDrawableHeight);
            return;
        }
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
        Bitmap bitmap = null;
        if (contentType.equals(CONTENT_TYPE_TEXT)) {
            bitmap = getDrawable(renderText(ledText, paint));
        } else if (contentType.equals(CONTENT_TYPE_IMAGE)) {
            bitmap = getDrawable(renderDrawable(ledImage, getWidth(), getHeight()));
        }

        if (bitmap != null) {
            int maxWidth = 1024 * 2;
            if(bitmap.getWidth() > maxWidth){
                for (int i =0; i < Math.round(bitmap.getWidth()/(float)maxWidth); i++){
                    int x = i * maxWidth;
                    int width = maxWidth;
                    if(x + width > bitmap.getWidth()){
                        width = bitmap.getWidth() - x;
                    }
                    Bitmap newBitmap = Bitmap.createBitmap(bitmap,x,0,width,bitmap.getHeight());
                    canvas.drawBitmap(newBitmap,x,0,paint);
                }
            }else {
                canvas.drawBitmap(bitmap,0,0,paint);
            }
        }
    }


    @Deprecated
    private void setLEDView(View view) {

    }

    /**
     * Set text content
     * @param text content
     */
    public void setText(CharSequence text) {
        this.contentType = CONTENT_TYPE_TEXT;
        this.ledText = text;
        measureTextBound(text.toString());
        requestLayout();
        invalidate();
    }


    /**
     * Set drawable content
     * @param drawable drawable
     */
    public void setDrawable(Drawable drawable) {
        this.contentType = CONTENT_TYPE_IMAGE;
        this.ledImage = drawable;
        mDrawableWidth = drawable.getIntrinsicWidth();
        mDrawableHeight = drawable.getIntrinsicHeight();
        requestLayout();
        invalidate();
    }

    private Bitmap getDrawable(Bitmap bitmap) {
        if (bitmap != null) {
            release();
            measureBitmap(bitmap);
            return generateLedBitmap(bitmap);
        }
        return null;
    }

    /**
     * measure the text width and height
     * @param text text content
     */
    private void measureTextBound(String text) {
        Paint.FontMetrics m = paint.getFontMetrics();
        mDrawableWidth = (int) paint.measureText(text);
        mDrawableHeight = (int) (m.bottom - m.ascent);
    }

    /**
     * Transform text to bitmap
     * @param text text content
     * @param paint paint
     * @return the bitmap of text
     */
    private static Bitmap renderText(CharSequence text, Paint paint) {
        int width = (int) paint.measureText(text.toString());
//        DynamicLayout dynamicLayout = new DynamicLayout(
//                text,
//                new TextPaint(paint),
//                width,
//                Layout.Alignment.ALIGN_CENTER,
//                0,
//                0,
//                false
//        );
        Paint.FontMetrics m = paint.getFontMetrics();
        int height = (int) (m.bottom - m.ascent);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
//        dynamicLayout.draw(canvas);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        canvas.drawText(text.toString(),0,yPos,paint);
        return bitmap;
    }

    /**
     * Transform the image drawable to bitmap
     * @param drawable the content drawable
     * @param width the new bitmap width
     * @param height the new bitmap height
     * @return bitmap of drawable
     */
    private static Bitmap renderDrawable(Drawable drawable, int width, int height) {
        Bitmap bitmap = getBitmapFromDrawable(drawable);
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    @Deprecated
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


    /**
     * Transform a bitmap to a led bitmap
     * @param src the original bitmap
     * @return led bitmap
     */
    private Bitmap generateLedBitmap(Bitmap src) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (Point point : circlePoint) {
            // Detect if the px is in range of our led position
            int color = isInRange(src, point.x, point.y);
            if (color != 0) {
                if (ledColor != 0 && !contentType.equals(CONTENT_TYPE_IMAGE)) {
                    color = ledColor;
                }
                paint.setColor(color);

                // draw shape according to ledType
                if (LED_TYPE_CIRCLE.equals(ledType)) {
                    canvas.drawCircle(point.x, point.y, ledRadius, paint);
                } else if (LED_TYPE_SQUARE.equals(ledType)) {
                    canvas.drawRect(point.x - ledRadius, point.y - ledRadius, point.x + ledRadius, point.y + ledRadius, paint);
                } else if (LED_TYPE_DRAWABLE.equals(ledType)) {
                    customLedLightDrawable.setBounds(point.x - ledRadius, point.y - ledRadius, point.x + ledRadius, point.y + ledRadius);
                    customLedLightDrawable.draw(canvas);
                }
            }
        }
        return bitmap;
    }

    public void release() {
        ledDrawableTemp = null;
        circlePoint.clear();
    }


    private void measureBitmap(Bitmap bitmap) {
        measurePoint(bitmap.getWidth(), bitmap.getHeight());
    }


    /**
     * Calculate the led point position
     */
    private void measurePoint(int width, int height) {
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

    /**
     * Measure if x and y is in range of leds
     * @param bitmap the origin bitmap
     * @param x led x
     * @param y led y
     * @return the color , if color is zero means empty
     */
    private int isInRange(Bitmap bitmap, int x, int y) {
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

    /**
     * Get bitmap from drawable, Copy from CircleImageView
     * @param drawable the drawable
     * @return the bitmap of drawable
     */
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
        paint.setColor(ledColor);
    }

    public int getLedTextSize() {
        return ledTextSize;
    }

    public void setLedTextSize(int ledTextSize) {
        if(ledText == null)
            throw new NullPointerException("Please set ledText before setLedTextSize");
        this.ledTextSize = ledTextSize;
        measureTextBound(ledText.toString());
        paint.setTextSize(ledTextSize);
    }

    public String getLedType() {
        return ledType;
    }

    public void setLedType(String ledType) {
        this.ledType = ledType;
    }

    public Drawable getLedLightDrawable() {
        return customLedLightDrawable;
    }

    public void setLedLightDrawable(Drawable ledLightDrawable) {
        this.customLedLightDrawable = ledLightDrawable;
    }

    public CharSequence getLedText() {
        return ledText;
    }

    public void setLedText(CharSequence ledText) {
        this.ledText = ledText;
    }
}
