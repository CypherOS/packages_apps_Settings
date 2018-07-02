/*
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.aoscp.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class CameraHal3PreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_CAMERA_HAL3 = "camera_hal3_switch";
    static final String PROPERTY_CAMERA_HAL3 = "persist.camera.HAL3.enabled";

    private SwitchPreference mPreference;

    public CameraHal3PreferenceController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mPreference = (SwitchPreference) screen.findPreference(KEY_CAMERA_HAL3);
            mPreference.setChecked(isCameraHal3Enabled());
        }
    }

    @Override
    public String getPreferenceKey() {
        return KEY_CAMERA_HAL3;
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources().getBoolean(R.bool.config_deviceSupportsHal3Switch);
    }

    @Override
    public void updateState(Preference preference) {
        updatePreference();
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_CAMERA_HAL3.equals(preference.getKey())) {
            final SwitchPreference switchPreference = (SwitchPreference)preference;
            SystemProperties.set(PROPERTY_CAMERA_HAL3,
                    switchPreference.isChecked() ? "1" : "0");
            Toast.makeText(mContext, R.string.camera_hal3_toast,
                    Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    public void enablePreference(boolean enabled) {
        if (isAvailable()) {
            mPreference.setEnabled(enabled);
        }
    }

    public boolean updatePreference() {
        if (!isAvailable()) {
            return false;
        }
        final boolean enabled = isCameraHal3Enabled();
        mPreference.setChecked(enabled);
        return enabled;
    }

    private boolean isCameraHal3Enabled() {
        return SystemProperties.getBoolean(PROPERTY_CAMERA_HAL3, false);
    }
}
