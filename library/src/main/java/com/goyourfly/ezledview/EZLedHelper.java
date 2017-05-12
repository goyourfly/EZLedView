package com.goyourfly.ezledview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyufei on 2017/5/12.
 */

public class EZLedHelper {
    private static final String TAG = "EZLedHelper";
    private Bitmap tempBitmap;
    private int width;
    private int height;
    private int circleBound = 50;
    private int circleSpacePx = 4;
    private int circleRadius;
    private int specificColor = 0;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<Point> circlePoint = new ArrayList<>();

    public EZLedHelper(){
        paint.setColor(Color.BLACK);
    }

    public void setCircleRadius(int circle){
        circleBound = circle;
    }

    public void setSpace(int space){
        this.circleSpacePx = space;
    }

    public void setColor(int color){
        this.specificColor = color;
    }
    public int getSpace(){
        return circleSpacePx;
    }

    public int getRadius(){
        return circleBound;
    }

    public int getColor(){
        return specificColor;
    }
    public Bitmap getLedBitmap(Bitmap src) {
        setTempBitmap(src);
        Bitmap bitmap = Bitmap.createBitmap(tempBitmap.getWidth(), tempBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (Point point : circlePoint) {
            int color = isInCircle(point.x, point.y);
            if(color != 0) {
                if(specificColor != 0){
                    color = specificColor;
                }
                paint.setColor(color);
                canvas.drawCircle(point.x, point.y, circleRadius, paint);
            }
        }
        return bitmap;
    }

    public void release() {
        tempBitmap.recycle();
        tempBitmap = null;
        circlePoint.clear();
        circlePoint = null;
    }


    private void setTempBitmap(Bitmap tempBitmap) {
        this.tempBitmap = tempBitmap;
        width = tempBitmap.getWidth();
        height = tempBitmap.getHeight();
        measurePoint();
    }


    private void measurePoint() {
        circlePoint.clear();
        int radius =  circleBound/2;
        circleRadius = radius - circleSpacePx / 2;
        int x = radius;
        int y = radius;
        for (; ; ) {
            for (; ; ) {
                circlePoint.add(new Point(x, y));
                y += radius * 2;
                if (y > height) {
                    y = radius;
                    break;
                }
            }
            x += radius * 2;
            if (x > width) {
                break;
            }
        }
    }


    private int isInCircle(int x, int y) {
        if (tempBitmap == null)
            return 0;
        if (y  > 0  && y < tempBitmap.getHeight()
                && x > 0 && x < tempBitmap.getWidth()) {
            int pxC = tempBitmap.getPixel(x, y);
            if (pxC != 0)
                return pxC;
        }
        if (x - circleRadius > 0 && x - circleRadius < tempBitmap.getWidth()
                && y > 0 && y < tempBitmap.getHeight()) {
            int pxL = tempBitmap.getPixel(x - circleRadius, y);
            if (pxL != 0)
                return pxL;
        }
        if (y - circleRadius > 0  && y - circleRadius < tempBitmap.getHeight()
                && x > 0 && x < tempBitmap.getWidth()) {
            int pxT = tempBitmap.getPixel(x, y - circleRadius);
            if (pxT != 0)
                return pxT;
        }

//        if(x + circleRadius < width) {
//            int pxR = tempBitmap.getPixel(x + circleRadius, y);
//            if (pxR != 0)
//                return true;
//        }
//        if(y + circleRadius < height) {
//            int pxB = tempBitmap.getPixel(x, y + circleRadius);
//            if (pxB != 0)
//                return true;
//        }
        return 0;
    }


}
