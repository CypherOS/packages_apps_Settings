/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.aoscp.gestures;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.R;
import com.android.settings.Utils;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TWIST_TO_FLIP_ENABLED;

public class DoubleTwistPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_GESTURE_DOUBLE_TWIST = "gesture_double_twist";
    private final UserManager mUserManager;

    public DoubleTwistPreferenceController(Context context) {
        super(context);
        mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
    }

    @Override
    public boolean isAvailable() {
        return hasSensor(R.string.gesture_double_twist_sensor_name,
                R.string.gesture_double_twist_sensor_vendor);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_GESTURE_DOUBLE_TWIST;
    }

    @Override
    public void updateState(Preference preference) {
        int value = Settings.Secure.getInt(mContext.getContentResolver(), CAMERA_DOUBLE_TWIST_TO_FLIP_ENABLED, 1);
        ((SwitchPreference) preference).setChecked(value != 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final int enabled = (boolean) newValue ? 1 : 0;
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.CAMERA_DOUBLE_TWIST_TO_FLIP_ENABLED, enabled);
        final int managedProfileUserId = getManagedProfileUserId();
        if (managedProfileUserId != UserHandle.USER_NULL) {
            Settings.Secure.putIntForUser(mContext.getContentResolver(),
                Settings.Secure.CAMERA_DOUBLE_TWIST_TO_FLIP_ENABLED, enabled, managedProfileUserId);
        }
        return true;
    }

    @VisibleForTesting
    int getManagedProfileUserId() {
        return Utils.getManagedProfileId(mUserManager, UserHandle.myUserId());
    }

    private boolean hasSensor(int nameResId, int vendorResId) {
        final Resources resources = mContext.getResources();
        final String name = resources.getString(nameResId);
        final String vendor = resources.getString(vendorResId);
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(vendor)) {
            final SensorManager sensorManager =
                    (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            for (Sensor s : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
                if (name.equals(s.getName()) && vendor.equals(s.getVendor())) {
                    return true;
                }
            }
        }
        return false;
    }
}