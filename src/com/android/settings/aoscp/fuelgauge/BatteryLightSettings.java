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

package com.android.settings.aoscp.fuelgauge;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BatteryLightSettings extends DashboardFragment {

    private static final String TAG = "BatteryLight";
    private static final String KEY_BATTERY_LIGHT_SUMMARY         = "battery_light_summary";
	private static final String KEY_BATTERY_LIGHT_PULSE           = "battery_light_pulse";
	private static final String KEY_BATTERY_LIGHT_PULSE_CHARGED   = "battery_light_only_fully_charged";
	
	private static final String KEY_CATEGORY_CHARGE_COLORS        = "colors_list";
	private static final String KEY_LOW_COLOR                     = "low_color";
    private static final String KEY_MEDIUM_COLOR                  = "medium_color";
    private static final String KEY_FULL_COLOR                    = "full_color";
	
    private BatteryLightPreference mLowColorPref;
    private BatteryLightPreference mMediumColorPref;
    private BatteryLightPreference mFullColorPref;
	
	private boolean mMultiColorLed;
	
	@Override
    public void displayResourceTiles() {
        final int resId = getPreferenceScreenResId();
        if (resId <= 0) {
            return;
        }
        addPreferencesFromResource(resId);
        final PreferenceScreen screen = getPreferenceScreen();
        Collection<AbstractPreferenceController> controllers = mPreferenceControllers.values();
        for (AbstractPreferenceController controller : controllers) {
            controller.displayPreference(screen);
        }

        mMultiColorLed = getResources().getBoolean(com.android.internal.R.bool.config_multiColorBatteryLed);
        if (mMultiColorLed) {
            mLowColorPref = (BatteryLightPreference) screen.findPreference(KEY_LOW_COLOR);
            mLowColorPref.setOnPreferenceChangeListener(this);

            mMediumColorPref = (BatteryLightPreference) screen.findPreference(KEY_MEDIUM_COLOR);
            mMediumColorPref.setOnPreferenceChangeListener(this);

            mFullColorPref = (BatteryLightPreference) screen.findPreference(KEY_FULL_COLOR);
            mFullColorPref.setOnPreferenceChangeListener(this);
        } else {
			screen.removePreference(screen.findPreference(KEY_CATEGORY_CHARGE_COLORS));
		}
		boolean showOnlyWhenFull = Settings.System.getInt(resolver,
                Settings.System.BATTERY_LIGHT_ONLY_FULLY_CHARGED, 0) != 0;
        updateEnablement(showOnlyWhenFull);
    }
	
	@Override
    public void onResume() {
        super.onResume();
        refreshDefault();
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.power_usage_summary_battery;
    }
	
	private void refreshDefault() {
        ContentResolver resolver = getContentResolver();
        Resources res = getResources();

        if (mLowColorPref != null) {
            int lowColor = Settings.System.getInt(resolver, Settings.System.BATTERY_LIGHT_LOW_COLOR,
                    res.getInteger(com.android.internal.R.integer.config_notificationsBatteryLowARGB));
            mLowColorPref.setColor(lowColor);
        }

        if (mMediumColorPref != null) {
            int mediumColor = Settings.System.getInt(resolver, Settings.System.BATTERY_LIGHT_MEDIUM_COLOR,
                    res.getInteger(com.android.internal.R.integer.config_notificationsBatteryMediumARGB));
            mMediumColorPref.setColor(mediumColor);
        }

        if (mFullColorPref != null) {
            int fullColor = Settings.System.getInt(resolver, Settings.System.BATTERY_LIGHT_FULL_COLOR,
                    res.getInteger(com.android.internal.R.integer.config_notificationsBatteryFullARGB));
            mFullColorPref.setColor(fullColor);
        }
    }
	
	/**
     * Updates the default or application specific battery color settings.
     *
     * @param key of the specific setting to update
     * @param color
     */
    protected void updateValues(String key, Integer color) {
        ContentResolver resolver = getContentResolver();
		
        if (key.equals(KEY_LOW_COLOR)) {
            Settings.System.putInt(resolver, Settings.System.BATTERY_LIGHT_LOW_COLOR, color);
        } else if (key.equals(KEY_MEDIUM_COLOR)) {
            Settings.System.putInt(resolver, Settings.System.BATTERY_LIGHT_MEDIUM_COLOR, color);
        } else if (key.equals(KEY_FULL_COLOR)) {
            Settings.System.putInt(resolver, Settings.System.BATTERY_LIGHT_FULL_COLOR, color);
        }
    }
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        BatteryLightPreference lightPref = (BatteryLightPreference) preference;
        updateValues(lightPref.getKey(), lightPref.getColor());
        return true;
    }

	private void updateEnablement(boolean showOnlyWhenFull) {
        if (mLowColorPref != null) {
            mLowColorPref.setEnabled(!showOnlyWhenFull);
        }
        if (mMediumColorPref != null) {
            mMediumColorPref.setEnabled(!showOnlyWhenFull);
        }
        if (mFullColorPref != null) {
            mFullColorPref.setEnabled(!showOnlyWhenFull);
        }
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new BatteryLightPreferenceController(context, KEY_BATTERY_LIGHT_SUMMARY));
		controllers.add(new BatteryLightPulseLowPreferenceController(context, KEY_BATTERY_LIGHT_PULSE));
		controllers.add(new BatteryLightPulseChargedPreferenceController(context, KEY_BATTERY_LIGHT_PULSE_CHARGED));
        return controllers;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.power_usage_summary_battery;
                    return Arrays.asList(sir);
                }

                @Override
                public List<AbstractPreferenceController> getPreferenceControllers(Context context) {
                    return buildPreferenceControllers(context, null /* lifecycle */);
                }
            };
}