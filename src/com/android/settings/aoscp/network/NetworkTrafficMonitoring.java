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

package com.android.settings.aoscp.network;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Switch;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.preference.SeekBarPreference;
import com.android.settings.preference.SystemSettingSwitchPreference;
import com.android.settings.widget.SwitchBar;

public class NetworkTrafficMonitoring extends SettingsPreferenceFragment implements 
        SwitchBar.OnSwitchChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "NetworkTrafficMonitoring";

    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private static final String KEY_AUTOHIDE_THRESHOLD = "autohide_threshold";

    private static final long WAIT_FOR_SWITCH_ANIM = 500;
    private final Handler mHandler = new Handler();
    
    private SeekBarPreference mAutohideThreshold;

    private boolean mCreated;
    private boolean mValidListener;
    private Context mContext;
    private SwitchBar mSwitchBar;
    private Switch mSwitch;
    
    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCreated) {
            mSwitchBar.show();
            return;
        }
        mCreated = true;
        addPreferencesFromResource(R.xml.network_traffic_monitor);
        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.network_traffic_description);
        mContext = getActivity();
        mSwitchBar = ((SettingsActivity) mContext).getSwitchBar();
        mSwitch = mSwitchBar.getSwitch();
        mSwitchBar.show();

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getContentResolver();

        mAutohideThreshold = (SeekBarPreference) findPreference(KEY_AUTOHIDE_THRESHOLD);
        int currentThreshold = Settings.System.getInt(resolver,
                        Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 0);
        mAutohideThreshold.setValue(currentThreshold);
        mAutohideThreshold.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSwitchBar.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mValidListener) {
            mSwitchBar.addOnSwitchChangeListener(this);
            mValidListener = true;
        }
        updateSwitch();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mValidListener) {
            mSwitchBar.removeOnSwitchChangeListener(this);
            mValidListener = false;
        }
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        mHandler.removeCallbacks(mEnableTrafficMonitor);
        if (isChecked) {
            mHandler.postDelayed(mEnableTrafficMonitor, WAIT_FOR_SWITCH_ANIM);
        } else {
            if (DEBUG) Log.d(TAG, "Disabling network traffic monitor from settings");
            tryEnableTrafficMonitor(false);
        }
    }

    private void tryEnableTrafficMonitor(boolean enabled) {
        if (!setTrafficMonitorEnabled(enabled)) {
            if (DEBUG) Log.d(TAG, "Setting enabled failed, fallback to current value");
            mHandler.post(mUpdateSwitch);
        }
    }

    private void updateSwitch() {
        final boolean enabled = isTrafficMonitorEnabled();
        if (DEBUG) Log.d(TAG, "updateSwitch: isChecked=" + mSwitch.isChecked() + " enabled=" + enabled);
        if (enabled == mSwitch.isChecked()) return;

        if (mValidListener) {
            mSwitchBar.removeOnSwitchChangeListener(this);
        }
        mSwitch.setChecked(enabled);
        if (mValidListener) {
            mSwitchBar.addOnSwitchChangeListener(this);
        }
    }

    private final Runnable mUpdateSwitch = new Runnable() {
        @Override
        public void run() {
            updateSwitch();
        }
    };

    private final Runnable mEnableTrafficMonitor = new Runnable() {
        @Override
        public void run() {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "Enabling traffic monitoring");
                    tryEnableTrafficMonitor(true);
                }
            });
        }
    };

    public boolean isTrafficMonitorEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, 1) != 0;
    }
  
    public boolean setTrafficMonitorEnabled(boolean enabled) {
        return Settings.System.putInt(mContext.getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, enabled ? 1 : 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mAutohideThreshold) {
            int threshold = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, threshold);
        }
        return true;
    }
}
