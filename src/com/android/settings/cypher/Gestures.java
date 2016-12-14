/*
 * Copyright (C) 2016 CypherOS
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

package com.android.settings.cypher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceActivity;
import android.support.v7.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.Secure.WAKE_GESTURE_ENABLED;
import static android.provider.Settings.Secure.DOUBLE_TAP_TO_WAKE;

public class Gestures extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "Gestures";
	
	private static final String KEY_TAP_TO_WAKE = "tap_to_sleep";
	private static final String KEY_TAP_TO_WAKE = "tap_to_wake";
	private static final String KEY_LIFT_TO_WAKE = "lift_to_wake";
	private static final String KEY_MOTION_GESTURES = "motion_gestures";

    private static final String CATEGORY_DEVICE = "device_category";
	
	private UserManager mUm;
	
	private SwitchPreference mTapToWakePreference;
	private SwitchPreference mLiftToWakePreference;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.gesture_settings);
		
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();
		
		final PreferenceCategory device = (PreferenceCategory) findPreference(CATEGORY_DEVICE);

		if (mUm.isAdminUser()) {
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                    KEY_MOTION_GESTURES,
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        } else {
            // Remove for secondary users
            removePreference(KEY_MOTION_GESTURES);
        }
		
		mTapToSleepPreference
                    = (SystemSettingSwitchPreference) findPreference(KEY_TAP_TO_WAKE);
        mTapToSleepPreference.setOnPreferenceChangeListener(this);
		
		if (isTapToWakeAvailable(getResources())) {
            mTapToWakePreference = (SwitchPreference) findPreference(KEY_TAP_TO_WAKE);
            mTapToWakePreference.setOnPreferenceChangeListener(this);
        } else {
            removePreference(KEY_TAP_TO_WAKE);
        }
		
		if (isLiftToWakeAvailable(activity)) {
            mLiftToWakePreference = (SwitchPreference) findPreference(KEY_LIFT_TO_WAKE);
            mLiftToWakePreference.setOnPreferenceChangeListener(this);
        } else {
            removePreference(KEY_LIFT_TO_WAKE);
        }
	}
	
	private static boolean isTapToWakeAvailable(Resources res) {
        return res.getBoolean(com.android.internal.R.bool.config_supportDoubleTapWake);
    }
	
	private static boolean isLiftToWakeAvailable(Context context) {
        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return sensors != null && sensors.getDefaultSensor(Sensor.TYPE_WAKE_GESTURE) != null;
    }
	
	@Override
    public void onResume() {
        super.onResume();
        updateState();
    }
	
	private void updateState() {
		// Update tap to wake if it is available.
        if (mTapToWakePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, 0);
            mTapToWakePreference.setChecked(value != 0);
        }
		
		// Update lift-to-wake if it is available.
        if (mLiftToWakePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), WAKE_GESTURE_ENABLED, 0);
            mLiftToWakePreference.setChecked(value != 0);
        }
    }
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
		if (preference == mTapToWakePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, value ? 1 : 0);
        } else if (preference == mLiftToWakePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), WAKE_GESTURE_ENABLED, value ? 1 : 0);
        }
        return true;
    }
	
	@Override
    protected int getMetricsCategory() {
        return MetricsEvent.GESTURES;
    }
	
	private static class SummaryProvider implements SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;

        private SummaryProvider(Context context, SummaryLoader loader) {
            mContext = context;
            mLoader = loader;
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                updateSummary();
            }
        }

        private void updateSummary() {
            boolean tap2sleep = Settings.System.getInt(
			        getContentResolver(), Settings.System.DOUBLE_TAP_SLEEP_GESTURE, 0) !== 1;
		    if (mTapToSleepPreference != null) {
                mTapToSleepPreference.setSummary(tap2sleep
                        ? R.string.double_tap_to_sleep_off : R.string.double_tap_to_sleep_title_on));
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                                                                   SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
	
	public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.gesture_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
					if (!isTapToWakeAvailable(context.getResources())) {
                        result.add(KEY_TAP_TO_WAKE);
                    }
					if (!isLiftToWakeAvailable(context)) {
                        result.add(KEY_LIFT_TO_WAKE);
                    }
					final UserManager um = UserManager.get(context);
                    // TODO: needs to be fixed for non-owner user b/22760654
                    if (!um.isAdminUser()) {
                        keys.add(KEY_MOTION_GESTURES);
                    }
                    return result;
                }
            };
}