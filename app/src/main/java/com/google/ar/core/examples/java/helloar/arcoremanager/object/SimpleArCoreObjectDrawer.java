package com.google.ar.core.examples.java.helloar.arcoremanager.object;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.PlaneHitResult;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.core.ARCanvas;
import com.google.ar.core.examples.java.helloar.core.rendering.ObjectRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneAttachment;
import com.google.ar.core.exceptions.NotTrackingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleArCoreObjectDrawer implements ARCoreObjectDrawer {

    private static final String TAG = "SimpleArCoreObjectDrawer";

    public final List<ArCoreObject> positions = new ArrayList<>();
    private final int MAX_OBJECTS_ON_SCREEN = 10;
    //the 3D object
    protected final ObjectRenderer objectRenderer = new ObjectRenderer();
    // Temporary matrix allocated here to reduce number of allocations for each frame.
    protected final float[] mAnchorMatrix = new float[16];
    private final String objFile;
    private final String objTextureAsset;

    public SimpleArCoreObjectDrawer(String objFile, String objTextureAsset) {
        this.objFile = objFile;
        this.objTextureAsset = objTextureAsset;
    }

    @Override
    public void addPlaneAttachment(PlaneHitResult planeHitResult, Session arCoreSession) throws NotTrackingException {

        // Cap the number of objects created. This avoids overloading both the
        // rendering system and ARCore.
        if (positions.size() >= MAX_OBJECTS_ON_SCREEN) {
            arCoreSession.removeAnchors(Arrays.asList(positions.get(0).getAnchor()));
            positions.remove(0);
        }
        // Adding an Anchor tells ARCore that it should track this position in
        // space. This anchor will be used in PlaneAttachment to place the 3d model
        // in the correct position relative both to the world and to the plane.
        positions.add(new ArCoreObject(new PlaneAttachment(
                planeHitResult.getPlane(),
                arCoreSession.addAnchor(planeHitResult.getHitPose()))));

        // Hits are sorted by depth. Consider only closest hit on a plane.
    }

    @Override
    public void setScaleFactor(float scaleFactor) {
        if (!positions.isEmpty()) {
            final ArCoreObject last = positions.get(positions.size() - 1);
            last.setScale(last.getScale()*scaleFactor);
        }
    }

    @Override
    public void onDraw(ARCanvas arCanvas) {
        // Visualize anchors created by touch.
        for (ArCoreObject arCoreObject : positions) {
            if (!arCoreObject.getPlaneAttachment().isTracking()) {
                continue;
            }

            drawObject(arCanvas, arCoreObject);
        }
    }

    protected void drawObject(ARCanvas arCanvas, ArCoreObject arCoreObject){
        // Get the current combined pose of an Anchor and Plane in world space. The Anchor
        // and Plane poses are updated during calls to session.update() as ARCore refines
        // its estimate of the world.
        arCoreObject.getPlaneAttachment().getPose().toMatrix(mAnchorMatrix, 0);

        // Update and draw the model and its shadow.
        objectRenderer.updateModelMatrix(mAnchorMatrix, arCoreObject.getScale());
        objectRenderer.draw(arCanvas.getCameraMatrix(), arCanvas.getProjMatrix(), arCanvas.getLightIntensity());
    }

    @Override
    public void prepare(Context context) {
        // Prepare the other rendering objects.
        try {
            objectRenderer.createOnGlThread(/*context=*/context, objFile, objTextureAsset);
            objectRenderer.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }
    }
}
