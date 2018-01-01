/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.display;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.util.Log;

import com.android.settings.R;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

public class TimeoutPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "TimeoutPrefContr";

    /** If there is no setting in the provider, use this. */
    public static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private final String mScreenTimeoutKey;
	
	private ListPreference mTimeoutPreference;

    public TimeoutPreferenceController(Context context, String key) {
        super(context);
        mScreenTimeoutKey = key;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mScreenTimeoutKey;
    }

    @Override
    public void updateState(Preference preference) {
		final ListPreference mTimeoutPreference = (ListPreference) preference;
        final long currentTimeout = Settings.System.getLong(mContext.getContentResolver(),
                SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
        mTimeoutPreference.setValue(String.valueOf(currentTimeout));
        updateTimeoutPreferenceDescription(mTimeoutPreference, currentTimeout);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContext.getContentResolver(), SCREEN_OFF_TIMEOUT, value);
            updateTimeoutPreferenceDescription((ListPreference) preference, value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist screen timeout setting", e);
        }
        return true;
    }

    public static CharSequence getTimeoutDescription(
            long currentTimeout, CharSequence[] entries, CharSequence[] values) {
        if (currentTimeout < 0 || entries == null || values == null
                || values.length != entries.length) {
            return null;
        }

        for (int i = 0; i < values.length; i++) {
            long timeout = Long.parseLong(values[i].toString());
            if (currentTimeout == timeout) {
                return entries[i];
            }
        }
        return null;
    }

    private void updateTimeoutPreferenceDescription(ListPreference preference,
            long currentTimeout) {
        final CharSequence[] entries = preference.getEntries();
        final CharSequence[] values = preference.getEntryValues();
        final String summary;
        final CharSequence timeoutDescription = getTimeoutDescription(currentTimeout, entries, values);
        summary = timeoutDescription == null
                ? ""
                : mContext.getString(R.string.screen_timeout_summary, timeoutDescription);
        }
        preference.setSummary(summary);
    }

}
