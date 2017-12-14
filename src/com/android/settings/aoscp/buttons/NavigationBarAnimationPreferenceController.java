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

import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.NAVIGATION_BAR_ANIMATION;

public class NavigationBarAnimationPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "NavigationBarAnimationPref";
  
    private final String mNavigationBarAnimationKey;

    private final Resources res = mContext.getResources();

    public NavigationBarAnimationPreferenceController(Context context, String key) {
        super(context);
        mNavigationBarAnimationKey = key;
    }

    @Override
    public boolean isAvailable() {
        boolean available = res.getBoolean(com.android.internal.R.bool.config_allowOpaLayout);
        if (mNavigationBarAnimationKey != null) {
            if (available) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return mNavigationBarAnimationKey;
    }

    @Override
    public void updateState(Preference preference) {
        boolean defaultToNavigationBar = res.getBoolean(com.android.internal.R.bool.config_defaultToNavigationBar);
        boolean navigationBarEnabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_ENABLED, defaultToNavigationBar ? 1 : 0, UserHandle.USER_CURRENT) != 0;
        int value = Settings.System.getInt(mContext.getContentResolver(), NAVIGATION_BAR_ANIMATION, 1);
        ((SwitchPreference) preference).setChecked(value != 0);
        ((SwitchPreference) preference).setEnabled(navigationBarEnabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_ANIMATION, enabled ? 1 : 0);
        return true;
    }
}
