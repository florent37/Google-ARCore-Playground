package com.google.ar.core.examples.java.helloar.bugdroid;

import android.content.Context;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.PlaneHitResult;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.arcoremanager.ARCoreObject;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneAttachment;
import com.google.ar.core.exceptions.NotTrackingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BugDroidArCoreObject implements ARCoreObject {

    private final int MAX_OBJECTS_ON_SCREEN = 10;

    private final BugDroidDrawer bugDroidDrawer;
    public final List<PlaneAttachment> positions = new ArrayList<>();

    public BugDroidArCoreObject() {
        this.bugDroidDrawer = new BugDroidDrawer();
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

        bugDroidDrawer.update(positions);
    }

    @Override
    public void onDraw(Frame arcoreFrame, float[] cameraMatrix, float[] projMatrix, float lightIntensity) {
        bugDroidDrawer.onDraw(arcoreFrame, cameraMatrix, projMatrix, lightIntensity);
    }

    @Override
    public void prepare(Context context) {
        bugDroidDrawer.prepare(context);
    }
}
