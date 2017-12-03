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

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.ar.core.examples.java.helloar.arcoremanager.ArCoreManager;
import com.google.ar.core.examples.java.helloar.bugdroid.BugDroidArCoreObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using
 * the ARCore API. The application will display any detected planes and will allow the user to
 * tap on a plane to place a 3d model of the Android robot.
 */
public class HelloArActivity extends AppCompatActivity {

    protected Snackbar mLoadingMessageSnackbar = null;
    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    @BindView(R.id.surfaceview)
    GLSurfaceView mSurfaceView;

    private ArCoreManager arCoreManager;

    private BottomBarHolder bottomBarHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        arCoreManager = new ArCoreManager(this, new ArCoreManager.Listener() {
            @Override
            public void onArCoreUnsuported() {
                Toast.makeText(HelloArActivity.this, "This device does not support AR", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onPermissionNotAllowed() {
                //on permission not allowed
                Toast.makeText(HelloArActivity.this,
                        "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void showLoadingMessage() {
                runOnUiThread(() -> {
                    mLoadingMessageSnackbar = Snackbar.make(
                            findViewById(android.R.id.content),
                            "Searching for surfaces...", Snackbar.LENGTH_INDEFINITE);
                    mLoadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
                    mLoadingMessageSnackbar.show();
                });
            }

            @Override
            public void hideLoadingMessage() {
                runOnUiThread(() -> {
                    if (mLoadingMessageSnackbar != null) {
                        mLoadingMessageSnackbar.dismiss();
                        mLoadingMessageSnackbar = null;
                    }
                });
            }
        });

        arCoreManager.setup(mSurfaceView);
        arCoreManager.addObjectToDraw(new BugDroidArCoreObject());

        bottomBarHolder = new BottomBarHolder(findViewById(android.R.id.content), arCoreManager.getSettings());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
