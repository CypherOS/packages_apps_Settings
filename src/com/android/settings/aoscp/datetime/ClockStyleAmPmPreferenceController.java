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
    public void updateState(Preference pref) {
        final ListPreference mClockStyleAmPm = (ListPreference) pref;
        final Resources res = mContext.getResources();
        if (mClockStyleAmPm != null) {
			mClockStyleAmPm.setOnPreferenceChangeListener(this);
			mClockStyleAmPm.setValue(Integer.toString(Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, 0)));
			boolean is24hour = DateFormat.is24HourFormat(getActivity());
            if (is24hour) {
                mClockStyleAmPm.setSummary(R.string.status_bar_am_pm_info);
            } else {
                mClockStyleAmPm.setSummary(mClockStyleAmPm.getEntry());
            }
            mClockStyleAmPm.setEnabled(!is24hour);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (pref == mClockStyleAmPm) {
            int val = Integer.parseInt((String) newValue);
            int index = mClockStyleAmPm.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, val);
            mClockStyleAmPm.setSummary(mClockStyleAmPm.getEntries()[index]);
            return true;
		}
        return false;
    }
}
