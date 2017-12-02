package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PlaneHitResult;
import com.google.ar.core.PointCloudHitResult;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.core.AbstractDrawManager;
import com.google.ar.core.examples.java.helloar.core.rendering.CloudAttachment;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneAttachment;
import com.google.ar.core.exceptions.NotTrackingException;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DrawManager extends AbstractDrawManager implements GLSurfaceView.Renderer {
    static final int MAX_OBJECTS_ON_SCREEN = 16;

    //the objects opengl will draw
    private final BackgroundDrawer mBackgroundDrawer;
    private final ObjectsToDraw mObjectsToDraw;
    private final LineDrawer mlineDrawer;

    private final SizeManager sizeManager = new SizeManager();

    public DrawManager(Context context, Session arCoreSession) {
        super(context, arCoreSession);
        mObjectsToDraw = new ObjectsToDraw(arCoreSession);
        mlineDrawer = new LineDrawer(context, arCoreSession, sizeManager);
        mBackgroundDrawer = new BackgroundDrawer(arCoreSession);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Create the texture and pass it to ARCore session to be filled during update().
        mBackgroundDrawer.prepare(mContext);
        mObjectsToDraw.prepare(mContext);
        mlineDrawer.prepare(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        mArcoreSession.setDisplayGeometry(width, height);

        sizeManager.width.set(width);
        sizeManager.height.set(height);
    }


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

            // Draw background.
            mBackgroundDrawer.onDraw(arcoreFrame, null, null, 0);

            // If not tracking, don't draw 3d objects.
            if (arcoreFrame.getTrackingState() == Frame.TrackingState.NOT_TRACKING) {
                return;
            }

            // Get projection matrix.
            float[] projMatrix = new float[16];
            mArcoreSession.getProjectionMatrix(projMatrix, 0, AppSettings.getNearClip(), AppSettings.getFarClip());

            // Get camera matrix and draw.
            float[] cameraMatrix = new float[16];
            arcoreFrame.getViewMatrix(cameraMatrix, 0);

            // Compute lighting from average intensity of the image.
            final float lightIntensity = arcoreFrame.getLightEstimate().getPixelIntensity();

            mObjectsToDraw.onDraw(arcoreFrame, cameraMatrix, projMatrix, lightIntensity);

            mlineDrawer.onDraw(arcoreFrame, cameraMatrix, projMatrix, lightIntensity);

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
                    // Cap the number of objects created. This avoids overloading both the
                    // rendering system and ARCore.
                    if (mObjectsToDraw.mClickedPlanePositions.size() >= MAX_OBJECTS_ON_SCREEN) {
                        mArcoreSession.removeAnchors(Arrays.asList(mObjectsToDraw.mClickedPlanePositions.get(0).getAnchor()));
                        mObjectsToDraw.mClickedPlanePositions.remove(0);
                    }
                    // Adding an Anchor tells ARCore that it should track this position in
                    // space. This anchor will be used in PlaneAttachment to place the 3d model
                    // in the correct position relative both to the world and to the plane.
                    mObjectsToDraw.mClickedPlanePositions.add(new PlaneAttachment(
                            ((PlaneHitResult) hit).getPlane(),
                            mArcoreSession.addAnchor(hit.getHitPose())));

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
}
