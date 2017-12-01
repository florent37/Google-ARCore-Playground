package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.examples.java.helloar.rendering.BackgroundRenderer;
import com.google.ar.core.examples.java.helloar.rendering.ObjectRenderer;
import com.google.ar.core.examples.java.helloar.rendering.PlaneRenderer;
import com.google.ar.core.examples.java.helloar.rendering.PointCloudRenderer;

import java.io.IOException;

public class DrawManagerRenderers {

    private final static String TAG = "DrawManagerRenderers";

    //the background / camera display
    public BackgroundRenderer background = new BackgroundRenderer();

    //the droid
    public ObjectRenderer androidObject = new ObjectRenderer();
    public ObjectRenderer androidObjectShadow = new ObjectRenderer();

    //will distplay triangles on the plane
    public PlaneRenderer plane = new PlaneRenderer();
    public PointCloudRenderer pointCloud = new PointCloudRenderer();

    private final Context mContext;

    public DrawManagerRenderers(Context mContext) {
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
}
