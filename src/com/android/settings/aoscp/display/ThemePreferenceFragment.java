/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.widget.RadioButtonPreference;

import com.android.settings.SettingsPreferenceFragment;

public class ThemePreferenceFragment extends SettingsPreferenceFragment
        implements RadioButtonPreference.OnClickListener {
			
			
    private static final String KEY_THEME_DEFAULT = "theme_default";
    private static final String KEY_THEME_TEAL = "theme_teal";
	
	private RadioButtonPreference mThemeDefault;
	private RadioButtonPreference mThemeTeal;
	
	private ThemeOverlayManager mThemeOM;
	
	private boolean mActive = false;
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }

    @Override
    public void onResume() {
        super.onResume();
		mActive = true;
        createPreferenceHierarchy();
    }

    @Override
    public void onPause() {
        super.onPause();
		mActive = false;
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen prefScreen = getPreferenceScreen();
        if (prefScreen != null) {
            prefScreen.removeAll();
        }
        addPreferencesFromResource(R.xml.theme_settings);
        prefScreen = getPreferenceScreen();

        mThemeDefault = (RadioButtonPreference) prefScreen.findPreference(KEY_THEME_DEFAULT);
        mThemeTeal = (RadioButtonPreference) prefScreen.findPreference(KEY_THEME_TEAL);
		
        mThemeDefault.setOnClickListener(this);
        mThemeTeal.setOnClickListener(this);

        return prefScreen;
    }

    private void updateRadioButtons(RadioButtonPreference pref) {
        if (pref == null) {
            mThemeDefault.setChecked(false);
            mThemeTeal.setChecked(false);
        } else if (pref == mThemeDefault) {
            mThemeDefault.setChecked(true);
            mThemeTeal.setChecked(false);
        } else if (pref == mThemeTeal) {
            mThemeDefault.setChecked(false);
            mThemeTeal.setChecked(true);
        }
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference pref) {
        String theme = null;
        if (pref == mThemeDefault) {
            theme = THEME_DEFAULT;
			try {
                mThemeOM.setEnabled(THEME_DEFAULT, true, UserHandle.myUserId());
            } catch (RemoteException e) {
            }
        } else if (pref == mThemeTeal) {
            theme = THEME_TEAL;
			try {
                mThemeOM.setEnabled(THEME_TEAL, true, UserHandle.myUserId());
            } catch (RemoteException e) {
            }
        }
        setTheme(theme);
    }
	
	public void setTheme(String theme) {
        Context context = getActivity();
        theme = THEME_DEFAULT, THEME_TEAL;
        if (mActive) {
            onThemeChanged(theme);
        }
    }

    public void onThemeChanged(String theme) {
        switch (theme) {
            case THEME_DEFAULT:
                updateRadioButtons(mThemeDefault);
                break;
            case THEME_TEAL:
                updateRadioButtons(mThemeTeal);
                break;
            default:
                break;
        }
    }
}
