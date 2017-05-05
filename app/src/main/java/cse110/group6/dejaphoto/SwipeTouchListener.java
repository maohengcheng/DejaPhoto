package cse110.group6.dejaphoto;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Michael on 5/3/2017.
 * class adapted from:
 * http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
 */

class SwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public SwipeListener(Context context){
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public GestureDetector getGestureDetector(){
        return gestureDetector;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int swipeDistanceThreshold = 80;
        private static final int swipeSpeedThreshold = 80;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float xCoordinate, float yCoordinate) {
            boolean result = false;
            try {
                double diffY = e2.getY() - e1.getY();
                double diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > swipeDistanceThreshold && Math.abs(xCoordinate) > swipeSpeedThreshold) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > swipeDistanceThreshold && Math.abs(yCoordinate) > swipeSpeedThreshold) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }
}
