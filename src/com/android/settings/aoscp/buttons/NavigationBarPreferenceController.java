/*
 * Copyright (C) 2018 CypherOS
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
 * limitations under the License.
 */
package com.android.settings.aoscp.buttons;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.widget.Toast;

import aoscp.hardware.DeviceHardwareManager;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import static android.provider.Settings.System.NAVIGATION_BAR_ENABLED;

public class NavigationBarPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private Context mContext;
    private String mKey;
    private Toast mNotifyFpNavStatus;

    private boolean mIsFingerprintNavigation;
    private int mDeviceHardwareKeys;

    public NavigationBarPreferenceController(Context context, String key) {
        super(context);
        mContext = context;
        mKey = key;
        mDeviceHardwareKeys = context.getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
    }

    @Override
    public boolean isAvailable() {
        final DeviceHardwareManager hwManager = DeviceHardwareManager.getInstance(mContext);
        mIsFingerprintNavigation = hwManager.get(DeviceHardwareManager.FEATURE_FINGERPRINT_NAVIGATION);
        return mDeviceHardwareKeys != 0 || mIsFingerprintNavigation;
    }

    @Override
    public String getPreferenceKey() {
        return mKey;
    }

    @Override
    public void updateState(Preference preference) {
        final boolean defaultToNavigationBar = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_defaultToNavigationBar);
        final boolean navigationBarEnabled = Settings.System.getIntForUser(
                mContext.getContentResolver(), NAVIGATION_BAR_ENABLED,
                defaultToNavigationBar ? 1 : 0, UserHandle.USER_CURRENT) != 0;
        ((SwitchPreference) preference).setChecked(navigationBarEnabled);
        mNotifyFpNavStatus = Toast.makeText(mContext, "Fingerprint navigation enabled",
                Toast.LENGTH_LONG);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(), NAVIGATION_BAR_ENABLED,
                enabled ? 1 : 0);
        if (mIsFingerprintNavigation && !enabled) {
            mNotifyFpNavStatus.show();
        }
        return true;
    }
}
