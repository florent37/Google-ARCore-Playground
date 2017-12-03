package com.google.ar.core.examples.java.helloar.core;

import javax.vecmath.Vector3f;

public class AppSettings {
    private static final float strokeDrawDistance = 0.125f;
    private static final float minDistance = 0.000625f;
    private static final float nearClip = 0.001f;
    private static final float farClip = 100.0f;


    public static float getStrokeDrawDistance() {
        return strokeDrawDistance;
    }

    public static float getMinDistance() {
        return minDistance;
    }

    public static float getNearClip(){
        return nearClip;
    }
    public static float getFarClip(){
        return farClip;
    }
}
