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

import android.content.ContentResolver;
import android.content.Context;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;
import static android.provider.Settings.Secure.WAKE_GESTURE_ENABLED;
import static android.provider.Settings.Secure.DOUBLE_TAP_TO_WAKE;

public class Gestures extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "Gestures";
	
	private static final String KEY_TAP_TO_WAKE = "tap_to_wake";
	private static final String KEY_LIFT_TO_WAKE = "lift_to_wake";
	private static final String TAG = "GestureSettings";
    private static final String PREF_KEY_DOUBLE_TAP_POWER = "gesture_double_tap_power";
    private static final String PREF_KEY_DOUBLE_TWIST = "gesture_double_twist";
    private static final String PREF_KEY_SWIPE_DOWN_FINGERPRINT = "gesture_swipe_down_fingerprint";
    private static final int PREF_ID_DOUBLE_TAP_POWER = 0;
    private static final int PREF_ID_DOUBLE_TWIST = 1;
    private static final int PREF_ID_SWIPE_DOWN_FINGERPRINT = 2;
			
	private SwitchPreference mTapToWakePreference;
	private SwitchPreference mLiftToWakePreference;
	private List<GesturePreference> mPreferences;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.gesture_settings);
		Context context = getActivity();
        mPreferences = new ArrayList();
		
        final ContentResolver resolver = activity.getContentResolver();
		
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
		
		// Double tap power for camera
        if (isCameraDoubleTapPowerGestureAvailable(getResources())) {
            int cameraDisabled = Secure.getInt(
                    getContentResolver(), Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0);
            addPreference(PREF_KEY_DOUBLE_TAP_POWER, cameraDisabled == 0, PREF_ID_DOUBLE_TAP_POWER);
        } else {
            removePreference(PREF_KEY_DOUBLE_TAP_POWER);
        }

        // Fingerprint slide for notifications
        if (isSystemUINavigationAvailable(context)) {
            addPreference(PREF_KEY_SWIPE_DOWN_FINGERPRINT, isSystemUINavigationEnabled(context),
                    PREF_ID_SWIPE_DOWN_FINGERPRINT);
        } else {
            removePreference(PREF_KEY_SWIPE_DOWN_FINGERPRINT);
        }

        // Double twist for camera mode
        if (isDoubleTwistAvailable(context)) {
            int doubleTwistEnabled = Secure.getInt(
                    getContentResolver(), Secure.CAMERA_DOUBLE_TWIST_TO_FLIP_ENABLED, 1);
            addPreference(PREF_KEY_DOUBLE_TWIST, doubleTwistEnabled != 0, PREF_ID_DOUBLE_TWIST);
        } else {
            removePreference(PREF_KEY_DOUBLE_TWIST);
        }
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        RecyclerView listview = getListView();
        listview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    for (GesturePreference pref : mPreferences) {
                        pref.setScrolling(true);
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    for (GesturePreference pref : mPreferences) {
                        pref.setScrolling(false);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        for (GesturePreference preference : mPreferences) {
            preference.onViewVisible();
        }
    }
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
		if (preference == mTapToWakePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, value ? 1 : 0);
        }
		if (preference == mLiftToWakePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), WAKE_GESTURE_ENABLED, value ? 1 : 0);
        }
		if (PREF_KEY_DOUBLE_TAP_POWER.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, value ? 0 : 1);
        } else if (PREF_KEY_SWIPE_DOWN_FINGERPRINT.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.SYSTEM_NAVIGATION_KEYS_ENABLED, value ? 1 : 0);
        } else if (PREF_KEY_DOUBLE_TWIST.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.CAMERA_DOUBLE_TWIST_TO_FLIP_ENABLED, value ? 1 : 0);
        }
        return true;
    }
	
	@Override
    protected int getHelpResource() {
        return R.string.help_url_gestures;
    }
	
	@Override
    protected int getMetricsCategory() {
        return MetricsEvent.GESTURES;
    }
	
	private static boolean isTapToWakeAvailable(Resources res) {
        return res.getBoolean(com.android.internal.R.bool.config_supportDoubleTapWake);
    }
	
	private static boolean isLiftToWakeAvailable(Context context) {
        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return sensors != null && sensors.getDefaultSensor(Sensor.TYPE_WAKE_GESTURE) != null;
    }
	
	private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
        return res.getBoolean(
                com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
    }

    private static boolean isSystemUINavigationAvailable(Context context) {
        return context.getResources().getBoolean(
                com.android.internal.R.bool.config_supportSystemNavigationKeys);
    }

    private static boolean isSystemUINavigationEnabled(Context context) {
        return Secure.getInt(context.getContentResolver(), Secure.SYSTEM_NAVIGATION_KEYS_ENABLED, 0)
                == 1;
    }

    private static boolean isDoubleTwistAvailable(Context context) {
        Resources resources = context.getResources();
        String name = resources.getString(R.string.gesture_double_twist_sensor_name);
        String vendor = resources.getString(R.string.gesture_double_twist_sensor_vendor);
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

    private void addPreference(String key, boolean enabled, int id) {
        GesturePreference preference = (GesturePreference) findPreference(key);
        preference.setChecked(enabled);
        preference.setOnPreferenceChangeListener(this);
        preference.loadPreview(getLoaderManager(), id);
        mPreferences.add(preference);
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
			boolean camgest = Settings.Secure.getInt(mContext.getContentResolver(),
                    CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 1) == 0;
            mLoader.setSummary(this, mContext.getString(camgest ? R.string.camera_double_tap_power_gesture_on
                    : R.string.camera_double_tap_power_gesture_off));
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
					if (!isCameraDoubleTapPowerGestureAvailable(context.getResources())) {
                        result.add(PREF_KEY_DOUBLE_TAP_POWER);
                    }
                    if (!isSystemUINavigationAvailable(context)) {
                        result.add(PREF_KEY_SWIPE_DOWN_FINGERPRINT);
                    }
                    if (!isDoubleTwistAvailable(context)) {
                        result.add(PREF_KEY_DOUBLE_TWIST);
                    }
                    return result;
                }
            };
}