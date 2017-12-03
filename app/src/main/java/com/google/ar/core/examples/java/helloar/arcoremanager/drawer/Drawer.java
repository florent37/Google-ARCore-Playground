package com.google.ar.core.examples.java.helloar.arcoremanager.drawer;

import android.content.Context;

import com.google.ar.core.Frame;

public interface Drawer {

    void prepare(Context context);

    void onDraw(Frame arcoreFrame, float[] cameraMatrix, float[] projMatrix, float lightIntensity);
}
