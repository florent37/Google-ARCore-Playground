package com.google.ar.core.examples.java.helloar.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RadioButton;

import com.google.ar.core.examples.java.helloar.R;
import com.google.ar.core.examples.java.helloar.arcoremanager.ArCoreManager;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ObjectSettings extends FrameLayout {

    private final Listener listener;
    @BindView(R.id.config_object_scale)
    RadioButton configScale;
    @BindView(R.id.config_object_rotate)
    RadioButton configRotate;
    @BindView(R.id.config_object_translate)
    RadioButton configTranslate;

    public ObjectSettings(@NonNull Context context, @NonNull Listener listener) {
        super(context);
        inflate(context, R.layout.config_objects, this);
        ButterKnife.bind(this);
        this.listener = listener;

        configScale.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked) {
                uncheckedOthers(compoundButton);
                listener.onTouchModeChanged(ArCoreManager.ObjectTouchMode.SCALE);
            }
        });
        configRotate.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked) {
                uncheckedOthers(compoundButton);
                listener.onTouchModeChanged(ArCoreManager.ObjectTouchMode.ROTATE);
            }
        });
        configTranslate.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked) {
                uncheckedOthers(compoundButton);
                listener.onTouchModeChanged(ArCoreManager.ObjectTouchMode.TRANSLATE);
            }
        });

        configScale.setChecked(true);
    }

    private void uncheckedOthers(CompoundButton exception) {
        configScale.setChecked(false);
        configTranslate.setChecked(false);
        configRotate.setChecked(false);
        exception.setChecked(true);
    }

    public interface Listener {
        void onTouchModeChanged(ArCoreManager.ObjectTouchMode objectTouchMode);
    }


}
