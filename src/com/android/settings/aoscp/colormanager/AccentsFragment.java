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

package com.android.settings.aoscp.colormanager;

import android.content.ContentResolver;
import android.content.Context;
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

public class AccentsFragment extends DashboardFragment implements RadioButtonPreference.OnClickListener {

    private static final String TAG = "ColorManager: Accents";

    private static final String KEY_ACCENT_DEFAULT     = "accent_default";
	// Category: Blue
	private static final String KEY_ACCENT_BLUE        = "accent_blue";
	private static final String KEY_ACCENT_INDIGO      = "accent_indigo";
	private static final String KEY_ACCENT_OCEANIC     = "accent_oceanic";
	private static final String KEY_ACCENT_BRIGHT_SKY  = "accent_bright_sky";
	// Category: Green
	private static final String KEY_ACCENT_GREEN       = "accent_green";
	private static final String KEY_ACCENT_LIMA_BEAN   = "accent_lima_bean";
	private static final String KEY_ACCENT_LIME        = "accent_lime";
	private static final String KEY_ACCENT_TEAL        = "accent_teal";
	// Category: Pink
	private static final String KEY_ACCENT_PINK        = "accent_pink";
	private static final String KEY_ACCENT_PLAYBOY     = "accent_play_boy";
	// Category: Purple
	private static final String KEY_ACCENT_PURPLE      = "accent_purple";
	private static final String KEY_ACCENT_DEEP_VALLEY = "accent_deep_valley";
	// Category: Red
	private static final String KEY_ACCENT_RED         = "accent_red";
	private static final String KEY_ACCENT_BLOODY_MARY = "accent_bloody_mary";
	// Category: Yellow
	private static final String KEY_ACCENT_YELLOW      = "accent_yellow";
	private static final String KEY_ACCENT_SUN_FLOWER  = "accent_sun_flower";
	// Category: Other
	private static final String KEY_ACCENT_BLACK       = "accent_black";
	private static final String KEY_ACCENT_GREY        = "accent_grey";
    private static final String KEY_ACCENT_WHITE       = "accent_white";

    List<RadioButtonPreference> mAccents = new ArrayList<>();

    private Context mContext;

    @Override
    public int getMetricsCategory() {
        return -1;
    }
    
    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.color_manager_accents_tab;
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
                RadioButtonPreference accentPref = (RadioButtonPreference) pref;
                accentPref.setOnClickListener(this);
                mAccents.add(accentPref);
            }
        }

        switch (Settings.Secure.getInt(getContentResolver(), Settings.Secure.DEVICE_ACCENT, 0)) {
            case 0:
                updateAccentItems(KEY_ACCENT_DEFAULT);
                break;
            case 1:
                updateAccentItems(KEY_ACCENT_BLUE);
                break;
            case 2:
                updateAccentItems(KEY_ACCENT_INDIGO);
                break;
            case 3:
                updateAccentItems(KEY_ACCENT_OCEANIC);
                break;
            case 4:
                updateAccentItems(KEY_ACCENT_BRIGHT_SKY);
                break;
            case 5:
                updateAccentItems(KEY_ACCENT_GREEN);
                break;
            case 6:
                updateAccentItems(KEY_ACCENT_LIMA_BEAN);
                break;
            case 7:
                updateAccentItems(KEY_ACCENT_LIME);
                break;
            case 8:
                updateAccentItems(KEY_ACCENT_TEAL);
                break;
            case 9:
                updateAccentItems(KEY_ACCENT_PINK);
                break;
			case 10:
                updateAccentItems(KEY_ACCENT_PLAYBOY);
                break;
            case 11:
                updateAccentItems(KEY_ACCENT_PURPLE);
                break;
            case 12:
                updateAccentItems(KEY_ACCENT_DEEP_VALLEY);
                break;
            case 13:
                updateAccentItems(KEY_ACCENT_RED);
                break;
            case 14:
                updateAccentItems(KEY_ACCENT_BLOODY_MARY);
                break;
            case 15:
                updateAccentItems(KEY_ACCENT_YELLOW);
                break;
            case 16:
                updateAccentItems(KEY_ACCENT_SUN_FLOWER);
                break;
            case 17:
                updateAccentItems(KEY_ACCENT_BLACK);
                break;
            case 18:
                updateAccentItems(KEY_ACCENT_GREY);
                break;
			case 19:
                updateAccentItems(KEY_ACCENT_WHITE);
                break;
        }
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        //controllers.add(new ThemePreferenceController(context));
        return controllers;
    }

    private void updateAccentItems(String selectionKey) {
        for (RadioButtonPreference pref : mAccents) {
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
            case KEY_ACCENT_DEFAULT:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 0);
                break;
            case KEY_ACCENT_BLUE:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 1);
                break;
            case KEY_ACCENT_INDIGO:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 2);
                break;
            case KEY_ACCENT_OCEANIC:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 3);
                break;
            case KEY_ACCENT_BRIGHT_SKY:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 4);
                break;
            case KEY_ACCENT_GREEN:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 5);
                break;
            case KEY_ACCENT_LIMA_BEAN:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 6);
                break;
            case KEY_ACCENT_LIME:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 7);
                break;
            case KEY_ACCENT_TEAL:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 8);
                break;
            case KEY_ACCENT_PINK:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 9);
                break;
			case KEY_ACCENT_PLAYBOY:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 10);
                break;
            case KEY_ACCENT_PURPLE:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 11);
                break;
            case KEY_ACCENT_DEEP_VALLEY:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 12);
                break;
            case KEY_ACCENT_RED:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 13);
                break;
            case KEY_ACCENT_BLOODY_MARY:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 14);
                break;
            case KEY_ACCENT_YELLOW:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 15);
                break;
            case KEY_ACCENT_SUN_FLOWER:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 16);
                break;
            case KEY_ACCENT_BLACK:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 17);
                break;
            case KEY_ACCENT_GREY:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 18);
                break;
		    case KEY_ACCENT_WHITE:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_ACCENT, 19);
                break;
        }
        updateAccentItems(pref.getKey());
    }
}