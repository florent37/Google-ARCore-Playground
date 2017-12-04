package com.google.ar.core.examples.java.helloar.arcoremanager.drawer;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.core.ARCanvas;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneRenderer;

import java.io.IOException;

// Visualize planes.
public class PlaneDrawer implements Drawer {
    private final static String TAG = "PlaneDrawer";

    //will distplay triangles on the plane
    private final PlaneRenderer plane = new PlaneRenderer();

    private final Session mArCoreSession;

    public PlaneDrawer(Session arcoreSession) {
        this.mArCoreSession = arcoreSession;
    }

    @Override
    public void prepare(Context context) {
        try {
            plane.createOnGlThread(/*context=*/context, "trigrid.png");
        } catch (IOException e) {
            Log.e(TAG, "Failed to read plane texture");
        }
    }

    @Override
    public void onDraw(ARCanvas arCanvas) {
        plane.drawPlanes(mArCoreSession.getAllPlanes(), arCanvas.getArcoreFrame().getPose(), arCanvas.getProjMatrix());
    }
}
