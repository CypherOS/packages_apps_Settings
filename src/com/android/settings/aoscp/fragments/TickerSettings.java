/*
 * Copyright (C) 2016 The Pure Nexus Project
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

package com.android.settings.aoscp.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.preference.BaseSystemSettingSwitchBar;

public class TickerSettings extends SettingsPreferenceFragment
        implements BaseSystemSettingSwitchBar.SwitchBarChangeCallback,
                Preference.OnPreferenceChangeListener {

    private BaseSystemSettingSwitchBar mEnabledSwitch;

    private ViewGroup mPrefsContainer;
    private View mDisabledText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get launch-able applications
        addPreferencesFromResource(R.xml.ticker_settings);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ticker_fragment, container, false);
        mPrefsContainer = (ViewGroup) v.findViewById(R.id.prefs_container);
        mDisabledText = v.findViewById(R.id.disabled_text);

        View prefs = super.onCreateView(inflater, mPrefsContainer, savedInstanceState);
        mPrefsContainer.addView(prefs);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        final SettingsActivity activity = (SettingsActivity) getActivity();
        mEnabledSwitch = new BaseSystemSettingSwitchBar(activity, activity.getSwitchBar(),
                Settings.System.STATUS_BAR_SHOW_TICKER, true, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        final SettingsActivity activity = (SettingsActivity) getActivity();
        if (mEnabledSwitch != null) {
            mEnabledSwitch.resume(activity);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mEnabledSwitch != null) {
            mEnabledSwitch.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mEnabledSwitch != null) {
            mEnabledSwitch.teardownSwitchBar();
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ADDITIONS;
    }

    private boolean getTickerState() {
        return Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_SHOW_TICKER,
               UserHandle.USER_CURRENT) != 0;
    }

    private void setTickerState(int val) {
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_TICKER,
                val, UserHandle.USER_CURRENT);
    }

    private void updateEnabledState() {
        mPrefsContainer.setVisibility(getTickerState() ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(getTickerState() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEnablerChanged(boolean isEnabled) {
        setTickerState(getTickerState() ? 1 : 0);
        updateEnabledState();
    }
}