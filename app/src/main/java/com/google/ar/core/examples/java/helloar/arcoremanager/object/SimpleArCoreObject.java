package com.google.ar.core.examples.java.helloar.arcoremanager.object;

import android.content.Context;

import com.google.ar.core.Frame;
import com.google.ar.core.PlaneHitResult;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.arcoremanager.ARCoreObject;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneAttachment;
import com.google.ar.core.exceptions.NotTrackingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleArCoreObject implements ARCoreObject {

    private final int MAX_OBJECTS_ON_SCREEN = 10;

    private final SimpleObjectDrawer simpleObjectDrawer;
    public final List<PlaneAttachment> positions = new ArrayList<>();

    public SimpleArCoreObject(String objFile, String objTextureAsset) {
        this.simpleObjectDrawer = new SimpleObjectDrawer(objFile, objTextureAsset);
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
        positions.add(new PlaneAttachment(
                planeHitResult.getPlane(),
                arCoreSession.addAnchor(planeHitResult.getHitPose())));

        // Hits are sorted by depth. Consider only closest hit on a plane.

        simpleObjectDrawer.update(positions);
    }

    @Override
    public void onDraw(Frame arcoreFrame, float[] cameraMatrix, float[] projMatrix, float lightIntensity) {
        simpleObjectDrawer.onDraw(arcoreFrame, cameraMatrix, projMatrix, lightIntensity);
    }

    @Override
    public void prepare(Context context) {
        simpleObjectDrawer.prepare(context);
    }
}
