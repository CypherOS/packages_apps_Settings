/*
 * Copyright (C) 2017 CypherOS
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.OverlayInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.ArrayMap;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.ThemePreferenceController;
import com.android.settings.widget.RadioButtonPreference;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ColorManagerFragment extends DashboardFragment
        implements RadioButtonPreference.OnClickListener {

    private static final String TAG = "ColorManagerSettings";

	// Base Themes
    private static final String KEY_THEME_AUTO         = "theme_auto";
    private static final String KEY_THEME_LIGHT        = "theme_light";
    private static final String KEY_THEME_DARK         = "theme_dark";
    private static final String KEY_THEME_BLACK        = "theme_black";
	
	// Theme Accents
	private static final String KEY_ACCENT_DEFAULT     = "accent_default";
	private static final String KEY_ACCENT_DEEP_PURPLE = "accent_deep_purple";
	private static final String KEY_ACCENT_INDIGO      = "accent_indigo";
	private static final String KEY_ACCENT_PINK        = "accent_pink";
	private static final String KEY_ACCENT_PURPLE      = "accent_purple";
	private static final String KEY_ACCENT_RED         = "accent_red";
	private static final String KEY_ACCENT_SKY_BLUE    = "accent_sky_blue";
	private static final String KEY_ACCENT_TEAL        = "accent_teal";
	private static final String KEY_ACCENT_WHITE       = "accent_white";
	private static final String KEY_ACCENT_YELLOW      = "accent_yellow";
	
	// Theme Accent Packages
	private static final String ACCENT_DEFAULT = "default";
	private static final String ACCENT_DEEP_PURPLE = "co.aoscp.accent.deeppurple";
	private static final String ACCENT_INDIGO = "co.aoscp.accent.indigo";
	private static final String ACCENT_PINK = "co.aoscp.accent.pink";
	private static final String ACCENT_PURPLE = "co.aoscp.accent.purple";
	private static final String ACCENT_RED = "co.aoscp.accent.red";
	private static final String ACCENT_SKY_BLUE = "co.aoscp.accent.skyblue";
	private static final String ACCENT_TEAL = "co.aoscp.accent.teal";
	private static final String ACCENT_WHITE = "co.aoscp.accent.white";
	private static final String ACCENT_YELLOW = "co.aoscp.accent.yellow";

    List<RadioButtonPreference> mThemes = new ArrayList<>();

    private Context mContext;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }
    
    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.color_manager_settings;
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    @Override
    public void displayResourceTiles() {
        final int resId = getPreferenceScreenResId();
        if (resId <= 0) {
            return;
        }
        addPreferencesFromResource(resId);
        final PreferenceScreen screen = getPreferenceScreen();
        Collection<AbstractPreferenceController> controllers = mPreferenceControllers.values();
        for (AbstractPreferenceController controller : controllers) {
            controller.displayPreference(screen);
        }

        for (int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference pref = screen.getPreference(i);
            if (pref instanceof RadioButtonPreference) {
                RadioButtonPreference themePref = (RadioButtonPreference) pref;
                themePref.setOnClickListener(this);
                mThemes.add(themePref);
            }
        }

        switch (Settings.Secure.getInt(getContentResolver(), Settings.Secure.DEVICE_THEME, 0)) {
            case 0:
                updateThemeItems(KEY_THEME_AUTO);
                break;
            case 1:
                updateThemeItems(KEY_THEME_LIGHT);
                break;
            case 2:
                updateThemeItems(KEY_THEME_DARK);
                break;
            case 3:
                updateThemeItems(KEY_THEME_BLACK);
                break;
        }
		
		switch (Settings.Secure.getInt(getContentResolver(), Settings.Secure.DEVICE_ACCENT, 0)) {
			case 0:
                updateThemeItems(KEY_ACCENT_DEFAULT);
                break;
			case 1:
                updateThemeItems(KEY_ACCENT_DEEP_PURPLE);
                break;
			case 2:
                updateThemeItems(KEY_ACCENT_INDIGO);
                break;
			case 3:
                updateThemeItems(KEY_ACCENT_PINK);
                break;
			case 4:
                updateThemeItems(KEY_ACCENT_PURPLE);
                break;
			case 5:
                updateThemeItems(KEY_ACCENT_RED);
                break;
			case 6:
                updateThemeItems(KEY_ACCENT_SKY_BLUE);
                break;
			case 7:
                updateThemeItems(KEY_ACCENT_TEAL);
                break;
			case 8:
                updateThemeItems(KEY_ACCENT_WHITE);
                break;
			case 9:
                updateThemeItems(KEY_ACCENT_YELLOW);
                break;
		}
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        // controllers.add(new ThemePreferenceController(context));
        return controllers;
    }

    private void updateThemeItems(String selectionKey) {
        for (RadioButtonPreference pref : mThemes) {
            if (selectionKey.equals(pref.getKey())) {
                pref.setChecked(true);
            } else {
                pref.setChecked(false);
            }
        }
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference pref) {
        switch (pref.getKey()) {
            case KEY_THEME_AUTO:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_THEME, 0);
                break;
            case KEY_THEME_LIGHT:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_THEME, 1);
                break;
            case KEY_THEME_DARK:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_THEME, 2);
                break;
            case KEY_THEME_BLACK:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_THEME, 3);
                break;
			case KEY_ACCENT_DEFAULT:
			    Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 0);
                break;
			case KEY_ACCENT_DEEP_PURPLE:
			    Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 1);
                break;
			case KEY_ACCENT_INDIGO:
			    Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 2);
                break;
			case KEY_ACCENT_PINK:
			    Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 3);
                break;
			case KEY_ACCENT_PURPLE:
			    Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 4);
                break;
			case KEY_ACCENT_RED:
			    Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 5);
                break;
			case KEY_ACCENT_SKY_BLUE:
			    Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 6);
                break;
			case KEY_ACCENT_TEAL:
			    Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 7);
                break;
			case KEY_ACCENT_WHITE:
			    Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 8);
                break;
			case KEY_ACCENT_YELLOW:
			    Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 9);
                break;
        }
        updateThemeItems(pref.getKey());
    }
}
