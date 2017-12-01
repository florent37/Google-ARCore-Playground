package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.examples.java.helloar.core.rendering.BackgroundRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.CloudAttachment;
import com.google.ar.core.examples.java.helloar.core.rendering.LineRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.ObjectRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneAttachment;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.PointCloudRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ObjectsToDraw {

    private final static String TAG = "ObjectsToDraw";

    //the background / camera display
    public BackgroundRenderer background = new BackgroundRenderer();

    //the droid
    public ObjectRenderer androidObject = new ObjectRenderer();
    public ObjectRenderer androidObjectShadow = new ObjectRenderer();

    //will distplay triangles on the plane
    public PlaneRenderer plane = new PlaneRenderer();
    public PointCloudRenderer pointCloud = new PointCloudRenderer();

    private final Context mContext;

    public ObjectsToDraw(Context mContext) {
        this.mContext = mContext;
    }

    public void preparePlane() {
        try {
            plane.createOnGlThread(/*context=*/mContext, "trigrid.png");
        } catch (IOException e) {
            Log.e(TAG, "Failed to read plane texture");
        }
    }

    public void prepareAndroidObject() {
        // Prepare the other rendering objects.
        try {
            androidObject.createOnGlThread(/*context=*/mContext, "andy.obj", "andy.png");
            androidObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);

            androidObjectShadow.createOnGlThread(/*context=*/mContext,
                    "andy_shadow.obj", "andy_shadow.png");
            androidObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
            androidObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }
    }

    public void preparePoints() {
        pointCloud.createOnGlThread(/*context=*/mContext);
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

    public void prepareBackground() {
        background.createOnGlThread(/*context=*/mContext);
    }
}
