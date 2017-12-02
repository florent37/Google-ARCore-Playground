package com.google.ar.core.examples.java.helloar.arcoremanager;

import android.Manifest;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.DrawManager;
import com.google.ar.core.examples.java.helloar.SurfaceTouchListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.concurrent.atomic.AtomicBoolean;

import florent37.github.com.rxlifecycle.RxLifecycle;
import io.reactivex.Observable;

public class ArCoreManager {

    private final AppCompatActivity mActivity;
    private final Session mArcoreSession;
    private final Config mDefaultConfig;
    private final Listener mListener;
    private final AtomicBoolean mCapturingLines = new AtomicBoolean(false);
    private DrawManager drawManager;
    private GLSurfaceView mSurfaceView;

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
        drawManager = new DrawManager(mActivity, mArcoreSession, mSettings);

        this.mSurfaceView = surfaceView;

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(drawManager);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        surfaceView.setOnTouchListener(new SurfaceTouchListener(mActivity, new SurfaceTouchListener.Listener() {
            @Override
            public boolean onSingleTap(MotionEvent event) {
                if (!mCapturingLines.get()) {
                    drawManager.addSingleTapEvent(event);
                }
                return true;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (mCapturingLines.get()) {
                    return drawManager.handleDrawingTouch(event); //for drawing
                }
                return true;
            }
        }));

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
    }

    public void drawPoints(boolean draw) {

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
    }
}
