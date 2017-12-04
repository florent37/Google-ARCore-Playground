package com.google.ar.core.examples.java.helloar.arcoremanager.drawer;

import android.content.Context;

import com.google.ar.core.Frame;
import com.google.ar.core.examples.java.helloar.core.ARCanvas;
import com.google.ar.core.examples.java.helloar.core.rendering.PointCloudRenderer;

// Visualize tracked points.
public class PointCloudDrawer implements Drawer {
    private final PointCloudRenderer pointCloud = new PointCloudRenderer();

    @Override
    public void prepare(Context context) {
        pointCloud.createOnGlThread(/*context=*/context);
    }

    @Override
    public void onDraw(ARCanvas arCanvas) {
        // Visualize tracked points.
        pointCloud.update(arCanvas.getArcoreFrame().getPointCloud());
        pointCloud.draw(arCanvas.getArcoreFrame().getPointCloudPose(), arCanvas.getCameraMatrix(), arCanvas.getProjMatrix());
    }
}
