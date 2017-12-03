package com.google.ar.core.examples.java.helloar;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.ar.core.examples.java.helloar.arcoremanager.ArCoreManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class BottomBarHolder {

    private final ArCoreManager.Settings settings;

    @BindView(R.id.addLines)
    TextView addLines;

    @BindView(R.id.addObjects)
    TextView addObjects;

    @BindView(R.id.onTouch)
    TextView onTouch;

    @BindView(R.id.config)
    TextView config;

    @BindView(R.id.configLayout)
    ViewGroup configLayout;

    @BindView(R.id.onTouchLayout)
    ViewGroup onTouchLayout;

    public BottomBarHolder(View view, ArCoreManager.Settings settings) {
        this.settings = settings;
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.onTouch)
    public void onTouchButtonClicked() {
        if(onTouchLayout.getVisibility() == View.VISIBLE){
            onTouchLayout.setVisibility(View.GONE);
            config.setVisibility(View.VISIBLE);
        } else {
            onTouchLayout.setVisibility(View.VISIBLE);
            config.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.config)
    public void onCongifButtonClicked() {
        if(configLayout.getVisibility() == View.VISIBLE){
            configLayout.setVisibility(View.GONE);
            onTouch.setVisibility(View.VISIBLE);
        } else {
            configLayout.setVisibility(View.VISIBLE);
            onTouch.setVisibility(View.GONE);
        }
    }

    @OnCheckedChanged(R.id.drawBackground)
    public void onDrawBackgroundClicked(android.widget.CompoundButton button, boolean checked) {
        settings.drawBackground.set(checked);
    }

    @OnCheckedChanged(R.id.drawDots)
    public void onDrawDotsClicked(android.widget.CompoundButton button, boolean checked) {
        settings.drawPoints.set(checked);
    }

    @OnClick(R.id.addLines)
    public void onAddLinesClicked() {
        settings.captureLines.set(true);
        addLines.setAlpha(1f);
        addObjects.setAlpha(0.6f);
    }

    @OnClick(R.id.addObjects)
    public void onAddObjectsClicked() {
        settings.captureLines.set(false);
        addLines.setAlpha(0.6f);
        addObjects.setAlpha(1f);
    }

}
