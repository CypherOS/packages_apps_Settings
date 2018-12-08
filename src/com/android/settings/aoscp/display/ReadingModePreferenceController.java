/*
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.aoscp.display;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import co.aoscp.internal.app.GrayScaleDisplayController;

import static android.provider.Settings.System.DOUBLE_TAP_SLEEP_GESTURE;

public class ReadingModePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_READING_MODE = "reading_mode";

    private GrayScaleDisplayController mController;

    public ReadingModePreferenceController(Context context) {
        super(context);
        mController = new GrayScaleDisplayController(context);
    }

    @Override
    public boolean isAvailable() {
        return GrayScaleDisplayController.isAvailable(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_READING_MODE;
    }

    @Override
    public void updateState(Preference preference) {
        boolean checked = ((SwitchPreference)preference).isChecked();
        ((SwitchPreference) preference).setChecked(checked);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        mController.setGrayScale(enabled ? true : false);
        return true;
    }
}
