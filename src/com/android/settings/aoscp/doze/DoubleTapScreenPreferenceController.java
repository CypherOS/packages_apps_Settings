/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.aoscp.doze;

import android.annotation.UserIdInt;
import android.content.Context;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;

import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.Secure.DOZE_PULSE_ON_DOUBLE_TAP;

public class DoubleTapScreenPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_DOUBLE_TAP_SCREEN = "ambient_display_double_tap";

    private final AmbientDisplayConfiguration mAmbientConfig;
    @UserIdInt
    private final int mUserId;

    public DoubleTapScreenPreferenceController(Context context, AmbientDisplayConfiguration config, 
        @UserIdInt int userId) {
        super(context);
        mAmbientConfig = config;
        mUserId = userId;
    }

    @Override
    public boolean isAvailable() {
        return mAmbientConfig.pulseOnDoubleTapAvailable();
    }

    @Override
    public String getPreferenceKey() {
        return KEY_DOUBLE_TAP_SCREEN;
    }

    @Override
    public void updateState(Preference preference) {
        int value = Settings.Secure.getInt(mContext.getContentResolver(), DOZE_PULSE_ON_DOUBLE_TAP, 1);
        ((SwitchPreference) preference).setChecked(value != 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (boolean) newValue;
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.DOZE_PULSE_ON_DOUBLE_TAP, enabled ? 1 : 0);
        return true;
    }
}