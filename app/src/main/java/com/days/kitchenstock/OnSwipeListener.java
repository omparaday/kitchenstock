package com.days.kitchenstock;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class OnSwipeListener implements OnTouchListener {

    static final int BUTTONS_WIDTH = 120;
    static final int MIN_DISTANCE_TO_SWIPE = 60;
    static final int MIN_DISTANCE_TO_START_MOVE = 30;
    private float downX, downY, upX, upY;

    public boolean onSwipeLeft() {
        return false;
    }

    public boolean onMoveLeft(float deltaX) {
        return false;
    }

    public boolean onDown() {
        return false;
    }

    public boolean onCancel() {
        return false;
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return onDown();
            }
            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                // swipe horizontal?
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (Math.abs(deltaX) > convertDpToPixel(MIN_DISTANCE_TO_SWIPE, v.getContext())) {
                        // left or right
                        if (deltaX > 0) {
                            return this.onSwipeLeft();
                        }
                    }
                }
                onCancel();
                return false; // We don't consume the event
            }
            case MotionEvent.ACTION_MOVE: {
                float curX = event.getX();
                float curY = event.getY();
                float deltaX = downX - curX;
                float deltaY = downY - curY;
                // swipe horizontal?
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > convertDpToPixel(MIN_DISTANCE_TO_START_MOVE, v.getContext())) {
                        return this.onMoveLeft(deltaX);
                    }
                }
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;

                if (Math.abs(deltaX) > convertDpToPixel(MIN_DISTANCE_TO_SWIPE, v.getContext())) {
                    // left or right
                    if (deltaX > 0) {
                        this.onSwipeLeft();
                        return false;
                    }
                }
                onCancel();
                return false;
        }
        return false;
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

}