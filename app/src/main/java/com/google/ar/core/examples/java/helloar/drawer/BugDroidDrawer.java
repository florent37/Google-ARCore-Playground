package com.google.ar.core.examples.java.helloar.drawer;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.core.rendering.ObjectRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneAttachment;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.PointCloudRenderer;
import com.google.ar.core.examples.java.helloar.drawer.Drawer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

//We will draw bugdroids on each clicked positions
public class BugDroidDrawer implements Drawer {

    private final static String TAG = "BugDroidDrawer";

    //the droid
    private final ObjectRenderer androidObject = new ObjectRenderer();
    private final ObjectRenderer androidObjectShadow = new ObjectRenderer();

    public final ArrayList<PlaneAttachment> mClickedPlanePositions = new ArrayList<>();

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] mAnchorMatrix = new float[16];

    public BugDroidDrawer() {
    }

    @Override
    public void prepare(Context context) {
        // Prepare the other rendering objects.
        try {
            androidObject.createOnGlThread(/*context=*/context, "andy.obj", "andy.png");
            androidObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);

            androidObjectShadow.createOnGlThread(/*context=*/context,
                    "andy_shadow.obj", "andy_shadow.png");
            androidObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
            androidObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }
    }

    @Override
    public void onDraw(Frame arcoreFrame, float[] cameraMatrix, float[] projMatrix, float lightIntensity) {
        // Visualize anchors created by touch.
        float scaleFactor = 1.0f;
        for (PlaneAttachment planeAttachment : mClickedPlanePositions) {
            if (!planeAttachment.isTracking()) {
                continue;
            }
            // Get the current combined pose of an Anchor and Plane in world space. The Anchor
            // and Plane poses are updated during calls to session.update() as ARCore refines
            // its estimate of the world.
            planeAttachment.getPose().toMatrix(mAnchorMatrix, 0);

            // Update and draw the model and its shadow.
            androidObject.updateModelMatrix(mAnchorMatrix, scaleFactor);
            androidObjectShadow.updateModelMatrix(mAnchorMatrix, scaleFactor);
            androidObject.draw(cameraMatrix, projMatrix, lightIntensity);
            androidObjectShadow.draw(cameraMatrix, projMatrix, lightIntensity);
        }

    }
}
