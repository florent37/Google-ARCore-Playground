package com.google.ar.core.examples.java.helloar.arcoremanager;

import android.content.Context;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.PlaneHitResult;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.NotTrackingException;

public interface ARCoreObject {

    void addPlaneAttachment(PlaneHitResult planeHitResult, Session arCoreSession) throws NotTrackingException;

    void onDraw(Frame arcoreFrame, float[] cameraMatrix, float[] projMatrix, float lightIntensity);

    void prepare(Context mContext);
}
