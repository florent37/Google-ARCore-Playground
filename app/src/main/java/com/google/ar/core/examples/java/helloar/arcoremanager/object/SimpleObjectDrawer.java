package com.google.ar.core.examples.java.helloar.arcoremanager.object;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.ar.core.Frame;
import com.google.ar.core.examples.java.helloar.arcoremanager.drawer.Drawer;
import com.google.ar.core.examples.java.helloar.core.rendering.ObjectRenderer;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneAttachment;

import java.io.IOException;
import java.util.List;

//We will draw an object on each clicked positions
public class SimpleObjectDrawer implements Drawer {

    private final static String TAG = "SimpleObjectDrawer";

    //the droid
    private final ObjectRenderer androidObject = new ObjectRenderer();

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] mAnchorMatrix = new float[16];

    @Nullable
    private List<PlaneAttachment> positions;

    private final String objFile;
    private final String objTextureAsset;

    public SimpleObjectDrawer(String objFile, String objTextureAsset) {
        this.objFile = objFile;
        this.objTextureAsset = objTextureAsset;
    }

    @Override
    public void prepare(Context context) {
        // Prepare the other rendering objects.
        try {
            androidObject.createOnGlThread(/*context=*/context, objFile, objTextureAsset);
            androidObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }
    }

    public void update(List<PlaneAttachment> positions){
        if(this.positions != positions) {
            this.positions = positions;
        }
    }

    @Override
    public void onDraw(Frame arcoreFrame, float[] cameraMatrix, float[] projMatrix, float lightIntensity) {
        // Visualize anchors created by touch.
        float scaleFactor = 1.0f;
        if (positions != null) {

            for (PlaneAttachment planeAttachment : positions) {
                if (!planeAttachment.isTracking()) {
                    continue;
                }
                // Get the current combined pose of an Anchor and Plane in world space. The Anchor
                // and Plane poses are updated during calls to session.update() as ARCore refines
                // its estimate of the world.
                planeAttachment.getPose().toMatrix(mAnchorMatrix, 0);

                // Update and draw the model and its shadow.
                androidObject.updateModelMatrix(mAnchorMatrix, scaleFactor);
                androidObject.draw(cameraMatrix, projMatrix, lightIntensity);
            }
        }

    }
}
