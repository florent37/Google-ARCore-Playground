package com.google.ar.core.examples.java.helloar.drawer;

import android.content.Context;

import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.core.rendering.BackgroundRenderer;

public class BackgroundDrawer implements Drawer {
    //the background / camera display
    public BackgroundRenderer background = new BackgroundRenderer();

    private final Session mArCoreSession;

    public BackgroundDrawer(Session mArCoreSession) {
        this.mArCoreSession = mArCoreSession;
    }


    @Override
    public void prepare(Context context) {
        background.createOnGlThread(/*context=*/context);

        mArCoreSession.setCameraTextureName(background.getTextureId());
    }

    @Override
    public void onDraw(Frame arcoreFrame, float[] cameraMatrix, float[] projMatrix, float lightIntensity) {
        background.draw(arcoreFrame);
    }
}
