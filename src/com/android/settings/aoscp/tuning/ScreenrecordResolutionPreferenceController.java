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
package com.android.settings.aoscp.tuning;

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

public class ScreenrecordResolutionPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "ScreenrecordResolutionPref";
  
    private final String mScreenrecordResKey;
  
    private ListPreference mScreenrecordRes;

    public ScreenrecordResolutionPreferenceController(Context context, String key) {
        super(context);
        mScreenrecordResKey = key;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mScreenrecordResKey;
    }

    @Override
    public void updateState(Preference preference) {
        final ListPreference mScreenrecordRes = (ListPreference) preference;
        final Resources res = mContext.getResources();
        if (mScreenrecordRes != null) {
            int screenRecordResValue = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.SCREEN_RECORD_QUALITY, 0,
                    UserHandle.USER_CURRENT);
            String resKey = String.valueOf(screenRecordResValue);
            mScreenrecordRes.setValue(resKey);
            updateScreenrecordResSummary(mScreenrecordRes, resKey);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String resKey = (String) newValue;
            Settings.System.putIntForUser(mContext.getContentResolver(), Settings.System.SCREEN_RECORD_QUALITY,
                    Integer.parseInt(resKey), UserHandle.USER_CURRENT);
            updateScreenrecordResSummary((ListPreference) preference, resKey);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not change screenrecord res setting", e);
        }
        return true;
    }

    private void updateScreenrecordResSummary(Preference mScreenrecordRes, String resKey) {
        if (resKey != null) {
            String[] values = mContext.getResources().getStringArray(R.array
                    .screenrecord_resolution_values);
            final int summaryArrayResId = R.array.screenrecord_resolution_entries;
            String[] summaries = mContext.getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (resKey.equals(values[i])) {
                    if (i < summaries.length) {
                        mScreenrecordRes.setSummary(summaries[i]);
                        return;
                    }
                }
            }
        }

        mScreenrecordRes.setSummary("");
        Log.e(TAG, "Invalid screenrecord res value: " + resKey);
    }
}
