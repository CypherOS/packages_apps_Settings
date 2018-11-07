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
package com.android.settings.aoscp.ambient.play;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.AMBIENT_RECOGNITION;
import static android.provider.Settings.System.AMBIENT_RECOGNITION_KEYGUARD;

public class AmbientPlayKeyguardPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private String mKey;

    public AmbientPlayKeyguardPreferenceController(Context context, String key) {
        super(context);
        mKey = key;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mKey;
    }

    @Override
    public void updateState(Preference preference) {
		final boolean ambientPlay = Settings.System.getInt(mContext.getContentResolver(),
                AMBIENT_RECOGNITION, 0) != 0;
        final boolean setting = Settings.System.getInt(mContext.getContentResolver(),
                AMBIENT_RECOGNITION_KEYGUARD, 1) != 0;
        ((SwitchPreference) preference).setChecked(ambientPlay && setting);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(), AMBIENT_RECOGNITION_KEYGUARD,
                enabled ? 1 : 0);
        return true;
    }
}
