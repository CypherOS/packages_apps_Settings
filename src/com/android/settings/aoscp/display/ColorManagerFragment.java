/*
 * Copyright (C) 2017-2018 CypherOS
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

    private Context mContext;

    // Base Themes
    private RadioButtonPreference mThemeAuto;
    private RadioButtonPreference mThemeLight;
    private RadioButtonPreference mThemeDark;
    private RadioButtonPreference mThemeBlack;

    // Theme Accents
    private RadioButtonPreference mAccentDefault;
    private RadioButtonPreference mAccentDeepPurple;
    private RadioButtonPreference mAccentIndigo;
    private RadioButtonPreference mAccentPink;
    private RadioButtonPreference mAccentPurple;
    private RadioButtonPreference mAccentRed;
    private RadioButtonPreference mAccentSkyBlue;
    private RadioButtonPreference mAccentTeal;
    private RadioButtonPreference mAccentWhite;
    private RadioButtonPreference mAccentYellow;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }
    
    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mProgressiveDisclosureMixin.setTileLimit(4);
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
        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.color_manager_description);

        // Search for themes
        mThemeAuto = (RadioButtonPreference) screen.findPreference(KEY_THEME_AUTO);
        mThemeLight = (RadioButtonPreference) screen.findPreference(KEY_THEME_LIGHT);
        mThemeDark = (RadioButtonPreference) screen.findPreference(KEY_THEME_DARK);
        mThemeBlack = (RadioButtonPreference) screen.findPreference(KEY_THEME_BLACK);
        // Set theme click listeners
        mThemeAuto.setOnClickListener(this);
        mThemeLight.setOnClickListener(this);
        mThemeDark.setOnClickListener(this);
        mThemeBlack.setOnClickListener(this);
        
        // Search for accents
        mAccentDefault = (RadioButtonPreference) screen.findPreference(KEY_ACCENT_DEFAULT);
        mAccentDeepPurple = (RadioButtonPreference) screen.findPreference(KEY_ACCENT_DEEP_PURPLE);
        mAccentIndigo = (RadioButtonPreference) screen.findPreference(KEY_ACCENT_INDIGO);
        mAccentPink = (RadioButtonPreference) screen.findPreference(KEY_ACCENT_PINK);
        mAccentPurple = (RadioButtonPreference) screen.findPreference(KEY_ACCENT_PURPLE);
        mAccentRed = (RadioButtonPreference) screen.findPreference(KEY_ACCENT_RED);
        mAccentSkyBlue = (RadioButtonPreference) screen.findPreference(KEY_ACCENT_SKY_BLUE);
        mAccentTeal = (RadioButtonPreference) screen.findPreference(KEY_ACCENT_TEAL);
        mAccentWhite = (RadioButtonPreference) screen.findPreference(KEY_ACCENT_WHITE);
        mAccentYellow = (RadioButtonPreference) screen.findPreference(KEY_ACCENT_YELLOW);
        // Set accent click listeners
        mAccentDefault.setOnClickListener(this);
        mAccentDeepPurple.setOnClickListener(this);
        mAccentIndigo.setOnClickListener(this);
        mAccentPink.setOnClickListener(this);
        mAccentPurple.setOnClickListener(this);
        mAccentRed.setOnClickListener(this);
        mAccentSkyBlue.setOnClickListener(this);
        mAccentTeal.setOnClickListener(this);
        mAccentWhite.setOnClickListener(this);
        mAccentYellow.setOnClickListener(this);

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
                updateAccentItems(KEY_ACCENT_DEFAULT);
                break;
            case 1:
                updateAccentItems(KEY_ACCENT_DEEP_PURPLE);
                break;
            case 2:
                updateAccentItems(KEY_ACCENT_INDIGO);
                break;
            case 3:
                updateAccentItems(KEY_ACCENT_PINK);
                break;
            case 4:
                updateAccentItems(KEY_ACCENT_PURPLE);
                break;
            case 5:
                updateAccentItems(KEY_ACCENT_RED);
                break;
            case 6:
                updateAccentItems(KEY_ACCENT_SKY_BLUE);
                break;
            case 7:
                updateAccentItems(KEY_ACCENT_TEAL);
                break;
            case 8:
                updateAccentItems(KEY_ACCENT_WHITE);
                break;
            case 9:
                updateAccentItems(KEY_ACCENT_YELLOW);
                break;
        }
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new ThemePreferenceController(context));
        return controllers;
    }

    private void updateThemeItems(String themeKey) {
        if (themeKey == null) {
            mThemeAuto.setChecked(false);
            mThemeLight.setChecked(false);
            mThemeDark.setChecked(false);
            mThemeBlack.setChecked(false);
        } else if (themeKey == KEY_THEME_AUTO) {
            mThemeAuto.setChecked(true);
            mThemeLight.setChecked(false);
            mThemeDark.setChecked(false);
            mThemeBlack.setChecked(false);
        } else if (themeKey == KEY_THEME_LIGHT) {
            mThemeAuto.setChecked(false);
            mThemeLight.setChecked(true);
            mThemeDark.setChecked(false);
            mThemeBlack.setChecked(false);
        } else if (themeKey == KEY_THEME_DARK) {
            mThemeAuto.setChecked(false);
            mThemeLight.setChecked(false);
            mThemeDark.setChecked(true);
            mThemeBlack.setChecked(false);
        } else if (themeKey == KEY_THEME_BLACK) {
            mThemeAuto.setChecked(false);
            mThemeLight.setChecked(false);
            mThemeDark.setChecked(false);
            mThemeBlack.setChecked(true);
        }
    }

    private void updateAccentItems(String accentKey) {
        if (accentKey == null) {
            mAccentDefault.setChecked(false);
            mAccentDeepPurple.setChecked(false);
            mAccentIndigo.setChecked(false);
            mAccentPink.setChecked(false);
            mAccentPurple.setChecked(false);
            mAccentRed.setChecked(false);
            mAccentSkyBlue.setChecked(false);
            mAccentTeal.setChecked(false);
            mAccentWhite.setChecked(false);
            mAccentYellow.setChecked(false);
        } else if (accentKey == KEY_ACCENT_DEFAULT) {
            mAccentDefault.setChecked(true);
            mAccentDeepPurple.setChecked(false);
            mAccentIndigo.setChecked(false);
            mAccentPink.setChecked(false);
            mAccentPurple.setChecked(false);
            mAccentRed.setChecked(false);
            mAccentSkyBlue.setChecked(false);
            mAccentTeal.setChecked(false);
            mAccentWhite.setChecked(false);
            mAccentYellow.setChecked(false);
        } else if (accentKey == KEY_ACCENT_DEEP_PURPLE) {
            mAccentDefault.setChecked(false);
            mAccentDeepPurple.setChecked(true);
            mAccentIndigo.setChecked(false);
            mAccentPink.setChecked(false);
            mAccentPurple.setChecked(false);
            mAccentRed.setChecked(false);
            mAccentSkyBlue.setChecked(false);
            mAccentTeal.setChecked(false);
            mAccentWhite.setChecked(false);
            mAccentYellow.setChecked(false);
        } else if (accentKey == KEY_ACCENT_INDIGO) {
            mAccentDefault.setChecked(false);
            mAccentDeepPurple.setChecked(false);
            mAccentIndigo.setChecked(true);
            mAccentPink.setChecked(false);
            mAccentPurple.setChecked(false);
            mAccentRed.setChecked(false);
            mAccentSkyBlue.setChecked(false);
            mAccentTeal.setChecked(false);
            mAccentWhite.setChecked(false);
            mAccentYellow.setChecked(false);
        } else if (accentKey == KEY_ACCENT_PINK) {
            mAccentDefault.setChecked(false);
            mAccentDeepPurple.setChecked(false);
            mAccentIndigo.setChecked(false);
            mAccentPink.setChecked(true);
            mAccentPurple.setChecked(false);
            mAccentRed.setChecked(false);
            mAccentSkyBlue.setChecked(false);
            mAccentTeal.setChecked(false);
            mAccentWhite.setChecked(false);
            mAccentYellow.setChecked(false);
        } else if (accentKey == KEY_ACCENT_PURPLE) {
            mAccentDefault.setChecked(false);
            mAccentDeepPurple.setChecked(false);
            mAccentIndigo.setChecked(false);
            mAccentPink.setChecked(false);
            mAccentPurple.setChecked(true);
            mAccentRed.setChecked(false);
            mAccentSkyBlue.setChecked(false);
            mAccentTeal.setChecked(false);
            mAccentWhite.setChecked(false);
            mAccentYellow.setChecked(false);
        } else if (accentKey == KEY_ACCENT_RED) {
            mAccentDefault.setChecked(false);
            mAccentDeepPurple.setChecked(false);
            mAccentIndigo.setChecked(false);
            mAccentPink.setChecked(false);
            mAccentPurple.setChecked(false);
            mAccentRed.setChecked(true);
            mAccentSkyBlue.setChecked(false);
            mAccentTeal.setChecked(false);
            mAccentWhite.setChecked(false);
            mAccentYellow.setChecked(false);
        } else if (accentKey == KEY_ACCENT_SKY_BLUE) {
            mAccentDefault.setChecked(false);
            mAccentDeepPurple.setChecked(false);
            mAccentIndigo.setChecked(false);
            mAccentPink.setChecked(false);
            mAccentPurple.setChecked(false);
            mAccentRed.setChecked(false);
            mAccentSkyBlue.setChecked(true);
            mAccentTeal.setChecked(false);
            mAccentWhite.setChecked(false);
            mAccentYellow.setChecked(false);
        } else if (accentKey == KEY_ACCENT_TEAL) {
            mAccentDefault.setChecked(false);
            mAccentDeepPurple.setChecked(false);
            mAccentIndigo.setChecked(false);
            mAccentPink.setChecked(false);
            mAccentPurple.setChecked(false);
            mAccentRed.setChecked(false);
            mAccentSkyBlue.setChecked(false);
            mAccentTeal.setChecked(true);
            mAccentWhite.setChecked(false);
            mAccentYellow.setChecked(false);
        } else if (accentKey == KEY_ACCENT_WHITE) {
            mAccentDefault.setChecked(false);
            mAccentDeepPurple.setChecked(false);
            mAccentIndigo.setChecked(false);
            mAccentPink.setChecked(false);
            mAccentPurple.setChecked(false);
            mAccentRed.setChecked(false);
            mAccentSkyBlue.setChecked(false);
            mAccentTeal.setChecked(false);
            mAccentWhite.setChecked(true);
            mAccentYellow.setChecked(false);
        } else if (accentKey == KEY_ACCENT_YELLOW) {
            mAccentDefault.setChecked(false);
            mAccentDeepPurple.setChecked(false);
            mAccentIndigo.setChecked(false);
            mAccentPink.setChecked(false);
            mAccentPurple.setChecked(false);
            mAccentRed.setChecked(false);
            mAccentSkyBlue.setChecked(false);
            mAccentTeal.setChecked(false);
            mAccentWhite.setChecked(false);
            mAccentYellow.setChecked(true);
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
        }
        
        switch (pref.getKey()) {
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
        updateAccentItems(pref.getKey());
    }
}
