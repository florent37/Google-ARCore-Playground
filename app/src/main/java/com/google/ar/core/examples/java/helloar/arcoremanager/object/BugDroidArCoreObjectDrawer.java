package com.google.ar.core.examples.java.helloar.arcoremanager.object;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.examples.java.helloar.core.ARCanvas;
import com.google.ar.core.examples.java.helloar.core.rendering.ObjectRenderer;

import java.io.IOException;

//We will draw bugdroids on each clicked positions
public class BugDroidArCoreObjectDrawer extends SimpleArCoreObjectDrawer {

    private static final String TAG = "BugDroidArCoreObjectDrawer";

    //(on super) the droid
    //his shadow
    private final ObjectRenderer androidObjectShadow = new ObjectRenderer();

    public BugDroidArCoreObjectDrawer() {
        super("andy.obj", "andy.png");
    }


    @Override
    public void onDraw(ARCanvas canvas) {
        // Visualize anchors created by touch.
        for (ArCoreObject bugDroid : positions) {
            if (!bugDroid.getPlaneAttachment().isTracking()) {
                continue;
            }
            // Get the current combined pose of an Anchor and Plane in world space. The Anchor
            // and Plane poses are updated during calls to session.update() as ARCore refines
            // its estimate of the world.
            bugDroid.getPlaneAttachment().getPose().toMatrix(mAnchorMatrix, 0);

            // Update and draw the model and its shadow.
            super.objectRenderer.updateModelMatrix(mAnchorMatrix, bugDroid.getScale());
            androidObjectShadow.updateModelMatrix(mAnchorMatrix, bugDroid.getScale());
            super.objectRenderer.draw(canvas.getCameraMatrix(), canvas.getProjMatrix(), canvas.getLightIntensity());
            androidObjectShadow.draw(canvas.getCameraMatrix(), canvas.getProjMatrix(), canvas.getLightIntensity());
        }
    }

    @Override
    public void prepare(Context context) {
        super.prepare(context); //draw the droid on super
        // Prepare the other rendering objects.
        try {
            androidObjectShadow.createOnGlThread(/*context=*/context,
                    "andy_shadow.obj", "andy_shadow.png");
            androidObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
            androidObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }
    }
}
