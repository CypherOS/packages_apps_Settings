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

public class DateStylePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "DateStylePref";
	private static final String CLOCK_DATE_STYLE = "clock_date_style";
  
    private ListPreference mDateStyle;

    public DateStylePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return CLOCK_DATE_STYLE;
    }

    @Override
    public void updateState(Preference pref) {
        final ListPreference mDateStyle = (ListPreference) pref;
        final Resources res = mContext.getResources();
        if (mDateStyle != null) {
			mDateStyle.setOnPreferenceChangeListener(this);
			mDateStyle.setValue(Integer.toString(Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_STYLE, 0)));
			mDateStyle.setSummary(mDateStyle.getEntry());
			if (dateIndicatorDisabled()) {
				((ListPreference) pref).setEnabled(false);
			} else { 
			    ((ListPreference) pref).setEnabled(true);
			}
        }
    }
	
	protected boolean dateIndicatorDisabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, 0) != 0;
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (pref == mDateStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mDateStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_STYLE, val);
            mDateStyle.setSummary(mDateStyle.getEntries()[index]);
            return true;
		}
        return false;
    }
}
