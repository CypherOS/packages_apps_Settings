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

public class ClockStylePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "ClockStylePref";
    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock_style";
  
    private ListPreference mClockStyle;

    public ClockStylePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return STATUS_BAR_CLOCK_STYLE;
    }

    @Override
    public void updateState(Preference preference) {
        final ListPreference mClockStyle = (ListPreference) preference;
        final Resources res = mContext.getResources();
        if (mClockStyle != null) {
            int clockStyleKey = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_STYLE, 0,
                    UserHandle.USER_CURRENT);
            String styleKey = String.valueOf(clockStyleKey);
            mClockStyle.setValue(styleKey);
            updateClockStyleSummary(mClockStyle, styleKey);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String styleKey = (String) newValue;
            Settings.System.putIntForUser(mContext.getContentResolver(), Settings.System.STATUSBAR_CLOCK_STYLE,
                    Integer.parseInt(styleKey), UserHandle.USER_CURRENT);
            updateClockStyleSummary((ListPreference) preference, styleKey);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not set clock style", e);
        }
        return true;
    }

    private void updateClockStyleSummary(Preference mClockStyle, String styleKey) {
        if (styleKey != null) {
            String[] values = mContext.getResources().getStringArray(R.array
                    .clock_style_values);
            final int summaryArrayResId = R.array.clock_style_entries;
            String[] summaries = mContext.getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (styleKey.equals(values[i])) {
                    if (i < summaries.length) {
                        mClockStyle.setSummary(summaries[i]);
                        return;
                    }
                }
            }
        }

        mClockStyle.setSummary("");
        Log.e(TAG, "Invalid clock style value: " + styleKey);
    }
}
