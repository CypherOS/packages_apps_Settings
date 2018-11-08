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
package com.android.settings.aoscp.gestures;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;

import com.android.internal.hardware.AmbientDisplayConfiguration;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.Secure.DOZE_PULSE_ON_HAND_WAVE;

public class HandWaveGesturePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

	private AmbientDisplayConfiguration mConfig;
    private String mKey;

    public HandWaveGesturePreferenceController(Context context, String key) {
        super(context);
        mKey = key;
    }

    @Override
    public boolean isAvailable() {
		if (mConfig == null) {
            mConfig = new AmbientDisplayConfiguration(mContext);
        }
        return mConfig.pulseOnHandWaveAvailable();
    }

    @Override
    public String getPreferenceKey() {
        return mKey;
    }

    @Override
    public void updateState(Preference preference) {
        int setting = Settings.Secure.getInt(mContext.getContentResolver(),
                DOZE_PULSE_ON_HAND_WAVE, 0);
        ((SwitchPreference) preference).setChecked(setting != 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        Settings.Secure.putInt(mContext.getContentResolver(), DOZE_PULSE_ON_HAND_WAVE,
                enabled ? 1 : 0);
        return true;
    }
}
