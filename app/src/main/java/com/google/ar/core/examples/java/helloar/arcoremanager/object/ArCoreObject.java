package com.google.ar.core.examples.java.helloar.arcoremanager.object;

import com.google.ar.core.Anchor;
import com.google.ar.core.examples.java.helloar.core.rendering.PlaneAttachment;

public class ArCoreObject {
    private final PlaneAttachment planeAttachment;
    private float scale;
    private float rotation;
    private float translationX;
    private float translationZ;

    public ArCoreObject(PlaneAttachment planeAttachment) {
        this.planeAttachment = planeAttachment;
        this.scale = 1f;
    }

    public Anchor getAnchor() {
        return planeAttachment.getAnchor();
    }

    public PlaneAttachment getPlaneAttachment() {
        return planeAttachment;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setRotation(float angle) {
        this.rotation = angle;
    }

    public float getRotation() {
        return rotation;
    }

    public void setTranslation(float distanceX, float distanceZ) {
        translationX = distanceX;
        translationZ = distanceZ;
    }

    public float getTranslationX() {
        return translationX;
    }

    public float getTranslationZ() {
        return translationZ;
    }
}
