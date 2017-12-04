package com.google.ar.core.examples.java.helloar.arcoremanager;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;

import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PlaneHitResult;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.arcoremanager.drawer.BackgroundDrawer;
import com.google.ar.core.examples.java.helloar.arcoremanager.drawer.LineDrawer;
import com.google.ar.core.examples.java.helloar.arcoremanager.drawer.PlaneDrawer;
import com.google.ar.core.examples.java.helloar.arcoremanager.drawer.PointCloudDrawer;
import com.google.ar.core.examples.java.helloar.arcoremanager.object.ARCoreObjectDrawer;
import com.google.ar.core.examples.java.helloar.core.ARCanvas;
import com.google.ar.core.examples.java.helloar.core.AppSettings;
import com.google.ar.core.exceptions.NotTrackingException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ARCoreRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "ARCoreRenderer";

    // Tap handling and UI.
    private final ArrayBlockingQueue<MotionEvent> mQueuedSingleTaps = new ArrayBlockingQueue<>(16);

    private final Context mContext;
    private final Session mArcoreSession;

    //the objects opengl will draw
    private final BackgroundDrawer mBackgroundDrawer;
    private final PointCloudDrawer mPointCloudDrawer;
    private final PlaneDrawer mPlaneDrawer;
    private final LineDrawer mlineDrawer;
    private final ArCoreManager.Settings mSettings;

    private final List<ARCoreObjectDrawer> arCoreObjectDrawerList = new ArrayList<>();
    @Nullable
    private ARCoreObjectDrawer currentARCoreObjectDrawer = null;

    @Nullable
    private Listener mListener;


    public ARCoreRenderer(Context context, Session arCoreSession, ArCoreManager.Settings settings) {
        this.mContext = context;
        this.mArcoreSession = arCoreSession;
        mSettings = settings;
        mBackgroundDrawer = new BackgroundDrawer(arCoreSession);
        mPlaneDrawer = new PlaneDrawer(arCoreSession);
        mPointCloudDrawer = new PointCloudDrawer();

        mlineDrawer = new LineDrawer(context, arCoreSession);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        // Create the texture and pass it to ARCore session to be filled during update().

        mBackgroundDrawer.prepare(mContext);
        mPlaneDrawer.prepare(mContext);
        mPointCloudDrawer.prepare(mContext);
        for (ARCoreObjectDrawer arCoreObjectDrawer : arCoreObjectDrawerList) {
            arCoreObjectDrawer.prepare(mContext);
        }
        mlineDrawer.prepare(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        mArcoreSession.setDisplayGeometry(width, height);

        arCanvas.setWidth(width);
        arCanvas.setHeight(height);
    }

    private final ARCanvas arCanvas = new ARCanvas();

    @Override
    public void onDrawFrame(GL10 gl) {
        // Check if we detected at least one plane. If so, hide the loading message.
        if (mListener != null) {
            for (Plane plane : mArcoreSession.getAllPlanes()) {
                if (plane.getType() == com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING &&
                        plane.getTrackingState() == Plane.TrackingState.TRACKING) {
                    mListener.hideLoading();
                    break;
                }
            }
        }

        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        try {
            // Obtain the current arcoreFrame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            final Frame arcoreFrame = mArcoreSession.update();

            // Handle taps. Handling only one tap per arcoreFrame, as taps are usually low frequency
            // compared to arcoreFrame rate.
            handleTaps(arcoreFrame);

            arCanvas.setArcoreFrame(arcoreFrame);

            if (mSettings.drawBackground.get()) {
                // Draw background.
                mBackgroundDrawer.onDraw(arCanvas);
            }

            // If not tracking, don't draw 3d objects.
            if (arcoreFrame.getTrackingState() == Frame.TrackingState.NOT_TRACKING) {
                return;
            }

            //handle color change
            mlineDrawer.update(mSettings.linesColor.get(), mSettings.linesDistance.get());

            // Get projection matrix.
            final float[] projMatrix = new float[16];
            mArcoreSession.getProjectionMatrix(projMatrix, 0, AppSettings.getNearClip(), AppSettings.getFarClip());

            // Get camera matrix and draw.
            final float[] cameraMatrix = new float[16];
            arcoreFrame.getViewMatrix(cameraMatrix, 0);

            // Compute lighting from average intensity of the image.
            final float lightIntensity = arcoreFrame.getLightEstimate().getPixelIntensity();

            arCanvas.setProjMatrix(projMatrix);
            arCanvas.setCameraMatrix(cameraMatrix);
            arCanvas.setLightIntensity(lightIntensity);

            //draw
            if (mSettings.drawPoints.get()) {
                mPointCloudDrawer.onDraw(arCanvas);
            }

            //draw
            if (mSettings.drawPlanes.get()) {
                mPlaneDrawer.onDraw(arCanvas);
            }

            for (ARCoreObjectDrawer arCoreObjectDrawer : arCoreObjectDrawerList) {
                arCoreObjectDrawer.onDraw(arCanvas);
            }
            mlineDrawer.onDraw(arCanvas);
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }

    }

    private void handleTaps(Frame frame) throws NotTrackingException {
        // Handle taps. Handling only one tap per frame, as taps are usually low frequency
        // compared to frame rate.
        MotionEvent tap = mQueuedSingleTaps.poll();
        if (tap != null && frame.getTrackingState() == Frame.TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                // Check if any plane was hit, and if it was hit inside the plane polygon.
                if (hit instanceof PlaneHitResult && ((PlaneHitResult) hit).isHitInPolygon()) {

                    if (currentARCoreObjectDrawer != null) {
                        currentARCoreObjectDrawer.addPlaneAttachment((PlaneHitResult) hit, mArcoreSession);
                    }

                    // Hits are sorted by depth. Consider only closest hit on a plane.
                    break;
                }
                /*else if(hit instanceof PointCloudHitResult){
                    mClickedCloudPositions.add(new CloudAttachment(
                            ((PointCloudHitResult) hit).getPointCloudPose(),
                            ((PointCloudHitResult) hit).getPointCloud(),
                            mArcoreSession.addAnchor(hit.getHitPose())));
                }*/
            }
        }
    }

    /**
     * onTouchEvent handles saving the lastTouch screen position and setting bTouchDown and bNewStroke
     * AtomicBooleans to trigger addPoint and addStroke on the GL Thread to be called
     */
    public boolean handleDrawingTouch(MotionEvent event) {
        return mlineDrawer.handleDrawingTouch(event);
    }

    public void addObjectToDraw(ARCoreObjectDrawer arCoreObjectDrawer) {
        arCoreObjectDrawerList.add(arCoreObjectDrawer);
        if (this.currentARCoreObjectDrawer == null) {
            currentARCoreObjectDrawer = arCoreObjectDrawer;
        }
    }

    public void addSingleTapEvent(MotionEvent e) {
        // Queue tap if there is space. Tap is lost if queue is full.
        mQueuedSingleTaps.offer(e);
    }

    public void setListener(@Nullable Listener listener) {
        this.mListener = listener;
    }

    public void onScale(float scaleFactor) {
        if (currentARCoreObjectDrawer != null) {
            currentARCoreObjectDrawer.setScaleFactor(scaleFactor);
        }
    }

    public void onRotate(float angle) {
        if (currentARCoreObjectDrawer != null) {
            currentARCoreObjectDrawer.rotate(angle);
        }
    }

    public void onTranslate(float distanceX, float distanceY) {
        if (currentARCoreObjectDrawer != null) {
            currentARCoreObjectDrawer.translate(distanceX, distanceY);
        }
    }


    public interface Listener {
        void hideLoading();
    }

}
