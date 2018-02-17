/*
 * Copyright (C) 2018 CypherOS
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
package com.android.settings.aoscp.datetime;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class DateDisplayPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "DateDisplayPref";
	private static final String CLOCK_DATE_DISPLAY = "clock_date_display";
  
    private ListPreference mDateDisplay;

    public DateDisplayPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return CLOCK_DATE_DISPLAY;
    }

	@Override
    public void updateState(Preference preference) {
        final ListPreference mDateDisplay = (ListPreference) preference;
        final Resources res = mContext.getResources();
        if (mDateDisplay != null) {
            int dateDisplayKey = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, 0,
                    UserHandle.USER_CURRENT);
            String dateKey = String.valueOf(dateDisplayKey);
            mDateDisplay.setValue(dateKey);
            updateDateDisplaySummary(mDateDisplay, dateKey);
        }
    }

	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String dateKey = (String) newValue;
            Settings.System.putIntForUser(mContext.getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY,
                    Integer.parseInt(dateKey), UserHandle.USER_CURRENT);
            updateDateDisplaySummary((ListPreference) preference, dateKey);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not set date display", e);
        }
        return true;
    }
	
	private void updateDateDisplaySummary(Preference mDateDisplay, String dateKey) {
        if (dateKey != null) {
            String[] values = mContext.getResources().getStringArray(R.array
                    .clock_date_display_values);
            final int summaryArrayResId = R.array.clock_date_display_entries;
            String[] summaries = mContext.getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (dateKey.equals(values[i])) {
                    if (i < summaries.length) {
                        mDateDisplay.setSummary(summaries[i]);
                        return;
                    }
                }
            }
        }

        mDateDisplay.setSummary("");
        Log.e(TAG, "Invalid date display value: " + dateKey);
    }
}
