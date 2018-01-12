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
package com.android.settings.aoscp.network;

import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settings.widget.MasterSwitchPreference;
import com.android.settings.widget.SummaryUpdater;

import static android.provider.Settings.System.NETWORK_TRAFFIC_STATE;

public class NetworkTrafficPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener, SummaryUpdater.OnSummaryChangeListener,
        OnResume, OnPause, OnStart, OnStop {

    private static final String TAG = "NetworkTrafficPref";

    public static final String KEY_TRAFFIC_MONITOR = "traffic_monitor";
	
    private MasterSwitchPreference mTrafficMonitorPref;
	private NetworkTrafficMonitoring mTrafficMonitorSettings;
	private final NetworkTrafficSummaryUpdater mSummaryUpdater;

    public NetworkTrafficPreferenceController(Context context) {
        super(context);
        mSummaryUpdater = new NetworkTrafficSummaryUpdater(mContext, this);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_TRAFFIC_MONITOR;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mTrafficMonitorPref = (MasterSwitchPreference) screen.findPreference(KEY_TRAFFIC_MONITOR);
    }

    @Override
    public void updateState(Preference preference) {
        mTrafficMonitorPref.setChecked(isTrafficMonitorEnabled());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (Boolean) newValue;
        if (enabled != isTrafficMonitorEnabled()
                && !setTrafficMonitorEnabled(enabled)) {
            return false;
        }
        return true;
    }
	
	@Override
    public void onResume() {
        mSummaryUpdater.register(true);
        if (mTrafficMonitorSettings != null) {
            mTrafficMonitorSettings.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mTrafficMonitorSettings != null) {
            mTrafficMonitorSettings.onPause();
        }
        mSummaryUpdater.register(false);
    }

    @Override
    public void onStart() {
        mSummaryUpdater.register(true);
    }

    @Override
    public void onStop() {
        mSummaryUpdater.register(false);
    }
	
	@Override
    public void onSummaryChanged(String summary) {
        if (mTrafficMonitorPref != null) {
            mTrafficMonitorPref.setSummary(summary);
        }
    }

    public boolean isTrafficMonitorEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(), NETWORK_TRAFFIC_STATE, 1) != 0;
    }

    public boolean setTrafficMonitorEnabled(boolean enabled) {
        return Settings.System.putInt(mContext.getContentResolver(), NETWORK_TRAFFIC_STATE, enabled ? 1 : 0);
    }
}
