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

import com.android.settings.R;
import com.android.settings.ListWithEntrySummaryPreference;
import com.android.settings.core.PreferenceController;

import static android.provider.Settings.System.SCREENSHOT_TYPE;

public class ScreenshotModePreferenceController extends PreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "ScreenshotModePref";
	private static final String KEY_SCREENSHOT_TYPE = "screenshot_type";
	
	private ListWithEntrySummaryPreference mScreenshotType;

    public ScreenshotModePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SCREENSHOT_TYPE;
    }

    @Override
    public void updateState(Preference preference) {
		final ContentResolver resolver = mContext.getContentResolver();
        final ListWithEntrySummaryPreference mScreenshotType = (ListWithEntrySummaryPreference) preference;
        int mScreenshotTypeValue = Settings.System.getInt(resolver, 
		        Settings.System.SCREENSHOT_TYPE, 0);
		mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
		mScreenshotType.setSummary(mScreenshotType.getEntry());
		mScreenshotType.setEntrySummaries(R.array.screenshot_type_entry_summaries);
		mScreenshotType.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = mContext.getContentResolver();
        if  (preference == mScreenshotType) {
            int mScreenshotTypeValue = Integer.parseInt(((String) newValue).toString());
            mScreenshotType.setSummary(
                    mScreenshotType.getEntries()[mScreenshotTypeValue]);
            Settings.System.putInt(resolver,
                    Settings.System.SCREENSHOT_TYPE, mScreenshotTypeValue);
            mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
            return true;
        }
        return false;
    }
}
