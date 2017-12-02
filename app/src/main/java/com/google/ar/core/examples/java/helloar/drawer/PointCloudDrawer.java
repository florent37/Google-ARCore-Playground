package com.google.ar.core.examples.java.helloar.drawer;

import android.content.Context;

import com.google.ar.core.Frame;
import com.google.ar.core.examples.java.helloar.core.rendering.PointCloudRenderer;

// Visualize tracked points.
public class PointCloudDrawer implements Drawer {
    private final PointCloudRenderer pointCloud = new PointCloudRenderer();

    @Override
    public void prepare(Context context) {
        pointCloud.createOnGlThread(/*context=*/context);
    }

    @Override
    public void onDraw(Frame arcoreFrame, float[] cameraMatrix, float[] projMatrix, float lightIntensity) {
        // Visualize tracked points.
        pointCloud.update(arcoreFrame.getPointCloud());
        pointCloud.draw(arcoreFrame.getPointCloudPose(), cameraMatrix, projMatrix);
    }
}
