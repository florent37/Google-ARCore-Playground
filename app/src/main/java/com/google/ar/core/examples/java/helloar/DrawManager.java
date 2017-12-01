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
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.rendering.PlaneAttachment;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DrawManager extends AbstractDrawManager implements GLSurfaceView.Renderer {

    // Temporary matrix allocated here to reduce number of allocations for each frame.

    private final float[] mAnchorMatrix = new float[16];

    //the objects opengl can draw
    private final DrawManagerRenderers mRenderers;

    public DrawManager(Context context, Session mArcoreSession) {
        super(context, mArcoreSession);
        mRenderers = new DrawManagerRenderers(context);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        try {
            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = mArcoreSession.update();

            // Handle taps. Handling only one tap per frame, as taps are usually low frequency
            // compared to frame rate.
            MotionEvent tap = mQueuedSingleTaps.poll();
            if (tap != null && frame.getTrackingState() == Frame.TrackingState.TRACKING) {
                for (HitResult hit : frame.hitTest(tap)) {
                    // Check if any plane was hit, and if it was hit inside the plane polygon.
                    if (hit instanceof PlaneHitResult && ((PlaneHitResult) hit).isHitInPolygon()) {
                        // Cap the number of objects created. This avoids overloading both the
                        // rendering system and ARCore.
                        if (mAndroidObjectPositions.size() >= 16) {
                            mArcoreSession.removeAnchors(Arrays.asList(mAndroidObjectPositions.get(0).getAnchor()));
                            mAndroidObjectPositions.remove(0);
                        }
                        // Adding an Anchor tells ARCore that it should track this position in
                        // space. This anchor will be used in PlaneAttachment to place the 3d model
                        // in the correct position relative both to the world and to the plane.
                        mAndroidObjectPositions.add(new PlaneAttachment(
                                ((PlaneHitResult) hit).getPlane(),
                                mArcoreSession.addAnchor(hit.getHitPose())));

                        // Hits are sorted by depth. Consider only closest hit on a plane.
                        break;
                    }
                }
            }

            // Draw background.
            mRenderers.background.draw(frame);

            // If not tracking, don't draw 3d objects.
            if (frame.getTrackingState() == Frame.TrackingState.NOT_TRACKING) {
                return;
            }

            // Get projection matrix.
            float[] projmtx = new float[16];
            mArcoreSession.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            frame.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            final float lightIntensity = frame.getLightEstimate().getPixelIntensity();

            // Visualize tracked points.
            mRenderers.pointCloud.update(frame.getPointCloud());
            mRenderers.pointCloud.draw(frame.getPointCloudPose(), viewmtx, projmtx);

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

            // Visualize planes.
            mRenderers.plane.drawPlanes(mArcoreSession.getAllPlanes(), frame.getPose(), projmtx);

            //We will draw bugdroids on each clicked positions

            // Visualize anchors created by touch.
            float scaleFactor = 1.0f;
            for (PlaneAttachment planeAttachment : mAndroidObjectPositions) {
                if (!planeAttachment.isTracking()) {
                    continue;
                }
                // Get the current combined pose of an Anchor and Plane in world space. The Anchor
                // and Plane poses are updated during calls to session.update() as ARCore refines
                // its estimate of the world.
                planeAttachment.getPose().toMatrix(mAnchorMatrix, 0);

                // Update and draw the model and its shadow.
                mRenderers.androidObject.updateModelMatrix(mAnchorMatrix, scaleFactor);
                mRenderers.androidObjectShadow.updateModelMatrix(mAnchorMatrix, scaleFactor);
                mRenderers.androidObject.draw(viewmtx, projmtx, lightIntensity);
                mRenderers.androidObjectShadow.draw(viewmtx, projmtx, lightIntensity);
            }

        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        mArcoreSession.setDisplayGeometry(width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Create the texture and pass it to ARCore session to be filled during update().
        mRenderers.background.createOnGlThread(/*context=*/mContext);
        mArcoreSession.setCameraTextureName(mRenderers.background.getTextureId());

        mRenderers.prepareAndroidObject();
        mRenderers.preparePlane();
        mRenderers.preparePoints();
    }


}
