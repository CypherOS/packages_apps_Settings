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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.widget.RadioButtonPreference;

import com.android.settings.SettingsPreferenceFragment;

public class ThemePreferenceFragment extends SettingsPreferenceFragment
        implements RadioButtonPreference.OnClickListener {
			
			
	/** Broadcast intent action when the theme is about to change. */
    private static final String THEME_CHANGED_ACTION =
            "com.android.settings.aoscp.display.THEME_CHANGED";
    private static final String CURRENT_THEME_KEY = "CURRENT_MODE";
    private static final String NEW_THEME_KEY = "NEW_MODE";
			
    private static final String KEY_THEME_AUTO = "theme_auto";
    private static final String KEY_THEME_LIGHT = "theme_light";
	private static final String KEY_THEME_DARK = "theme_dark";
	
	private RadioButtonPreference mThemeAuto;
	private RadioButtonPreference mThemeLight;
	private RadioButtonPreference mThemeDark;
	
	private int mCurrentTheme;
    private BroadcastReceiver mReceiver;
	
	private boolean mActive = false;
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Received theme changed intent: " + intent);
                }
                refreshTheme();
            }
        };
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }

    @Override
    public void onResume() {
        super.onResume();
		mActive = true;
		IntentFilter filter = new IntentFilter();
        filter.addAction(com.android.systemui.statusbar.THEME_CHANGED_ACTION);
        getActivity().registerReceiver(mReceiver, filter);
        createPreferenceHierarchy();
    }

    @Override
    public void onPause() {
		try {
            getActivity().unregisterReceiver(mReceiver);
        } catch (RuntimeException e) {
            // Ignore exceptions caused by race condition
        }
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

		mThemeAuto = (RadioButtonPreference) prefScreen.findPreference(KEY_THEME_AUTO);
        mThemeLight = (RadioButtonPreference) prefScreen.findPreference(KEY_THEME_LIGHT);
        mThemeDark = (RadioButtonPreference) prefScreen.findPreference(KEY_THEME_DARK);
		
		mThemeAuto.setOnClickListener(this);
        mThemeLight.setOnClickListener(this);
        mThemeDark.setOnClickListener(this);
		
		refreshTheme();
        return prefScreen;
    }

    private void updateRadioButtons(RadioButtonPreference pref) {
        if (pref == null) {
			mThemeAuto.setChecked(false);
            mThemeLight.setChecked(false);
            mThemeDark.setChecked(false);
        } else if (pref == mThemeAuto) {
			mThemeAuto.setChecked(true);
            mThemeLight.setChecked(false);
            mThemeDark.setChecked(false);
        } else if (pref == mThemeLight) {
            mThemeAuto.setChecked(false);
            mThemeLight.setChecked(true);
			mThemeDark.setChecked(false);
		} else if (pref == mThemeDark) {
            mThemeAuto.setChecked(false);
            mThemeLight.setChecked(false);
			mThemeDark.setChecked(true);
        }
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference pref) {
        int theme = Settings.Secure.DEVICE_THEME, 0;
        if (pref == mThemeAuto) {
            mode = Settings.Secure.DEVICE_THEME, 0;
        } else if (pref == mThemeLight) {
            mode = Settings.Secure.DEVICE_THEME, 1;
		} else if (pref == mThemeDark) {
            mode = Settings.Secure.DEVICE_THEME, 2;
        }
        setTheme(theme);
    }
	
	public static boolean updateTheme(Context context, int oldTheme, int newTheme) {
        Intent intent = new Intent(THEME_CHANGED_ACTION);
        intent.putExtra(CURRENT_THEME_KEY, oldTheme);
        intent.putExtra(NEW_THEME_KEY, newTheme);
        context.sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
        return Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.DEVICE_THEME,
                newTheme);
    }
	
	public void setTheme(int theme) {
        Context context = getActivity();
        theme = Settings.Secure.getInt(getContentResolver(), 
		        Settings.Secure.DEVICE_THEME, 0);
        if (mActive) {
            onThemeChanged(theme);
        }
		
		updateTheme(context, mCurrentTheme, theme);
		refreshTheme();
    }

    public void onThemeChanged(int theme) {
        switch (theme) {
            case Settings.Secure.DEVICE_THEME, 0;
                updateRadioButtons(mThemeAuto);
                break;
            case Settings.Secure.DEVICE_THEME, 1;
                updateRadioButtons(mThemeLight);
                break;
			case Settings.Secure.DEVICE_THEME, 2;
                updateRadioButtons(mThemeDark);
                break;
            default:
                break;
        }
    }
	
	public void refreshTheme() {
        if (mActive) {
            int theme = Settings.Secure.getInt(getContentResolver(), Settings.Secure.DEVICE_THEME, 0);
            mCurrentMode = theme;
            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, "Theme has been changed");
            }
            onThemeChanged(theme);
        }
    }
}
