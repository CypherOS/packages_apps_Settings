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
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.preference.Preference;

import com.android.settings.R;
import com.android.settings.aoscp.preference.IllustrationPreferenceController;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;

import static android.provider.Settings.Secure.NIGHT_DISPLAY_ACTIVATED;

public class NightDisplayPreferenceController extends IllustrationPreferenceController {

    private static final String PREF_KEY_ILLUSTRATION = "night_display_video";
    private final String mNightDisplayKey;
	
	private boolean mNightDisplayEnabled;

    public NightDisplayPreferenceController(Context context, String key) {
        super(context);
        mNightDisplayKey = key;
    }

    @Override
    public boolean isAvailable() {
        return NightDisplayController.isAvailable(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return mNightDisplayKey;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (boolean) newValue;
        Settings.Secure.putInt(mContext.getContentResolver(), NIGHT_DISPLAY_ACTIVATED, enabled ? ON : OFF);
		return mController.setActivated((Boolean) newValue);
        return true;
    }

    @Override
    protected String getIllustrationKey() {
        return PREF_KEY_ILLUSTRATION;
    }

    @Override
    protected boolean isSwitchPrefEnabled() {
        final int nightDisplayEnabled = Settings.Secure.getInt(
                  mContext.getContentResolver(), NIGHT_DISPLAY_ACTIVATED, ON);
        return nightDisplayEnabled != 0;
    }

	@Override
	protected int getSummary() {
		mNightDisplayEnabled = isSwitchPrefEnabled();
        return mNightDisplayEnabled
            ? R.string.night_display_on
            : R.string.night_display_off;
    }

    @Override
    public ResultPayload getResultPayload() {
        final Intent intent = DatabaseIndexingUtils.buildSubsettingIntent(mContext,
                "NightDisplaySettings", mNightDisplayKey,
                mContext.getString(R.string.display_settings_title));

        return new InlineSwitchPayload(NIGHT_DISPLAY_ACTIVATED, ResultPayload.SettingsSource.SECURE,
                ON /* onValue */, intent, isAvailable(), ON /* defaultValue */);
    }
}