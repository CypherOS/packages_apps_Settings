/*
 * Copyright (C) 2018 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.settings.aoscp.display;

import static android.provider.Settings.Secure.SYSTEM_ACCENT;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

/**
 * Setting where user can pick if SystemUI will be light, dark or try to match
 * the wallpaper colors.
 */
public class SystemAccentPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private ListPreference mAccentPref;
	private final String mKey;

    public SystemAccentPreferenceController(Context context, String key) {
        super(context, key);
		mKey = key;
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources().getBoolean(
		        com.android.internal.R.bool.config_colorManagerAvailable);
    }

	@Override
    public String getPreferenceKey() {
        return mKey;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mAccentPref = (ListPreference) screen.findPreference(getPreferenceKey());
        int value = Settings.Secure.getInt(mContext.getContentResolver(), SYSTEM_ACCENT, 0);
        mAccentPref.setValue(Integer.toString(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt((String) newValue);
        Settings.Secure.putInt(mContext.getContentResolver(), SYSTEM_ACCENT, value);
        refreshSummary(preference);
        return true;
    }

    @Override
    public CharSequence getSummary() {
        int value = Settings.Secure.getInt(mContext.getContentResolver(), SYSTEM_ACCENT, 0);
        int index = mAccentPref.findIndexOfValue(Integer.toString(value));
        return mAccentPref.getEntries()[index];
    }
}
