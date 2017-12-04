package com.google.ar.core.examples.java.helloar.core;

import com.google.ar.core.Frame;

//not a canvas, but just for troll :)
public class ARCanvas {
    private Frame arcoreFrame;
    private float[] cameraMatrix;
    private float[] projMatrix;
    private float lightIntensity;
    private float width;
    private float height;

    public ARCanvas() {

    }

    public void setArcoreFrame(Frame arcoreFrame) {
        this.arcoreFrame = arcoreFrame;
    }

    public void setCameraMatrix(float[] cameraMatrix) {
        this.cameraMatrix = cameraMatrix;
    }

    public void setProjMatrix(float[] projMatrix) {
        this.projMatrix = projMatrix;
    }

    public void setLightIntensity(float lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public Frame getArcoreFrame() {
        return arcoreFrame;
    }

    public float[] getCameraMatrix() {
        return cameraMatrix;
    }

    public float[] getProjMatrix() {
        return projMatrix;
    }

    public float getLightIntensity() {
        return lightIntensity;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
