package com.google.ar.core.examples.java.helloar.core;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneAttachment;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class AbstractDrawManager {

    protected static final String TAG = "ARCore";

    protected final Context mContext;
    protected final Session mArcoreSession;

    @Nullable
    protected Listener mListener;

    // Tap handling and UI.
    protected final ArrayBlockingQueue<MotionEvent> mQueuedSingleTaps = new ArrayBlockingQueue<>(16);
    protected final ArrayList<PlaneAttachment> mAndroidObjectPositions = new ArrayList<>();

    public AbstractDrawManager(Context mContext, Session mArcoreSession) {
        this.mContext = mContext;
        this.mArcoreSession = mArcoreSession;
    }

    public void addSingleTapEvent(MotionEvent e) {
        // Queue tap if there is space. Tap is lost if queue is full.
        mQueuedSingleTaps.offer(e);
    }

    public void setListener(@Nullable Listener listener) {
        this.mListener = listener;
    }

    public interface Listener {
        void hideLoading();
    }
}
