/*
 *  Copyright (C) 2017 CypherOS
 *  Copyright (C) 2014 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.settings.aoscp.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.view.View;
import android.util.Log;
import android.app.AlertDialog;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.settings.aoscp.preference.DynamicSeekBarPreference;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ButtonBrightnessSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "ButtonBrightnessSettings";

    private static final String KEY_BUTTON_BACKLIGHT_BRIGHTNESS = "button_brightness";
    private static final String KEY_BUTTON_BACKLIGHT_TIMEOUT = "button_timeout";

    private DynamicSeekBarPreference mButtonBrightness;
	private DynamicSeekBarPreference mButtonBacklightTimeout;
    private IPowerManager mPowerService;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ADDITIONS;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.button_brightness_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getContentResolver();
		
        mButtonBrightness = (DynamicSeekBarPreference) findPreference(KEY_BUTTON_BACKLIGHT_BRIGHTNESS);
        final int customButtonBrightness = getResources().getInteger(
                com.android.internal.R.integer.config_buttonBrightnessSettingDefault);
        final int currentBrightness = Settings.System.getInt(resolver,
                Settings.System.BUTTON_BRIGHTNESS, customButtonBrightness);
        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        mButtonBrightness.setMaxValue(pm.getDefaultButtonBrightness());
        mButtonBrightness.setValue(currentBrightness);
        mButtonBrightness.setOnPreferenceChangeListener(this);

        mButtonBacklightTimeout = (DynamicSeekBarPreference) findPreference(KEY_BUTTON_BACKLIGHT_TIMEOUT);
        int currentTimeout = Settings.System.getInt(resolver,
                        Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 0);
        mButtonBacklightTimeout.setValue(currentTimeout);
        mButtonBacklightTimeout.setOnPreferenceChangeListener(this);

        mPowerService = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();

        if (preference == mButtonBacklightTimeout) {
            int buttonTimeout = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, buttonTimeout);
        } else if (preference == mButtonBrightness) {
            int buttonBrightness = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BUTTON_BRIGHTNESS, buttonBrightness);
        } else {
            return false;
        }
        return true;
    }
}
