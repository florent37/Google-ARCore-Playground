package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.core.rendering.CloudAttachment;
import com.google.ar.core.examples.java.helloar.core.rendering.ObjectRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneAttachment;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.PointCloudRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ObjectsToDraw implements Drawer {

    private final static String TAG = "ObjectsToDraw";

    //the droid
    private final ObjectRenderer androidObject = new ObjectRenderer();
    private final ObjectRenderer androidObjectShadow = new ObjectRenderer();

    protected final ArrayList<PlaneAttachment> mClickedPlanePositions = new ArrayList<>();

    //will distplay triangles on the plane
    private final PlaneRenderer plane = new PlaneRenderer();

    private final PointCloudRenderer pointCloud = new PointCloudRenderer();

    private final Session mArCoreSession;

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] mAnchorMatrix = new float[16];

    public ObjectsToDraw(Session mArCoreSession) {

        this.mArCoreSession = mArCoreSession;
    }

    public void preparePlane(Context context) {
        try {
            plane.createOnGlThread(/*context=*/context, "trigrid.png");
        } catch (IOException e) {
            Log.e(TAG, "Failed to read plane texture");
        }
    }

    @Override
    public void prepare(Context context) {
        preparePlane(context);
        prepareAndroidObject(context);
        preparePoints(context);
    }

    @Override
    public void onDraw(Frame arcoreFrame, float[] cameraMatrix, float[] projMatrix, float lightIntensity) {
        // Visualize tracked points.
        drawPoints(arcoreFrame, cameraMatrix, projMatrix);

        // Visualize planes.
        drawPlanes(mArCoreSession.getAllPlanes(), arcoreFrame, projMatrix);

        //We will draw bugdroids on each clicked positions
        drawBugDroids(mClickedPlanePositions, mAnchorMatrix, cameraMatrix, projMatrix, lightIntensity);
    }


    private void prepareAndroidObject(Context contex) {
        // Prepare the other rendering objects.
        try {
            androidObject.createOnGlThread(/*context=*/contex, "andy.obj", "andy.png");
            androidObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);

            androidObjectShadow.createOnGlThread(/*context=*/contex,
                    "andy_shadow.obj", "andy_shadow.png");
            androidObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
            androidObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }
    }

    private void preparePoints(Context contex) {
        pointCloud.createOnGlThread(/*context=*/contex);
    }

    public void drawPoints(Frame frame, float[] cameraMatrix, float[] projMatrix) {
        // Visualize tracked points.
        pointCloud.update(frame.getPointCloud());
        pointCloud.draw(frame.getPointCloudPose(), cameraMatrix, projMatrix);
    }

    public void drawPlanes(Collection<Plane> allPlanes, Frame frame, float[] projMatrix) {
        plane.drawPlanes(allPlanes, frame.getPose(), projMatrix);
    }

    public void drawBugDroids(Collection<PlaneAttachment> androidPositions, float[] anchorMatrix, float[] cameraMatrix, float[] projMatrix, float lightIntensity) {
        // Visualize anchors created by touch.
        float scaleFactor = 1.0f;
        for (PlaneAttachment planeAttachment : androidPositions) {
            if (!planeAttachment.isTracking()) {
                continue;
            }
            // Get the current combined pose of an Anchor and Plane in world space. The Anchor
            // and Plane poses are updated during calls to session.update() as ARCore refines
            // its estimate of the world.
            planeAttachment.getPose().toMatrix(anchorMatrix, 0);

            // Update and draw the model and its shadow.
            androidObject.updateModelMatrix(anchorMatrix, scaleFactor);
            androidObjectShadow.updateModelMatrix(anchorMatrix, scaleFactor);
            androidObject.draw(cameraMatrix, projMatrix, lightIntensity);
            androidObjectShadow.draw(cameraMatrix, projMatrix, lightIntensity);
        }

    }
}
