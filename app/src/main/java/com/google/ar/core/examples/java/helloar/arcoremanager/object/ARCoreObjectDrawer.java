package com.google.ar.core.examples.java.helloar.arcoremanager.object;

import com.google.ar.core.PlaneHitResult;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.helloar.arcoremanager.drawer.Drawer;
import com.google.ar.core.exceptions.NotTrackingException;

public interface ARCoreObjectDrawer extends Drawer {

    void addPlaneAttachment(PlaneHitResult planeHitResult, Session arCoreSession) throws NotTrackingException;

    void setScaleFactor(float scaleFactor);
}
