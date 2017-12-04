package com.google.ar.core.examples.java.helloar.arcoremanager;

import android.Manifest;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.RotationGestureDetector;
import com.google.ar.core.examples.java.helloar.arcoremanager.object.ARCoreObjectDrawer;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import florent37.github.com.rxlifecycle.RxLifecycle;
import io.reactivex.Observable;

public class ArCoreManager {

    private final AppCompatActivity mActivity;

    private final Session mArcoreSession;
    private final Config mDefaultConfig;
    private final Listener mListener;

    private ARCoreRenderer ARCoreRenderer;
    private GLSurfaceView mSurfaceView;

    private ObjectTouchMode touchMode = ObjectTouchMode.SCALE;
    private final Settings mSettings = new Settings();

    public ArCoreManager(AppCompatActivity activity, @NonNull Listener listener) {
        this.mActivity = activity;
        this.mListener = listener;

        // Create default config, check is supported, create session from that config.
        mArcoreSession = new Session(/*context=*/activity);
        mDefaultConfig = Config.createDefaultConfig();

        if (!mArcoreSession.isSupported(mDefaultConfig)) {
            listener.onArCoreUnsuported();
        }
    }

    public void setup(final GLSurfaceView surfaceView) {
        ARCoreRenderer = new ARCoreRenderer(mActivity, mArcoreSession, mSettings);

        this.mSurfaceView = surfaceView;

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(ARCoreRenderer);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        surfaceView.setOnTouchListener(new View.OnTouchListener() {

            private final GestureDetectorCompat mGestureDetector = new GestureDetectorCompat(mActivity, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent event) {
                    ARCoreRenderer.addSingleTapEvent(event);
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    ARCoreRenderer.onTranslate(distanceX, distanceY);
                    return true;
                }
            });

            private ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(mActivity, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                    ARCoreRenderer.onScale(scaleGestureDetector.getScaleFactor());
                    return true;
                }
            });

            private RotationGestureDetector mRotationDetector = new RotationGestureDetector(new RotationGestureDetector.OnRotationGestureListener() {
                @Override
                public void OnRotation(RotationGestureDetector rotationDetector) {
                    ARCoreRenderer.onRotate(rotationDetector.getAngle());
                }
            });

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(mSettings.captureLines.get()){
                    return ARCoreRenderer.handleDrawingTouch(event); //for drawing
                } else {
                    boolean res = true;
                    if (touchMode == ObjectTouchMode.SCALE) {
                        res = mScaleDetector.onTouchEvent(event);
                        if (!mScaleDetector.isInProgress()) {
                            if (mGestureDetector.onTouchEvent(event)) {
                                return false;
                            }
                        }
                        return res;
                    } else if(touchMode == ObjectTouchMode.ROTATE){
                        res = mRotationDetector.onTouchEvent(event);
                        if (!res) {
                            if (mGestureDetector.onTouchEvent(event)) {
                                return false;
                            }
                        }
                    } else if(touchMode == ObjectTouchMode.TRANSLATE){
                        if (mGestureDetector.onTouchEvent(event)) {
                            return false;
                        }
                    }
                    return res;
                }
            }
        });

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        RxLifecycle.with(mActivity)
                .onResume()
                .flatMap($ -> new RxPermissions(mActivity).request(Manifest.permission.CAMERA))
                .flatMap(success -> {
                    if (!success) return Observable.error(new Throwable());
                    else return Observable.just(success);
                })
                .subscribe(event -> {
                    mListener.showLoadingMessage();
                    // Note that order matters - see the note in onPause(), the reverse applies here.
                    mArcoreSession.resume(mDefaultConfig);

                    mSurfaceView.onResume();
                }, throwable -> {
                    mListener.onPermissionNotAllowed();
                });

        RxLifecycle.with(mActivity)
                .onPause()
                .subscribe(event -> {
                    // Note that the order matters - GLSurfaceView is paused first so that it does not try
                    // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
                    // still call mSession.update() and get a SessionPausedException.
                    mSurfaceView.onPause();
                    mArcoreSession.pause();
                });

        ARCoreRenderer.setListener(new ARCoreRenderer.Listener() {
            @Override
            public void hideLoading() {
                if (mListener != null) {
                    mListener.hideLoadingMessage();
                }
            }
        });
    }

    public void addObjectToDraw(ARCoreObjectDrawer arCoreObjectDrawer){
        ARCoreRenderer.addObjectToDraw(arCoreObjectDrawer);
    }

    public void setCaptureLines(boolean captureLines) {
        mSettings.captureLines.set(captureLines);
    }

    public Settings getSettings() {
        return mSettings;
    }

    public void setTouchMode(ObjectTouchMode objectTouchMode) {
        this.touchMode = objectTouchMode;
    }

    public interface Listener {
        void onArCoreUnsuported();

        void onPermissionNotAllowed();

        void showLoadingMessage();

        void hideLoadingMessage();
    }

    public static class Settings {
        public final AtomicBoolean drawBackground = new AtomicBoolean(true);
        public final AtomicBoolean drawPoints = new AtomicBoolean(true);
        public final AtomicBoolean captureLines = new AtomicBoolean(false);
        public final AtomicBoolean drawPlanes = new AtomicBoolean(true);;
        public final AtomicInteger linesColor = new AtomicInteger(Color.WHITE);
        public final AtomicReference<Float> linesDistance = new AtomicReference<Float>(0.125f);
    }

    public enum ObjectTouchMode{
        SCALE,
        ROTATE,
        TRANSLATE
    }
}
