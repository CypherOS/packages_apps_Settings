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
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settings.widget.MasterSwitchPreference;

import static android.provider.Settings.System.BUTTON_BRIGHTNESS_ENABLED;
import static android.provider.Settings.System.NAVIGATION_BAR_ENABLED;

public class ButtonBacklightPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener, LifecycleObserver, OnStart, OnStop {

    private static final String TAG = "ButtonBacklightPref";
	private static final boolean DEBUG = false;
  
    private final String mButtonBacklightKey;
	
	private final ButtonBacklightStateReceiver mButtonBacklightStateReceiver;
    private MasterSwitchPreference mButtonBacklightPref;

    private boolean mButtonBacklightSupported;

    public ButtonBacklightPreferenceController(Context context, String key, Lifecycle lifecycle) {
        super(context);
        mButtonBacklightKey = key;
		
		lifecycle.addObserver(this);
        mButtonBacklightStateReceiver = new ButtonBacklightStateReceiver();
    }

    @Override
    public boolean isAvailable() {
        /*final Resources res = mContext.getResources();
        mButtonBacklightSupported = res.getInteger(
                com.android.internal.R.bool.config_deviceHasVariableButtonBrightness);
        if (mButtonBacklightKey != null) {
            if (mButtonBacklightSupported) {
                return true;
            }
        }*/
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mButtonBacklightKey;
    }
	
	@Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        ButtonBacklightPref = (MasterSwitchPreference) screen.findPreference(mButtonBacklightKey);
    }

    @Override
    public void updateState(Preference preference) {
        int navBarEnabled = Settings.System.getInt(mContext.getContentResolver(), NAVIGATION_BAR_ENABLED, 0);
        ButtonBacklightPref.setEnabled(navBarEnabled != 0);
		ButtonBacklightPref.setChecked(isButtonBacklightEnabled());
        updateSummary();
    }
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (Boolean) newValue;
        if (enabled != isButtonBacklightEnabled()
                && !setButtonBacklightEnabled(enabled)) {
            return false;
        }
        updateSummary();
        return true;
    }
	
	@Override
    public void onStart() {
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.BUTTON_BRIGHTNESS_ENABLED)
                , true, mObserver);

        mButtonBacklightStateReceiver.setListening(true);
    }

    @Override
    public void onStop() {
        mContext.getContentResolver().unregisterContentObserver(mObserver);
        mButtonBacklightStateReceiver.setListening(false);
    }
	
	private void updateSummary() {
        final boolean enabled = isButtonBacklightEnabled();
        final int format = enabled ? R.string.button_backlight_title_summary_on
                : R.string.button_backlight_title_summary_off;

        final String summary = mContext.getString(format);
        mButtonBacklightPref.setSummary(summary);
    }

	public boolean isButtonBacklightEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(), BUTTON_BRIGHTNESS_ENABLED, isAvailable() ? 1 : 0) != 0;
    }

    public boolean setButtonBacklightEnabled(boolean enabled) {
        return Settings.System.putInt(mContext.getContentResolver(), BUTTON_BRIGHTNESS_ENABLED, enabled ? 1 : 0);
    }

    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateSummary();
        }
    };
	
	private final class ButtonBacklightStateReceiver extends BroadcastReceiver {
        private boolean mRegistered;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) {
                Log.d(TAG, "Received: Button backlight state");
            }
			if (isAvailable()) {
				mButtonBacklightPref.setChecked(isButtonBacklightEnabled());
				updateSummary();
			}
        }

        public void setListening(boolean listening) {
            if (listening && !mRegistered) {
                final IntentFilter intentFilter = new IntentFilter();
                // Todo: add a real button backlight intent action
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
