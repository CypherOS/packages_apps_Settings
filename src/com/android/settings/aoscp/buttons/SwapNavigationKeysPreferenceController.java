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
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.core.PreferenceController;

import static android.provider.Settings.System.SWAP_NAVIGATION_KEYS;

public class SwapNavigationKeysPreferenceController extends PreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "SwapNavigationKeysPref";
  
    private final String mSwapNavigationKeysKey;

    public SwapNavigationKeysPreferenceController(Context context, String key) {
        super(context);
        mSwapNavigationKeysKey = key;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mSwapNavigationKeysKey;
    }

    @Override
    public void updateState(Preference preference) {
        int value = Settings.System.getInt(mContext.getContentResolver(), SWAP_NAVIGATION_KEYS, 1);
        ((SwitchPreference) preference).setChecked(value != 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.SWAP_NAVIGATION_KEYS, enabled ? 1 : 0);
        return true;
    }
}
