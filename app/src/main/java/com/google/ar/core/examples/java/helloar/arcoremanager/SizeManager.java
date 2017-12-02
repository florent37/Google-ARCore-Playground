package com.google.ar.core.examples.java.helloar.arcoremanager;

import java.util.concurrent.atomic.AtomicInteger;

public class SizeManager {
    public final AtomicInteger width = new AtomicInteger(0);
    public final AtomicInteger height = new AtomicInteger(0);

    public int getWidth() {
        return width.get();
    }

    public int getHeight() {
        return height.get();
    }
}
