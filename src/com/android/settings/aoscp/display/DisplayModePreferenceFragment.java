/*
 * Copyright (C) 2018-2019 CypherOS
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

package com.android.settings.aoscp.display;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import aoscp.hardware.DisplayMode;
import aoscp.hardware.DeviceHardwareManager;

import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.widget.RadioButtonPreference;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;

public class DisplayModePreferenceFragment extends DashboardFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "DisplayModePreferenceFragment";

	private static final String KEY_DISPLAY_MODE = "display_mode_";

    List<RadioButtonPreference> mAvailableModes = new ArrayList<>();

    private Context mContext;
	private DeviceHardwareManager mHwManager;
	private RadioButtonPreference mModePref;

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
	
	@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mHwManager = DeviceHardwareManager.getInstance(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mHwManager != null) {
            mHwManager = null;
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.display_mode_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

	@VisibleForTesting
    void configureAndInstallPreview(LayoutPreference preview, PreferenceScreen screen) {
        preview.setSelectable(false);
        screen.addPreference(preview);
    }

    @Override
    public void displayResourceTiles() {
        super.displayResourceTiles();
        final PreferenceScreen screen = getPreferenceScreen();
		final LayoutPreference preview = new LayoutPreference(screen.getContext(),
                R.layout.color_mode_preview);
        configureAndInstallPreview(preview, screen);

        int currentModeId = -1;
		final DisplayMode currentMode = mHwManager.getCurrentDisplayMode() != null
                ? mHwManager.getCurrentDisplayMode() : mHwManager.getDefaultDisplayMode();
		final DisplayMode[] modes = mHwManager.getDisplayModes();
		for (int i = 0; i < modes.length; i++) {
			Preference pref = screen.getPreference(i);
			if (pref instanceof RadioButtonPreference) {
				mModePref = (RadioButtonPreference) pref;
				mModePref.setKey(KEY_DISPLAY_MODE + String.valueOf(modes[i].id));
				mModePref.setTitle(modes[i].name);
				mModePref.setOnPreferenceChangeListener(this);
				mAvailableModes.add(mModePref);
				if (currentMode != null && modes[i].id == currentMode.id) {
					currentModeId = currentMode.id;
				}
			}
		}
		
		if (currentModeId >= 0) {
			updateDisplayMode(String.valueOf(currentModeId));
		}
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        return controllers;
    }

    private void updateDisplayMode(String selectionKey) {
        for (RadioButtonPreference pref : mAvailableModes) {
            if (selectionKey.equals(pref.getKey())) {
                pref.setChecked(true);
            } else {
                pref.setChecked(false);
            }
        }
    }

	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mModePref) {
            int id = Integer.valueOf((String)objValue);
            for (DisplayMode mode : mHwManager.getDisplayModes()) {
                if (mode.id == id) {
                    mHwManager.setDisplayMode(mode, true);
					String newMode = String.valueOf(mode.id);
					updateDisplayMode(newMode);
                    break;
                }
            }
        }
        return true;
    }

	public String getLocalizedString(final Resources res, final String stringName,
            final String stringFormat) {
        final String name = stringName.toLowerCase().replace(" ", "_");
        final String nameRes = String.format(stringFormat, name);
        return getStringForResourceName(res, nameRes, stringName);
    }

	public String getStringForResourceName(final Resources res, final String resourceName,
            final String defaultValue) {
        final int resId = res.getIdentifier(resourceName, "string", "com.android.settings");
        if (resId <= 0) {
            Log.e(TAG, "No resource found for " + resourceName);
            return defaultValue;
        } else {
            return res.getString(resId);
        }
    }
}
