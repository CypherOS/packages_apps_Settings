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

package com.android.settings.gestures;

import android.annotation.UserIdInt;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;

import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.core.PreferenceController;

import static android.provider.Settings.Secure.DOZE_PULSE_ON_PICK_UP;

public class PickupGesturePreferenceController extends PreferenceController
        implements Preference.OnPreferenceChangeListener {

	private static final String KEY_PICK_UP = "gesture_pick_up";

    private final AmbientDisplayConfiguration mAmbientConfig;
    @UserIdInt
    private final int mUserId;

    public PickupGesturePreferenceController(Context context, AmbientDisplayConfiguration config, 
	    @UserIdInt int userId) {
        super(context);
        mAmbientConfig = config;
        mUserId = userId;
    }

    @Override
    public boolean isAvailable() {
        return mAmbientConfig.pulseOnPickupAvailable();
    }

    @Override
    public String getPreferenceKey() {
        return KEY_PICK_UP;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (boolean) newValue;
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.DOZE_PULSE_ON_PICK_UP, enabled ? 1 : 0);
        return true;
    }
	
	@Override
    public void updateState(Preference preference) {
        int enabled = Settings.Secure.getInt(mContext.getContentResolver(), DOZE_PULSE_ON_PICK_UP, 0);
        ((SwitchPreference) preference).setChecked(enabled != 0);
    }
}
