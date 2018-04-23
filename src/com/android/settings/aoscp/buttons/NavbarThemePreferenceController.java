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
package com.android.settings.aoscp.buttons;

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

import static android.provider.Settings.Secure.NAVBAR_THEME;

public class NavbarThemePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "NavbarThemePref";
  
    private final String mNavbarThemeKey;
  
    private ListPreference mNavBarTheme;

    public NavbarThemePreferenceController(Context context, String key) {
        super(context);
        mNavbarThemeKey = key;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mNavbarThemeKey;
    }

    @Override
    public void updateState(Preference preference) {
        final ListPreference mNavBarTheme = (ListPreference) preference;
        if (mNavBarTheme != null) {
			int navBarTheme = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.NAVBAR_THEME, 0);
			mNavBarTheme.setValue(String.valueOf(navBarTheme));
			mNavBarTheme.setSummary(mNavBarTheme.getEntry());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            int navBarTheme = Integer.parseInt((String) newValue);
            int index = mNavBarTheme.findIndexOfValue((String) newValue);
			Settings.Secure.putInt(mContext.getContentResolver(), 
			        Settings.Secure.NAVBAR_THEME, navBarTheme);
			mNavBarTheme.setSummary(mNavBarTheme.getEntries()[index]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not persist navbar theme setting", e);
        }
        return true;
    }
}
