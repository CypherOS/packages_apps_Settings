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

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.aoscp.FooterConfirm;
import com.android.settingslib.aoscp.FooterConfirm.onActionClickListener;
import com.android.settingslib.aoscp.FooterConfirmMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static android.provider.Settings.System.BATTERY_LIGHT_ENABLED;

public class BatteryLightSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

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

    private static final int MENU_RESET = Menu.FIRST;

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
            setHasOptionsMenu(true);
            mLowColorPref = (BatteryLightPreference) screen.findPreference(KEY_LOW_COLOR);
            mLowColorPref.setOnPreferenceChangeListener(this);

            mMediumColorPref = (BatteryLightPreference) screen.findPreference(KEY_MEDIUM_COLOR);
            mMediumColorPref.setOnPreferenceChangeListener(this);

            mFullColorPref = (BatteryLightPreference) screen.findPreference(KEY_FULL_COLOR);
            mFullColorPref.setOnPreferenceChangeListener(this);
        } else {
            screen.removePreference(screen.findPreference(KEY_CATEGORY_CHARGE_COLORS));
        }
        boolean showOnlyWhenFull = Settings.System.getInt(getContentResolver(),
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
        disableLight();
    }

    /**
     * Updates the default battery color settings.
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
        disableLight();
    }

    private void enableLight() {
        Settings.System.putInt(getContentResolver(), BATTERY_LIGHT_ENABLED, 1);
    }

    private void disableLight() {
        Settings.System.putInt(getContentResolver(), BATTERY_LIGHT_ENABLED, 0);
        enableLight();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
            .setIcon(R.drawable.ic_settings_backup_restore)
            .setAlphabeticShortcut('r')
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetColors();
                return true;
        }
        return false;
    }

    protected void resetColors() {
        ContentResolver resolver = getActivity().getContentResolver();
        Resources res = getResources();
        FooterConfirmMixin.show(FooterConfirm.with(getContext())
            .setMessage("Colors will return to default")
            .setDuration(-1)
            .setAction(true)
            .setActionTitle("Reset")
            .setActionListener(new onActionClickListener() {
                @Override
                public void onActionClicked(FooterConfirm footerConfirm) {
                      Settings.System.putInt(resolver, Settings.System.BATTERY_LIGHT_LOW_COLOR,
                                  res.getInteger(com.android.internal.R.integer.config_notificationsBatteryLowARGB));
                      Settings.System.putInt(resolver, Settings.System.BATTERY_LIGHT_MEDIUM_COLOR,
                                  res.getInteger(com.android.internal.R.integer.config_notificationsBatteryMediumARGB));
                      Settings.System.putInt(resolver, Settings.System.BATTERY_LIGHT_FULL_COLOR,
                                  res.getInteger(com.android.internal.R.integer.config_notificationsBatteryFullARGB));
                }
            }));
        refreshDefault();
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