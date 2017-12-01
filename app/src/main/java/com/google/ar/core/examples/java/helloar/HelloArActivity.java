/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.core.examples.java.helloar;

import android.Manifest;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.examples.java.helloar.core.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import florent37.github.com.rxlifecycle.RxLifecycle;
import io.reactivex.Observable;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using
 * the ARCore API. The application will display any detected planes and will allow the user to
 * tap on a plane to place a 3d model of the Android robot.
 */
public class HelloArActivity extends BaseActivity {

    private DrawManager drawManager;

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView mSurfaceView;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceview);

        drawManager = new DrawManager(this, mArcoreSession);

        // Set up tap listener.
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                drawManager.addSingleTapEvent(e);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        mSurfaceView.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));

        // Set up renderer.
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        mSurfaceView.setRenderer(drawManager);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        RxLifecycle.with(this)
                .onResume()
                .flatMap($ -> new RxPermissions(this).request(Manifest.permission.CAMERA))
                .flatMap(success -> {
                    if (!success) return Observable.error(new Throwable());
                    else return Observable.just(success);
                })
                .subscribe(event -> {
                    showLoadingMessage();
                    // Note that order matters - see the note in onPause(), the reverse applies here.
                    mArcoreSession.resume(mDefaultConfig);

                    mSurfaceView.onResume();
                }, throwable -> {
                    //on permission not allowed
                    Toast.makeText(this,
                            "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
                    finish();
                });

        RxLifecycle.with(this)
                .onPause()
                .subscribe(event -> {
                    // Note that the order matters - GLSurfaceView is paused first so that it does not try
                    // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
                    // still call mSession.update() and get a SessionPausedException.
                    mSurfaceView.onPause();
                    mArcoreSession.pause();
                });

        drawManager.setListener(() -> {
            HelloArActivity.super.hideLoadingMessage();
        });
    }
}
