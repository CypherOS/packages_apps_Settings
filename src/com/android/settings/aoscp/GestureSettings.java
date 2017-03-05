/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.aoscp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.System.DOUBLE_TAP_SLEEP_GESTURE;
import static android.provider.Settings.Secure.DOUBLE_TAP_TO_WAKE;

/**
 * Top level fragment for gesture settings.
 * This will create individual switch preference for each gesture and handle updates when each
 * preference is updated
 */
public class GestureSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "GestureSettings";
	
	private static final String PREF_KEY_MOVES_CATEGORY = "moves_category";
    private static final String PREF_KEY_DOUBLE_TAP_POWER = "gesture_double_tap_power";
    private static final String PREF_KEY_DOUBLE_TWIST = "gesture_double_twist";
    private static final String PREF_KEY_PICK_UP = "gesture_pick_up";
    private static final String PREF_KEY_SWIPE_DOWN_FINGERPRINT = "gesture_swipe_down_fingerprint";
	private static final String PREF_KEY_TAP_TO_WAKE = "tap_to_wake";
    private static final String PREF_KEY_DOUBLE_TAP_SCREEN = "gesture_double_tap_screen";
    private static final String DEBUG_DOZE_COMPONENT = "debug.doze.component";
	
	private static final String PREF_KEY_OPO_DOZE_SETTINGS = "opo_doze_settings";

    private AmbientDisplayConfiguration mAmbientConfig;

	private SwitchPreference mTapToWakePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gesture_settings);
        Context context = getActivity();
		
		final PreferenceCategory moves = (PreferenceCategory) findPreference(PREF_KEY_MOVES_CATEGORY);

        // Double tap power for camera
        if (isCameraDoubleTapPowerGestureAvailable(getResources())) {
            int cameraDisabled = Secure.getInt(
                    getContentResolver(), Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0);
            addPreference(PREF_KEY_DOUBLE_TAP_POWER, cameraDisabled == 0);
        } else {
            moves.removePreference(findPreference(PREF_KEY_DOUBLE_TAP_POWER));
        }

        // Ambient Display
        mAmbientConfig = new AmbientDisplayConfiguration(context);
        if (mAmbientConfig.pulseOnPickupAvailable()) {
            boolean pickup = mAmbientConfig.pulseOnPickupEnabled(UserHandle.myUserId());
			addPreference(PREF_KEY_PICK_UP, pickup);
        } else {
			moves.removePreference(findPreference(PREF_KEY_PICK_UP));
        }
		
        if (mAmbientConfig.pulseOnDoubleTapAvailable()) {
            boolean doubleTap = mAmbientConfig.pulseOnDoubleTapEnabled(UserHandle.myUserId());
			addPreference(PREF_KEY_DOUBLE_TAP_SCREEN, doubleTap);
        } else {
			moves.removePreference(findPreference(PREF_KEY_DOUBLE_TAP_SCREEN));
        }

        // Fingerprint slide for notifications
        if (isSystemUINavigationAvailable(context)) {
			addPreference(PREF_KEY_SWIPE_DOWN_FINGERPRINT, isSystemUINavigationEnabled(context));
        } else {
			moves.removePreference(findPreference(PREF_KEY_SWIPE_DOWN_FINGERPRINT));
        }
		
		// Tap to wake
		if (isTapToWakeAvailable(getResources())) {
            mTapToWakePreference = (SwitchPreference) findPreference(PREF_KEY_TAP_TO_WAKE);
            mTapToWakePreference.setOnPreferenceChangeListener(this);
        } else {
            removePreference(PREF_KEY_TAP_TO_WAKE);
        }

        // Double twist for camera mode
        if (isDoubleTwistAvailable(context)) {
            int doubleTwistEnabled = Secure.getInt(
                    getContentResolver(), Secure.CAMERA_DOUBLE_TWIST_TO_FLIP_ENABLED, 1);
            addPreference(PREF_KEY_DOUBLE_TWIST, doubleTwistEnabled != 0);
        } else {
			moves.removePreference(findPreference(PREF_KEY_DOUBLE_TWIST));
        }
		
		final Activity act = getActivity();
		
		Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                PREF_KEY_OPO_DOZE_SETTINGS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

    }
	
    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (boolean) newValue;
        String key = preference.getKey();
        if (PREF_KEY_DOUBLE_TAP_POWER.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, enabled ? 0 : 1);
        } else if (PREF_KEY_PICK_UP.equals(key)) {
            Secure.putInt(getContentResolver(), Secure.DOZE_PULSE_ON_PICK_UP, enabled ? 1 : 0);
        } else if (PREF_KEY_DOUBLE_TAP_SCREEN.equals(key)) {
            Secure.putInt(getContentResolver(), Secure.DOZE_PULSE_ON_DOUBLE_TAP, enabled ? 1 : 0);
        } else if (PREF_KEY_SWIPE_DOWN_FINGERPRINT.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.SYSTEM_NAVIGATION_KEYS_ENABLED, enabled ? 1 : 0);
		} else if (preference == mTapToWakePreference) {
            Secure.putInt(getContentResolver(), 
			        Secure.DOUBLE_TAP_TO_WAKE, enabled ? 1 : 0);
        } else if (PREF_KEY_DOUBLE_TWIST.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.CAMERA_DOUBLE_TWIST_TO_FLIP_ENABLED, enabled ? 1 : 0);
        }
        return true;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.SETTINGS_GESTURES;
    }

    private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
        return res.getBoolean(
                com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
    }

    private static boolean isSystemUINavigationAvailable(Context context) {
        return context.getResources().getBoolean(
                com.android.internal.R.bool.config_supportSystemNavigationKeys);
    }
	
	private static boolean isTapToWakeAvailable(Resources res) {
        return res.getBoolean(com.android.internal.R.bool.config_supportDoubleTapWake);
    }

    private static boolean isSystemUINavigationEnabled(Context context) {
        return Secure.getInt(context.getContentResolver(), Secure.SYSTEM_NAVIGATION_KEYS_ENABLED, 0)
                == 1;
    }

    private static boolean isDoubleTwistAvailable(Context context) {
        return hasSensor(context, R.string.gesture_double_twist_sensor_name,
                R.string.gesture_double_twist_sensor_vendor);
    }

    private static boolean hasSensor(Context context, int nameResId, int vendorResId) {
        Resources resources = context.getResources();
        String name = resources.getString(nameResId);
        String vendor = resources.getString(vendorResId);
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(vendor)) {
            SensorManager sensorManager =
                    (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            for (Sensor s : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
                if (name.equals(s.getName()) && vendor.equals(s.getVendor())) {
                    return true;
                }
            }
        }
        return false;
    }
	
	private void updateState() {

        // Update tap to wake if it is available.
        if (mTapToWakePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, 0);
            mTapToWakePreference.setChecked(value != 0);
        }
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
            boolean tap = Settings.System.getInt(mContext.getContentResolver(),
                    DOUBLE_TAP_SLEEP_GESTURE, 1) == 0;
            mLoader.setSummary(this, mContext.getString(tap ? R.string.double_tap_to_sleep_off
                    : R.string.double_tap_to_sleep_on));
        }
    }
	
	private void addPreference(String key, boolean enabled) {
        SwitchPreference preference = (SwitchPreference) findPreference(key);
        preference.setChecked(enabled);
        preference.setOnPreferenceChangeListener(this);
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
                AmbientDisplayConfiguration ambientConfig
                        = new AmbientDisplayConfiguration(context);
                if (!isCameraDoubleTapPowerGestureAvailable(context.getResources())) {
                    result.add(PREF_KEY_DOUBLE_TAP_POWER);
                }
                if (!ambientConfig.pulseOnPickupAvailable()) {
                    result.add(PREF_KEY_PICK_UP);
                }
                if (!ambientConfig.pulseOnDoubleTapAvailable()) {
                    result.add(PREF_KEY_DOUBLE_TAP_SCREEN);
                }
                if (!isSystemUINavigationAvailable(context)) {
                    result.add(PREF_KEY_SWIPE_DOWN_FINGERPRINT);
                }
				if (!isTapToWakeAvailable(context.getResources())) {
                    result.add(PREF_KEY_TAP_TO_WAKE);
                    }
                if (!isDoubleTwistAvailable(context)) {
                    result.add(PREF_KEY_DOUBLE_TWIST);
                }
                return result;
            }
        };

}
