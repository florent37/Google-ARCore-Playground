package com.google.ar.core.examples.java.helloar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.google.ar.core.examples.java.helloar.arcoremanager.ArCoreManager;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LinesSettings extends FrameLayout {

    @BindView(R.id.linesDistance)
    SeekBar linesDistance;

    private final ArCoreManager.Settings settings;

    public LinesSettings(@NonNull Context context, ArCoreManager.Settings settings) {
        super(context);
        this.settings = settings;
        inflate(context, R.layout.config_lines, this);
        ButterKnife.bind(this);

        linesDistance.setProgress((int) (settings.linesDistance.get() * 100));
        linesDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                settings.linesDistance.set(value / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
