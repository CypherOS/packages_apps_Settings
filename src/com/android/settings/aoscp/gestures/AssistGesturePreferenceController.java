/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.net.Uri;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.core.PreferenceController;

import java.util.Arrays;
import java.util.List;

import static android.provider.Settings.Secure.ASSIST_GESTURE_ENABLED;

public class AssistGesturePreferenceController extends PreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_GESTURE_ASSIST = "gesture_assist";

    private PreferenceScreen mScreen;
    private Preference mPreference;

    public AssistGesturePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        mScreen = screen;
        mPreference = screen.findPreference(getPreferenceKey());
        // Call super last or AbstractPreferenceController might remove the preference from the
        // screen (if !isAvailable()) before we can save a reference to it.
        super.displayPreference(screen);
    }

    private void updatePreference() {
        if (mPreference == null) {
            return;
        }

        if (isAvailable()) {
            if (mScreen.findPreference(getPreferenceKey()) == null) {
                mScreen.addPreference(mPreference);
            }
        } else {
            mScreen.removePreference(mPreference);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (boolean) newValue;
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.ASSIST_GESTURE_ENABLED, enabled ? 1 : 0);
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_GESTURE_ASSIST;
    }

    @Override
    public void updateState(Preference preference) {
        int value = Settings.Secure.getInt(mContext.getContentResolver(), ASSIST_GESTURE_ENABLED, 1);
        ((SwitchPreference) preference).setChecked(value != 0);
    }
}
