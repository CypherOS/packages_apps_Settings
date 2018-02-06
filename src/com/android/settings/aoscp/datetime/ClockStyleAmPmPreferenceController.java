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
import android.text.format.DateFormat;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class ClockStyleAmPmPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "ClockStyleAmPmPref";
	private static final String STATUS_BAR_CLOCK_AM_PM_STYLE = "status_bar_am_pm";
  
    private ListPreference mClockStyleAmPm;

    public ClockStyleAmPmPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return STATUS_BAR_CLOCK_AM_PM_STYLE;
    }
	
	@Override
    public void updateState(Preference preference) {
        final ListPreference mClockStyleAmPm = (ListPreference) preference;
        final Resources res = mContext.getResources();
        if (mClockStyleAmPm != null) {
            int clockStyleAmPmKey = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, 0,
                    UserHandle.USER_CURRENT);
            String amPmKey = String.valueOf(clockStyleAmPmKey);
            mClockStyleAmPm.setValue(amPmKey);
            updateClockStyleAmPmSummary(mClockStyleAmPm, amPmKey);
        }
    }
	
    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        try {
            String amPmKey = (String) newValue;
            Settings.System.putIntForUser(mContext.getContentResolver(), Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE,
                    Integer.parseInt(amPmKey), UserHandle.USER_CURRENT);
            updateClockStyleAmPmSummary((ListPreference) preference, amPmKey);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not set AM/PM setting", e);
        }
        return true;
    }
	
	private void updateClockStyleAmPmSummary(Preference mClockStyleAmPm, String amPmKey) {
		boolean is24hour = DateFormat.is24HourFormat(mContext);
        if (amPmKey != null) {
            String[] values = mContext.getResources().getStringArray(R.array
                    .values_status_bar_am_pm);
            final int summaryArrayResId = R.array.entries_status_bar_am_pm;
            String[] summaries = mContext.getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (amPmKey.equals(values[i])) {
                    if (i < summaries.length) {
						if (is24hour) {
							mClockStyleAmPm.setSummary(R.string.status_bar_am_pm_info);
					    } else {
                            mClockStyleAmPm.setSummary(summaries[i]);
                            return;
						}
                    }
                }
            }
        }

        mClockStyleAmPm.setSummary("");
        Log.e(TAG, "Invalid AM/PM value: " + amPmKey);
    }
}
