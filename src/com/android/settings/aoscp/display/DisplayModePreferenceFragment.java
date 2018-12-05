/*
 * Copyright (C) 2018 CypherOS
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
package com.android.settings.aoscp.display;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceScreen;

import aoscp.hardware.DeviceHardwareManager;

import com.android.settings.applications.LayoutPreference;
import com.android.settings.R;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.widget.CandidateInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class DisplayModePreferenceFragment extends RadioButtonPickerFragment {

    @VisibleForTesting
    static final String KEY_DISPLAY_MODE_DEFAULT = "display_mode_default";
    @VisibleForTesting
    static final String KEY_DISPLAY_MODE_1 = "display_mode_1";
    @VisibleForTesting
    static final String KEY_DISPLAY_MODE_2 = "display_mode_2";
    @VisibleForTesting
    static final String KEY_DISPLAY_MODE_3 = "display_mode_3";
	@VisibleForTesting
    static final String KEY_DISPLAY_MODE_4 = "display_mode_4";

    private DeviceHardwareManager mHwManager;

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

    @VisibleForTesting
    void configureAndInstallPreview(LayoutPreference preview, PreferenceScreen screen) {
        preview.setSelectable(false);
        screen.addPreference(preview);
    }

    @Override
    protected void addStaticPreferences(PreferenceScreen screen) {
        final LayoutPreference preview = new LayoutPreference(screen.getContext(),
                R.layout.color_mode_preview);
        configureAndInstallPreview(preview, screen);
    }

    @Override
    protected List<? extends CandidateInfo> getCandidates() {
        final Context ctx = getContext();
        final int[] availableModes = mHwManager.getDisplayModes();

        List<DisplayModeCandidateInfo> candidates = new ArrayList<DisplayModeCandidateInfo>();
        if (availableModes != null) {
            for (int colorMode : availableModes) {
                if (colorMode == 0) {
                    candidates.add(new DisplayModeCandidateInfo(
                                ctx.getText(R.string.display_mode_option_default),
                                KEY_DISPLAY_MODE_DEFAULT, true /* enabled */));
                } else if (colorMode == 1) {
                    candidates.add(new DisplayModeCandidateInfo(
                                ctx.getText(R.string.display_mode_option_1),
                                KEY_DISPLAY_MODE_1, true /* enabled */));
                } else if (colorMode == 2) {
                    candidates.add(new DisplayModeCandidateInfo(
                                ctx.getText(R.string.display_mode_option_2),
                                KEY_DISPLAY_MODE_2, true /* enabled */));
                } else if (colorMode == 3) {
                    candidates.add(new DisplayModeCandidateInfo(
                                ctx.getText(R.string.display_mode_option_3),
                                KEY_DISPLAY_MODE_3, true /* enabled */));
				} else if (colorMode == 3) {
                    candidates.add(new DisplayModeCandidateInfo(
                                ctx.getText(R.string.display_mode_option_4),
                                KEY_DISPLAY_MODE_4, true /* enabled */));
                }
            }
        }
        return candidates;
    }

    @Override
    protected String getDefaultKey() {
        final int colorMode = mHwManager.getCurrentDisplayMode();
		if (colorMode == 4) {
            return KEY_DISPLAY_MODE_4;
        } else if (colorMode == 3) {
            return KEY_DISPLAY_MODE_3;
        } else if (colorMode == 2) {
            return KEY_DISPLAY_MODE_2;
        } else if (colorMode == 1) {
            return KEY_DISPLAY_MODE_1;
        }
        return KEY_DISPLAY_MODE_DEFAULT;
    }

    @Override
    protected boolean setDefaultKey(String key) {
        switch (key) {
            case KEY_DISPLAY_MODE_DEFAULT:
                mHwManager.setDisplayMode(0, true);
                break;
            case KEY_DISPLAY_MODE_1:
                mHwManager.setDisplayMode(1, true);
                break;
            case KEY_DISPLAY_MODE_2:
                mHwManager.setDisplayMode(2, true);
                break;
            case KEY_DISPLAY_MODE_3:
                mHwManager.setDisplayMode(3, true);
                break;
			case KEY_DISPLAY_MODE_4:
                mHwManager.setDisplayMode(4, true);
                break;
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @VisibleForTesting
    static class DisplayModeCandidateInfo extends CandidateInfo {
        private final CharSequence mLabel;
        private final String mKey;

        DisplayModeCandidateInfo(CharSequence label, String key, boolean enabled) {
            super(enabled);
            mLabel = label;
            mKey = key;
        }

        @Override
        public CharSequence loadLabel() {
            return mLabel;
        }

        @Override
        public Drawable loadIcon() {
            return null;
        }

        @Override
        public String getKey() {
            return mKey;
        }
    }
}
