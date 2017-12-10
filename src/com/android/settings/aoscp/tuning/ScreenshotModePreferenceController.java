/*
 * Copyright (C) 2017 CypherOS
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
package com.android.settings.aoscp.tuning;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.ListWithEntrySummaryPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.SCREENSHOT_TYPE;

public class ScreenshotModePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "ScreenshotModePref";
  
    private final String mScreenTypeKey;
  
    private ListWithEntrySummaryPreference mScreenshotType;

    public ScreenshotModePreferenceController(Context context, String key) {
        super(context);
        mScreenTypeKey = key;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mScreenTypeKey;
    }

    @Override
    public void updateState(Preference preference) {
        final ListWithEntrySummaryPreference mScreenshotType = (ListWithEntrySummaryPreference) preference;
        if (mScreenshotType != null) {
            int mScreenshotTypeValue = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.SCREENSHOT_TYPE, 0);
            String modeValue = String.valueOf(mScreenshotTypeValue);
            mScreenshotType.setValue(modeValue);
            mScreenshotType.setEntrySummaries(R.array.screenshot_type_entry_summaries);
            updateScreenshotModeSummary(mScreenshotType, modeValue);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String modeValue = (String) newValue;
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREENSHOT_TYPE,
                    Integer.parseInt(modeValue));
            updateScreenshotModeSummary((ListWithEntrySummaryPreference) preference, modeValue);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not persist screenshot mode setting", e);
        }
        return true;
    }

    private void updateScreenshotModeSummary(Preference mScreenshotType, String modeValue) {
        if (modeValue != null) {
            String[] values = mContext.getResources().getStringArray(R.array
                    .screenshot_type_values);
            final int summaryArrayResId = R.array.screenshot_type_entries;
            String[] summaries = mContext.getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (modeValue.equals(values[i])) {
                    if (i < summaries.length) {
                        mScreenshotType.setSummary(summaries[i]);
                        return;
                    }
                }
            }
        }

        mScreenshotType.setSummary("");
        Log.e(TAG, "Invalid screenshot mode value: " + modeValue);
    }
}
