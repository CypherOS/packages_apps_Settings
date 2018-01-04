/*
 * Copyright (C) 2017 CypherOS
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
package com.android.settings.aoscp.buttons;

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

public class ButtonBacklightSettings extends SettingsPreferenceFragment implements
        SwitchBar.OnSwitchChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "ButtonBacklightSettings";
	
	private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
	
	private static final String KEY_LIT_ON_TOUCH = "lit_on_touch";
	private static final String KEY_BUTTON_TIMEOUT = "button_timeout";
	
	private static final long WAIT_FOR_SWITCH_ANIM = 500;
    private final Handler mHandler = new Handler();
	
	private SystemSettingSwitchPreference mLitOnTouchPref;
	private SeekBarPreference mTimeout;
	
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
        addPreferencesFromResource(R.xml.button_backlight_settings);
        mContext = getActivity();
        mSwitchBar = ((SettingsActivity) mContext).getSwitchBar();
        mSwitch = mSwitchBar.getSwitch();
        mSwitchBar.show();

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getContentResolver();
        mButtonBacklightEnabled = getResources().getBoolean(
                com.android.internal.R.bool.config_deviceHasVariableButtonBrightness);

        mLitOnTouchPref = (SystemSettingSwitchPreference)prefScreen.findPreference(KEY_LIT_ON_TOUCH);
        mLitOnTouchPref.setChecked(Settings.System.getInt(resolver,
                        Settings.System.BUTTON_BACKLIGHT_ON_TOUCH_ONLY, mButtonBacklightEnabled ? 1 : 0) != 0);
        mLitOnTouchPref.setOnPreferenceChangeListener(this);
		
		mTimeout = (SeekBarPreference) findPreference(KEY_BUTTON_TIMEOUT);
        int currentTimeout = Settings.System.getInt(resolver,
                        Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 0);
        mTimeout.setValue(currentTimeout);
        mTimeout.setOnPreferenceChangeListener(this);
		
		updateDependencies();
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
		updateDependencies();
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
        mHandler.removeCallbacks(mEnableButtonBacklight);
        if (isChecked) {
            mHandler.postDelayed(mEnableButtonBacklight, WAIT_FOR_SWITCH_ANIM);
        } else {
            if (DEBUG) Log.d(TAG, "Disabling button light from settings");
            tryEnableButtonBacklight(false);
        }
    }

    private void tryEnableButtonBacklight(boolean enabled) {
        if (!setButtonBacklightEnabled(enabled)) {
            if (DEBUG) Log.d(TAG, "Setting enabled failed, fallback to current value");
            mHandler.post(mUpdateSwitch);
        }
    }

    private void updateSwitch() {
        final boolean enabled = isButtonBacklightEnabled();
        if (DEBUG) Log.d(TAG, "updateSwitch: isChecked=" + mSwitch.isChecked() + " enabled=" + enabled);
        if (enabled == mSwitch.isChecked()) return;

        // set listener to null so that that code below doesn't trigger onCheckedChanged()
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

    private final Runnable mEnableButtonBacklight = new Runnable() {
        @Override
        public void run() {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "Enabling button backlight");
                    tryEnableButtonBacklight(true);
                }
            });
        }
    };
	
	public boolean isButtonBacklightEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(), BUTTON_BRIGHTNESS_ENABLED, mButtonBacklightEnabled ? 1 : 0) != 0;
    }
  
    public boolean setButtonBacklightEnabled(boolean enabled) {
        return Settings.System.putInt(mContext.getContentResolver(), BUTTON_BRIGHTNESS_ENABLED, enabled ? 1 : 0);
    }
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mLitOnTouchPref) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_ON_TOUCH_ONLY, value ? 1 : 0);
        } else if (preference == mTimeout) {
            int timeout = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, timeout);
        return true;
    }
	
	private void updateDependencies() {
        if (mLitOnTouchPref != null) {
            mLitOnTouchPref.setEnabled(isButtonBacklightEnabled());
        }
		if (mTimeout != null) {
            mTimeout.setEnabled(isButtonBacklightEnabled());
        }
    }
}
