package com.google.ar.core.examples.java.helloar.arcoremanager.drawer;

import android.content.Context;

import com.google.ar.core.Frame;
import com.google.ar.core.examples.java.helloar.core.ARCanvas;

public interface Drawer {

    void prepare(Context context);

    void onDraw(ARCanvas arCanvas);
}
