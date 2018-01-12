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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import android.util.Log;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.dashboard.conditional.TrafficMonitorCondition;
import com.android.settings.dashboard.conditional.ConditionManager;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settings.widget.MasterSwitchPreference;

import static android.provider.Settings.System.NETWORK_TRAFFIC_STATE;

public class NetworkTrafficPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener, OnStart, OnStop {

    private static final String TAG = "NetworkTrafficPref";
    private static final boolean DEBUG = false;

    private static final String KEY_TRAFFIC_MONITOR = "traffic_monitor";

    private final TrafficMonitorStateReceiver mTrafficMonitorStateReceiver;
    private MasterSwitchPreference mTrafficMonitorPref;

    public NetworkTrafficPreferenceController(Context context) {
        super(context);
        mTrafficMonitorStateReceiver = new TrafficMonitorStateReceiver();
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
        updateSummary();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (Boolean) newValue;
        if (enabled != isTrafficMonitorEnabled()
                && !setTrafficMonitorEnabled(enabled)) {
            return false;
        }
		refreshCondition();
        updateSummary();
        return true;
    }

    @Override
    public void onStart() {
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.NETWORK_TRAFFIC_STATE)
                , true, mObserver);

        mTrafficMonitorStateReceiver.setListening(true);
    }

    @Override
    public void onStop() {
        mContext.getContentResolver().unregisterContentObserver(mObserver);
        mTrafficMonitorStateReceiver.setListening(false);
    }
		
    private void refreshCondition() {
        ConditionManager.get(mContext).getCondition(TrafficMonitorCondition.class).refreshState();
    }

    private void updateSummary() {
        final boolean enabled = isTrafficMonitorEnabled();
        final int format = enabled ? R.string.network_traffic_summary_on
                : R.string.network_traffic_summary_off;

        final String summary = mContext.getString(format);
        mTrafficMonitorPref.setSummary(summary);
    }

    public boolean isTrafficMonitorEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(), NETWORK_TRAFFIC_STATE, 1) != 0;
    }

    public boolean setTrafficMonitorEnabled(boolean enabled) {
        return Settings.System.putInt(mContext.getContentResolver(), NETWORK_TRAFFIC_STATE, enabled ? 1 : 0);
    }

    private final ContentObserver mObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            updateSummary();
        }
    };

    private final class TrafficMonitorStateReceiver extends BroadcastReceiver {
        private boolean mRegistered;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) {
                Log.d(TAG, "Received: Network traffic state");
            }
            if (isAvailable()) {
                mTrafficMonitorPref.setChecked(isTrafficMonitorEnabled());
                updateSummary();
            }
        }

        public void setListening(boolean listening) {
            if (listening && !mRegistered) {
                final IntentFilter intentFilter = new IntentFilter();
                // Todo: add a real traffic monitor intent action
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                mContext.registerReceiver(this, intentFilter);
                mRegistered = true;
            } else if (!listening && mRegistered) {
                mContext.unregisterReceiver(this);
                mRegistered = false;
            }
        }
    }
}
