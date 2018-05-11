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

package com.android.settings.aoscp.doze;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.preference.Preference;

import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.R;
import com.android.settings.aoscp.preference.IllustrationPreferenceController;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;

import static android.provider.Settings.Secure.DOZE_PULSE_ON_HAND_WAVE;

public class HandWaveGesturePreferenceController extends IllustrationPreferenceController {

    private static final String PREF_KEY_ILLUSTRATION = "gesture_ambient_hand_wave_video";
    private final String mAmbientHandWaveKey;
	
	private final AmbientDisplayConfiguration mAmbientConfig;

    public HandWaveGesturePreferenceController(Context context, AmbientDisplayConfiguration config, String key) {
        super(context);
        mAmbientHandWaveKey = key;
		mAmbientConfig = config;
    }

    @Override
    public boolean isAvailable() {
        return mAmbientConfig.pulseOnHandWaveAvailable();
    }

    @Override
    public String getPreferenceKey() {
        return mAmbientHandWaveKey;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (boolean) newValue;
        Settings.Secure.putInt(mContext.getContentResolver(), DOZE_PULSE_ON_HAND_WAVE, enabled ? ON : OFF);
        return true;
    }

    @Override
    protected String getIllustrationKey() {
        return PREF_KEY_ILLUSTRATION;
    }

    @Override
    protected boolean isSwitchPrefEnabled() {
        final int ambientHandWaveEnabled = Settings.Secure.getInt(
                  mContext.getContentResolver(), DOZE_PULSE_ON_HAND_WAVE, OFF);
        return ambientHandWaveEnabled != 0;
    }

    @Override
    public ResultPayload getResultPayload() {
        final Intent intent = DatabaseIndexingUtils.buildSubsettingIntent(mContext,
                "HandWaveSettings", mAmbientHandWaveKey,
                mContext.getString(R.string.gesture_settings_title));

        return new InlineSwitchPayload(DOZE_PULSE_ON_HAND_WAVE, ResultPayload.SettingsSource.SECURE,
                ON /* onValue */, intent, isAvailable(), ON /* defaultValue */);
    }
}