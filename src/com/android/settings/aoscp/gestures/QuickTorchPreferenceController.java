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
package com.android.settings.aoscp.gestures;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

import com.android.internal.util.aoscp.FeatureUtils;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class QuickTorchPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "QuickTorchPref";
	private static final String KEY_QUICK_TORCH = "gesture_quick_torch";
  
    private ListPreference mQuickTorch;

    public QuickTorchPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return FeatureUtils.deviceSupportsFlashLight(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_QUICK_TORCH;
    }

    @Override
    public void updateState(Preference preference) {
        final ListPreference mQuickTorch = (ListPreference) preference;
        final Resources res = mContext.getResources();
        if (mQuickTorch != null) {
			int quickTorchValue = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.TORCH_POWER_BUTTON_GESTURE, 0);
            String quickTorchKey = String.valueOf(quickTorchValue);
            mQuickTorch.setValue(quickTorchKey);
            updateQuickTorchSummary(mQuickTorch, quickTorchKey);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String quickTorchKey = (String) newValue;
			int quickTorchValue = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.TORCH_POWER_BUTTON_GESTURE, 0);
			Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.TORCH_POWER_BUTTON_GESTURE,
                    Integer.parseInt(quickTorchKey));
			if (quickTorchValue == 1) {
				//if doubletap for torch is enabled, switch off double tap for camera
				 Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 1);
			}
            updateQuickTorchSummary((ListPreference) preference, quickTorchKey);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not set the quick torch mode", e);
        }
        return true;
    }

    private void updateQuickTorchSummary(Preference mQuickTorch, String quickTorchKey) {
		int quickTorchValue = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.TORCH_POWER_BUTTON_GESTURE, 0);
		String summary = mContext.getString(R.string.quick_torch_disabled_summary);
        if (quickTorchKey != null) {
            if (quickTorchValue == 1) {
				summary = mContext.getString(R.string.quick_torch_double_tap_summary);
			} else if (quickTorchValue == 2) {
				summary = mContext.getString(R.string.quick_torch_longpress_summary);
			} else {
				summary = mContext.getString(R.string.quick_torch_disabled_summary);
			}
        }

        mQuickTorch.setSummary(summary);
        Log.e(TAG, "Invalid value: " + quickTorchKey);
    }
}
