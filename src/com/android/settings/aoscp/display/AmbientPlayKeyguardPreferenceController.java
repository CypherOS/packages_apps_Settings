/*
 * Copyright (C) 2019 CypherOS
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
package com.android.settings.aoscp.display;

import static android.provider.Settings.System.AMBIENT_RECOGNITION_KEYGUARD;

import android.content.Context;
import android.provider.Settings;

import com.android.internal.app.ColorDisplayController;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class AmbientPlayKeyguardPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_AMBIENT_PLAY_KEYGUARD = "ambient_play_keyguard";

    public AmbientPlayKeyguardPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_AMBIENT_PLAY_KEYGUARD;
    }

	@Override
    public void updateState(Preference preference) {
        final boolean setting = Settings.System.getInt(mContext.getContentResolver(),
                AMBIENT_RECOGNITION_KEYGUARD, 1) != 0;
        ((SwitchPreference) preference).setChecked(setting);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(), AMBIENT_RECOGNITION_KEYGUARD,
                enabled ? 1 : 0);
        return true;
    }
}