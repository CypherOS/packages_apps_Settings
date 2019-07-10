package com.android.settings.aoscp.buttons;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.VOLUME_ROCKER_WAKE;

public class VolumeRockerWakePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private String mKey;
    private int mDeviceHardwareKeys;

    public VolumeRockerWakePreferenceController (Context context, String key) {
        super(context);
        mKey = key;
        mDeviceHardwareKeys = context.getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
    }

    @Override
    public boolean isAvailable() {
        return ;
    }

    @Override
    public String getPreferenceKey() {
        return mKey;
    }

    @Override
    public void updateState(Preference preference) {
        int setting = Settings.System.getInt(mContext.getContentResolver(),
                VOLUME_ROCKER_WAKE, 1);
        ((SwitchPreference) preference).setChecked(setting != 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(), VOLUME_ROCKER_WAKE,
                enabled ? 1 : 0);
        return true;
    }
}
