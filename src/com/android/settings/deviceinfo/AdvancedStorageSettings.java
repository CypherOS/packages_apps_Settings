/*
 *  Copyright (C) 2016 Krexus
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
 *
 */
package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.mrapocalypse.screwdshop.prefs.SystemSettingSwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class AdvancedStorageSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String USB_DATA_AUTO_UNLOCK_KEY = "usb_data_auto_unlock";
    private static final String USB_CONFIGURATION_KEY = "select_usb_configuration";

    private SystemSettingSwitchPreference mUsbDataAutoUnlock;
    private ListPreference mUsbConfiguration;
    private KeyguardManager mKeyguardManager;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.SCREWD;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.storage_advanced_settings);

        mUsbDataAutoUnlock = (SystemSettingSwitchPreference) findPreference(USB_DATA_AUTO_UNLOCK_KEY);
        mUsbConfiguration = (ListPreference) findPreference(USB_CONFIGURATION_KEY);
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        boolean keyguardSecure = mKeyguardManager.isKeyguardSecure();
        mUsbDataAutoUnlock.setEnabled(!keyguardSecure);
        if (keyguardSecure) {
            mUsbDataAutoUnlock.setChecked(false);
        }
        getActivity().registerReceiver(mUsbReceiver, filter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mUsbReceiver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mUsbConfiguration) {
            writeUsbConfigurationOption(newValue);
            return true;
        }
        return false;
    }

    private void updateUsbConfigurationValues() {
        if (mUsbConfiguration != null) {
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

            String[] values = getResources().getStringArray(R.array.usb_configuration_values);
            String[] titles = getResources().getStringArray(R.array.usb_configuration_titles);
            int index = 0;
            for (int i = 0; i < titles.length; i++) {
                if (manager.isFunctionEnabled(values[i])) {
                    index = i;
                    break;
                }
            }
            mUsbConfiguration.setValue(values[index]);
            mUsbConfiguration.setSummary(titles[index]);
            mUsbConfiguration.setOnPreferenceChangeListener(this);
        }
    }

    private void writeUsbConfigurationOption(Object newValue) {
        UsbManager manager = (UsbManager)getActivity().getSystemService(Context.USB_SERVICE);
        String function = newValue.toString();
        if (function.equals("none")) {
            manager.setCurrentFunction(null);
            manager.setUsbDataUnlocked(false);
        } else {
            manager.setCurrentFunction(function);
            manager.setUsbDataUnlocked(true);
        }
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUsbConfigurationValues();
       }
    };
}
