/*
 * Copyright (C) 2019 CypherOS
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
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.aoscp.widget.IllustrationPreferenceController;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;

public class TapToSleepPreferenceController extends IllustrationPreferenceController {

    private final int ON = 1;
    private final int OFF = 0;

    private static final String PREF_KEY_ILLUSTRATION = "tap_to_sleep_video";
    private final String mTapToSleepPrefKey;

    public TapToSleepPreferenceController(Context context, String key) {
        super(context, key);
        mTapToSleepPrefKey = key;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "tap_to_sleep");
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.System.putInt(mContext.getContentResolver(), DOUBLE_TAP_SLEEP_GESTURE,
                isChecked ? ON : OFF);
    }

    @Override
    protected String getIllustrationKey() {
        return PREF_KEY_ILLUSTRATION;
    }

    @Override
    public boolean isChecked() {
        boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(),
                DOUBLE_TAP_SLEEP_GESTURE, 0) != 0;
        return isEnabled;
    }

    @Override
    public ResultPayload getResultPayload() {
        final Intent intent = DatabaseIndexingUtils.buildSearchResultPageIntent(mContext,
                TapToSleepGestureSettings.class.getName(), mTapToSleepPrefKey,
                mContext.getString(R.string.gesture_preference_title));
        return new InlineSwitchPayload(DOUBLE_TAP_SLEEP_GESTURE, ResultPayload.SettingsSource.SYSTEM,
                ON /* onValue */, intent, isAvailable(), OFF /* defaultValue */);
    }
}
