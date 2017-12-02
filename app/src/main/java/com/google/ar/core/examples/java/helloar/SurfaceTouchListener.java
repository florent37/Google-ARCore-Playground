package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SurfaceTouchListener implements View.OnTouchListener{
    public interface Listener {
        boolean onSingleTap(MotionEvent event);
        boolean onTouchEvent(MotionEvent event);
    }

    @NonNull
    private final GestureDetector mGestureDetector;
    @Nullable
    private final Listener mListener;

    public SurfaceTouchListener(Context context, @Nullable Listener listener) {
        this.mListener = listener;

        //we only use singleTap
        this.mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mListener != null) {
                    return mListener.onSingleTap(e);
                } else {
                    return true;
                }
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

        if (mListener != null) {
            return mListener.onTouchEvent(event); //for drawing
        } else {
            return true;
        }
    }

}
