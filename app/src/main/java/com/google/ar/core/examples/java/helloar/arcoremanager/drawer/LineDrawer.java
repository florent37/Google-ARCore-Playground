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

package com.google.ar.core.examples.java.helloar.arcoremanager.drawer;

import android.content.Context;
import android.graphics.Color;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.core.AppSettings;
import com.google.ar.core.examples.java.helloar.arcoremanager.SizeManager;
import com.google.ar.core.examples.java.helloar.core.rendering.BiquadFilter;
import com.google.ar.core.examples.java.helloar.core.rendering.LineRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.LineUtils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;


/**
 * This is a complex example that shows how to create an augmented reality (AR) application using
 * the ARCore API.
 */

public class LineDrawer implements Drawer {
    private static final String TAG = "LineDrawer";

    private final Session mArCoreSession;
    private final SizeManager mScreenSizeManager;

    private final ArrayList<ArrayList<Vector3f>> mStrokes = new ArrayList<>();
    private final Context mContext;

    private LineRenderer mLineShaderRenderer = new LineRenderer();

    private float[] mZeroMatrix = new float[16];

    private BiquadFilter biquadFilter;
    private Vector3f mLastPoint = new Vector3f(0, 0, 0);
    private Vector2f lastTouch = new Vector2f();

    private float mLineWidthMax = 0.33f;
    private float mDistanceScale = 0.0f;
    private float mLineSmoothing = 0.1f;

    private float[] mLastFramePosition;

    public AtomicBoolean bIsTracking = new AtomicBoolean(true);
    public AtomicBoolean bReCenterView = new AtomicBoolean(false);
    public AtomicBoolean bTouchDown = new AtomicBoolean(false);
    public AtomicBoolean bClearDrawing = new AtomicBoolean(false);
    public AtomicBoolean bLineParameters = new AtomicBoolean(false);
    public AtomicBoolean bUndo = new AtomicBoolean(false);
    public AtomicBoolean bNewStroke = new AtomicBoolean(false);

    public AtomicInteger color = new AtomicInteger(Color.WHITE);
    public AtomicReference<Float> distance = new AtomicReference<Float>(0.125f);

    /**
     * Setup the app when main activity is created
     */
    public LineDrawer(final Context context, final Session arCoreSession, final SizeManager screenSizeManager) {
        this.mScreenSizeManager = screenSizeManager;
        this.mArCoreSession = arCoreSession;
        this.mContext = context;

        // Reset the zero matrix
        Matrix.setIdentityM(mZeroMatrix, 0);
    }

    @Override
    public void prepare(Context context) {
        mLineShaderRenderer.createOnGlThread(mContext);
    }

    /**
     * addStroke adds a new stroke to the scene
     *
     * @param touchPoint a 2D point in screen space and is projected into 3D world space
     */
    private void addStroke(Vector2f touchPoint, final float[] cameramtx, final float[] projmtx) {
        Vector3f newPoint = LineUtils.GetWorldCoords(touchPoint, mScreenSizeManager.getWidth(), mScreenSizeManager.getHeight(), projmtx, cameramtx, distance.get());
        addStroke(newPoint);
    }


    /**
     * addPoint adds a point to the current stroke
     *
     * @param touchPoint a 2D point in screen space and is projected into 3D world space
     */
    private void addPoint(Vector2f touchPoint, final float[] cameramtx, final float[] projmtx) {
        Vector3f newPoint = LineUtils.GetWorldCoords(touchPoint, mScreenSizeManager.getWidth(), mScreenSizeManager.getHeight(), projmtx, cameramtx, distance.get());
        addPoint(newPoint);
    }


    /**
     * addStroke creates a new stroke
     *
     * @param newPoint a 3D point in world space
     */
    private void addStroke(Vector3f newPoint) {
        biquadFilter = new BiquadFilter(mLineSmoothing);
        for (int i = 0; i < 1500; i++) {
            biquadFilter.update(newPoint);
        }
        Vector3f p = biquadFilter.update(newPoint);
        mLastPoint = new Vector3f(p);
        mStrokes.add(new ArrayList<Vector3f>());
        mStrokes.get(mStrokes.size() - 1).add(mLastPoint);
    }

    /**
     * addPoint adds a point to the current stroke
     *
     * @param newPoint a 3D point in world space
     */
    private void addPoint(Vector3f newPoint) {
        if (LineUtils.distanceCheck(newPoint, mLastPoint)) {
            Vector3f p = biquadFilter.update(newPoint);
            mLastPoint = new Vector3f(p);
            mStrokes.get(mStrokes.size() - 1).add(mLastPoint);
        }
    }

    public void update(int color, float distance){
        if(color != this.color.get()) {
            this.color.set(color);
            mLineShaderRenderer.bNeedsUpdate.set(true);
        }

        if(distance != this.distance.get()) {
            this.distance.set(distance);
            mLineShaderRenderer.bNeedsUpdate.set(true);
        }
    }


    /**
     * onDraw() is executed on the GL Thread.
     * The method handles all operations that need to take place before drawing to the screen.
     * The method :
     * extracts the current projection matrix and view matrix from the AR Pose
     * handles adding stroke and points to the data collections
     * updates the ZeroMatrix and performs the matrix multiplication needed to re-center the drawing
     * updates the Line Renderer with the current strokes, color, distance scale, line width etc
     */
    public void update(final Frame arCoreFrame, final float[] cameramtx, final float[] projmtx) {
        try {

            // Update tracking states
            if (arCoreFrame.getTrackingState() == Frame.TrackingState.TRACKING && !bIsTracking.get()) {
                bIsTracking.set(true);
            } else if (arCoreFrame.getTrackingState() == Frame.TrackingState.NOT_TRACKING && bIsTracking.get()) {
                bIsTracking.set(false);
                bTouchDown.set(false);
            }

            float[] position = new float[3];
            arCoreFrame.getPose().getTranslation(position, 0);

            // Check if camera has moved much, if thats the case, stop touchDown events
            // (stop drawing lines abruptly through the air)
            if (mLastFramePosition != null) {
                Vector3f distance = new Vector3f(position[0], position[1], position[2]);
                distance.sub(new Vector3f(mLastFramePosition[0], mLastFramePosition[1], mLastFramePosition[2]));

                if (distance.length() > 0.15) {
                    bTouchDown.set(false);
                }
            }
            mLastFramePosition = position;

            // Multiply the zero matrix
            Matrix.multiplyMM(cameramtx, 0, cameramtx, 0, mZeroMatrix, 0);

            if (bNewStroke.get()) {
                bNewStroke.set(false);
                addStroke(lastTouch,  cameramtx, projmtx);
                mLineShaderRenderer.bNeedsUpdate.set(true);
            } else if (bTouchDown.get()) {
                addPoint(lastTouch, cameramtx, projmtx);
                mLineShaderRenderer.bNeedsUpdate.set(true);
            }

            if (bReCenterView.get()) {
                bReCenterView.set(false);
                mZeroMatrix = getCalibrationMatrix(arCoreFrame);
            }

            if (bClearDrawing.get()) {
                bClearDrawing.set(false);
                clearDrawing();
                mLineShaderRenderer.bNeedsUpdate.set(true);
            }

            if (bUndo.get()) {
                bUndo.set(false);
                if (mStrokes.size() > 0) {
                    mStrokes.remove(mStrokes.size() - 1);
                    mLineShaderRenderer.bNeedsUpdate.set(true);
                }
            }
            mLineShaderRenderer.setDrawDebug(bLineParameters.get());
            if (mLineShaderRenderer.bNeedsUpdate.get()) {
                mLineShaderRenderer.setColor(colorToVector(color.get()));
                mLineShaderRenderer.mDrawDistance = distance.get();
                mLineShaderRenderer.setDistanceScale(mDistanceScale);
                mLineShaderRenderer.setLineWidth(mLineWidthMax);
                mLineShaderRenderer.clear();
                mLineShaderRenderer.updateStrokes(mStrokes);
                mLineShaderRenderer.upload();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Vector3f colorToVector(int color){
        return new Vector3f(
                Color.red(color) / 255f,
                Color.green(color) / 255f,
                Color.blue(color) / 255f
        );
    }

    public void onDraw(final Frame arCoreFrame, final float[] cameramtx, final float[] projmtx, float lightIntensity){
        update(arCoreFrame, cameramtx, projmtx);
        if (arCoreFrame.getTrackingState() == Frame.TrackingState.TRACKING) {
            mLineShaderRenderer.draw(cameramtx, projmtx, mScreenSizeManager.getWidth(), mScreenSizeManager.getHeight(), AppSettings.getNearClip(), AppSettings.getFarClip());
        }
    }

    /**
     * Get a matrix usable for zero calibration (only position and compass direction)
     */
    public float[] getCalibrationMatrix(Frame arCoreFrame) {
        float[] t = new float[3];
        float[] m = new float[16];

        arCoreFrame.getPose().getTranslation(t, 0);
        float[] z = arCoreFrame.getPose().getZAxis();
        Vector3f zAxis = new Vector3f(z[0], z[1], z[2]);
        zAxis.y = 0;
        zAxis.normalize();

        double rotate = Math.atan2(zAxis.x, zAxis.z);

        Matrix.setIdentityM(m, 0);
        Matrix.translateM(m, 0, t[0], t[1], t[2]);
        Matrix.rotateM(m, 0, (float) Math.toDegrees(rotate), 0, 1, 0);
        return m;
    }

    /**
     * Clears the Datacollection of Strokes and sets the Line Renderer to clear and update itself
     * Designed to be executed on the GL Thread
     */
    public void clearDrawing() {
        mStrokes.clear();
        mLineShaderRenderer.clear();
    }

    /**
     * onClickRecenter handles the touch input on the GUI and sets the AtomicBoolean bReCEnterView to be true
     * the actual recenter functionality is executed on the GL Thread
     */
    public void onClickRecenter(View button) {
        bReCenterView.set(true);
    }

    public boolean handleDrawingTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouch.set(event.getX(), event.getY());
            bTouchDown.set(true);
            bNewStroke.set(true);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            lastTouch.set(event.getX(), event.getY());
            bTouchDown.set(true);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            bTouchDown.set(false);
            lastTouch.set(event.getX(), event.getY());
            return true;
        }
        return false;
    }
}
