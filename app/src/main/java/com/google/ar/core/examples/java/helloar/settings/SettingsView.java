package com.google.ar.core.examples.java.helloar.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.ar.core.examples.java.helloar.R;
import com.google.ar.core.examples.java.helloar.arcoremanager.ArCoreManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


public class SettingsView extends FrameLayout {

    public interface Listener {
        void onDrawBackgroundChanged(boolean active);
        void onDrawDotsChanged(boolean active);
        void onDrawPlanesChanged(boolean active);
        void onLinesColorChanged(int color);
    }

    @BindView(R.id.drawPlanes)
    CompoundButton drawPlanes;

    @BindView(R.id.drawBackground)
    CompoundButton drawBackground;

    @BindView(R.id.drawDetectionDots)
    CompoundButton drawDetectionDots;

    @BindView(R.id.linesColor)
    View linesColor;

    @NonNull
    private final ArCoreManager.Settings settings;

    public SettingsView(@NonNull Context context, @NonNull ArCoreManager.Settings settings) {
        super(context);
        inflate(context, R.layout.dialog_settings, this);
        ButterKnife.bind(this);

        this.settings = settings;

        drawPlanes.setChecked(settings.drawPlanes.get());
        drawBackground.setChecked(settings.drawBackground.get());
        drawDetectionDots.setChecked(settings.drawPoints.get());
        drawDetectionDots.setChecked(settings.drawPoints.get());
        linesColor.setBackgroundColor(settings.linesColor.get());
    }

    @OnCheckedChanged(R.id.drawPlanes)
    public void onDrawPlanesChanged(boolean checked){
        settings.drawPlanes.set(checked);
    }

    @OnCheckedChanged(R.id.drawBackground)
    public void onDrawBackgroundChanged(boolean checked){
        settings.drawBackground.set(checked);
    }

    @OnCheckedChanged(R.id.drawDetectionDots)
    public void onDrawDotsChanged(boolean checked){
        settings.drawPoints.set(checked);
    }

    @OnClick(R.id.linesColor)
    public void onLinesColorClicked(){
        ColorPickerDialogBuilder
                .with(getContext())
                .setTitle("Choose color")
                .initialColor(settings.linesColor.get())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        settings.linesColor.set(selectedColor);
                        linesColor.setBackgroundColor(settings.linesColor.get());
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }
}
