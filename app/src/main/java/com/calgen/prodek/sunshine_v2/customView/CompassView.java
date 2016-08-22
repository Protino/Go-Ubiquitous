package com.calgen.prodek.sunshine_v2.customView;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Gurupad on 21-Aug-16.
 */
public class CompassView extends View {
    Paint mCircleBorderPaint;
    Paint mCircleFillPaint;
    Paint mArrowPaint;
    int mHeight;
    int mWidth;

    int speed;
    int direction;

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompassView(Context context) {
        super(context);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if(hSpecMode == MeasureSpec.EXACTLY)
            mHeight = hSpecSize;
        else if (hSpecMode == MeasureSpec.AT_MOST){
        }
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        if (speed > -1 && speed < 3)
            this.speed = speed;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        if (direction > -1 && direction < 8)
            this.direction = direction;
    }

    private void init(){
        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }


}
