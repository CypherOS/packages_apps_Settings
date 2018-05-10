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
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.preference.Preference;

import com.android.settings.R;
import com.android.settings.aoscp.preference.IllustrationPreferenceController;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;

import static android.provider.Settings.System.GESTURE_DOUBLE_TAP_SLEEP;

public class TapToSleepPreferenceController extends IllustrationPreferenceController {

    private static final String PREF_KEY_ILLUSTRATION = "gesture_tap_to_sleep_video";
    private final String mTapToSleepKey;

    public TapToSleepPreferenceController(Context context, String key) {
        super(context);
        mTapToSleepKey = key;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mTapToSleepKey;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(), GESTURE_DOUBLE_TAP_SLEEP, enabled ? ON : OFF);
        return true;
    }

    @Override
    protected String getIllustrationKey() {
        return PREF_KEY_ILLUSTRATION;
    }

    @Override
    protected boolean isSwitchPrefEnabled() {
        final int tapToSleepEnabled = Settings.System.getInt(
		        mContext.getContentResolver(), GESTURE_DOUBLE_TAP_SLEEP, OFF);
        return tapToSleepEnabled != 0;
    }

    @Override
    public ResultPayload getResultPayload() {
        final Intent intent = DatabaseIndexingUtils.buildSubsettingIntent(mContext,
                "TapToSleepSettings", mTapToSleepKey,
                mContext.getString(R.string.gesture_settings_title));

        return new InlineSwitchPayload(GESTURE_DOUBLE_TAP_SLEEP, ResultPayload.SettingsSource.SYSTEM,
                ON /* onValue */, intent, isAvailable(), ON /* defaultValue */);
    }
}
