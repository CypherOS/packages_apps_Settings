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
 * limitations under the License
 */

package com.android.settings.aoscp.display;

import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import aoscp.hardware.DeviceHardwareManager;
import aoscp.hardware.DisplayMode;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class DisplayModeListPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

	private static final String PREF_DISPLAY_MODE = "display_modes";
	
	private static final String DISPLAY_MODE_TITLE =
            PREF_DISPLAY_MODE + "_%s_title";

    private static final String DISPLAY_MODE_SUMMARY =
            PREF_DISPLAY_MODE + "_%s_summary";

    private Context mContext;
	private DeviceHardwareManager mHwManager;
    private ListPreference mPref;
	private String[] mDisplayModeSummaries;

    private int mDefaultBehavior;
    private final String mKey;

    public DisplayModeListPreferenceController(Context context) {
        super(context, key);
        mContext = context;
		mHwManager = DeviceHardwareManager.getInstance(mContext);
    }

    @Override
    public int getAvailabilityStatus() {
        return mHwManager.isSupported(DeviceHardwareManager.FEATURE_DISPLAY_ENGINE) ? 
                AVAILABLE : DISABLED_FOR_USER;
    }

    @Override
    public String getPreferenceKey() {
        return PREF_DISPLAY_MODE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPref = (ListPreference) screen.findPreference(getPreferenceKey());
        if (mPref == null) return;
        updateDisplayModes();
    }
	
	private boolean updateDisplayModes() {
        final DisplayMode[] modes = mHwManager.getDisplayModes();
        if (modes == null || modes.length == 0) {
            return false;
        }

        final DisplayMode cur = mHwManager.getCurrentDisplayMode() != null
                ? mHwManager.getCurrentDisplayMode() : mHwManager.getDefaultDisplayMode();
        int curId = -1;
        String[] entries = new String[modes.length];
        String[] values = new String[modes.length];
        mDisplayModeSummaries = new String[modes.length];
        for (int i = 0; i < modes.length; i++) {
            values[i] = String.valueOf(modes[i].id);
            entries[i] = getLocalizedString(
                    getResources(), modes[i].name, DISPLAY_MODE_TITLE);

            // Populate summary
            String summary = getLocalizedString(
                    getResources(), modes[i].name, DISPLAY_MODE_SUMMARY);
            if (summary != null) {
                summary = String.format("%s - %s", entries[i], summary);
            }
            mDisplayModeSummaries[i] = summary;

            if (cur != null && modes[i].id == cur.id) {
                curId = cur.id;
            }
        }
        mPref.setEntries(entries);
        mPref.setEntryValues(values);
        if (curId >= 0) {
            mPref.setValue(String.valueOf(curId));
        }

        return true;
    }
	
	private void updateSummary(String value) {
		mSummary = value;
        if (mSummary == null) {
            DisplayMode cur = mHwManager.getCurrentDisplayMode() != null
                    ? mHwManager.getCurrentDisplayMode() : mHwManager.getDefaultDisplayMode();
            if (cur != null && cur.id >= 0) {
                mSummary = String.valueOf(cur.id);
            }
        }
        mPref.setValue(mSummary);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int id = Integer.valueOf((String)newValue);
		for (DisplayMode mode : mHwManager.getDisplayModes()) {
			if (mode.id == id) {
				mHwManager.setDisplayMode(mode, true);
				updateSummary((String)newValue);
				refreshSummary(preference);
				break;
			}
		}
        return true;
    }

    @Override
    public CharSequence getSummary() {
        int index = mPref.findIndexOfValue(mSummary);
        return mDisplayModeSummaries[index];
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
