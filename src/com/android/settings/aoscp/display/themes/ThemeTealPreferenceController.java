/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.settings.aoscp.display.themes;

import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.aoscp.display.ThemeOverlayManager;
import com.android.settings.aoscp.display.ThemePreferenceFragment;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class ThemeTealPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {
    private static final String TAG = "TealTheme";
    private static final String KEY_THEME_TEAL = "theme_teal";
	
	private ThemeOverlayManager mThemeOM;
	private ThemePreferenceFragment mThemeMgr;
	
    public ThemeTealPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_THEME_TEAL;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		String current = mThemeMgr.getTheme();
        if (Objects.equal(newValue, current)) {
            return true;
        }
        try {
            mThemeOM.setEnabled(("co.aoscp.theme.teal") newValue, true, UserHandle.myUserId());
        } catch (RemoteException e) {
            return false;
        }
        return true;
    }
}
