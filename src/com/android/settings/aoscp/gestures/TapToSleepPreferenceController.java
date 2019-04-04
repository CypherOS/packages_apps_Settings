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

import static android.provider.Settings.System.DOUBLE_TAP_SLEEP_GESTURE;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.aoscp.widget.IllustrationPreference;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;

public class TapToSleepPreferenceController extends BasePreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String PREF_KEY_ILLUSTRATION = "tap_to_sleep_video";
    private static final String KEY_TAP_TO_SLEEP = "tap_to_sleep_summary";

    private IllustrationPreference mIllustrationPreference;

    public TapToSleepPreferenceController(Context context) {
        super(context, KEY_TAP_TO_SLEEP);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mIllustrationPreference = (IllustrationPreference) screen.findPreference(PREF_KEY_ILLUSTRATION);
        }
    }

    @Override
    public void updateState(Preference preference) {
        if (preference != null) {
            if (preference instanceof SwitchPreference) {
                int setting = Settings.System.getInt(mContext.getContentResolver(),
                        DOUBLE_TAP_SLEEP_GESTURE, 0);
                ((SwitchPreference) preference).setChecked(setting != 0);
            }
        }
    }

    @Override
    public CharSequence getSummary() {
        CharSequence summary;
        boolean enabled = Settings.System.getInt(mContext.getContentResolver(), 
                DOUBLE_TAP_SLEEP_GESTURE, 0) != 0;
        if (enabled) {
            summary = mContext.getText(R.string.gesture_setting_on);
        } else {
            summary = mContext.getText(R.string.gesture_setting_off);
        }
        return summary;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean allowTapToSleep = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(), DOUBLE_TAP_SLEEP_GESTURE,
                allowTapToSleep ? 1 : 0);
        return true;
    }
}
