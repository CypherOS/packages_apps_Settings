/*
 * Copyright (C) 2017 CypherOS
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

package com.android.settings.aoscp.dashboard;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.widget.Switch;

import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.aoscp.gestures.DoubleTapPowerPreferenceController;
import com.android.settings.aoscp.gestures.DoubleTwistPreferenceController;
import com.android.settings.aoscp.gestures.QuickTorchPreferenceController;
import com.android.settings.aoscp.gestures.SwipeToNotificationPreferenceController;
import com.android.settings.aoscp.gestures.SwipeToScreenshotPreferenceController;
import com.android.settings.aoscp.gestures.TapToWakePreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.SwitchBar;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class GesturesSettings extends DashboardFragment implements 
        OnPreferenceChangeListener, Indexable {

    private static final String LOG_TAG = "GesturesSettings";

    // Off-screen Gestures
    private static final String KEY_FINGERPRINT_GESTURES = "fingerprint_gestures";
    private static final String KEY_SINGLE_TAP           = "single_tap";
    private static final String KEY_DOUBLE_TAP           = "double_tap";
    private static final String KEY_LONG_PRESS           = "long_press";
    private static final String KEY_SWIPE_UP             = "swipe_up";
    private static final String KEY_SWIPE_DOWN           = "swipe_down";
    private static final String KEY_SWIPE_LEFT           = "swipe_left";
    private static final String KEY_SWIPE_RIGHT          = "swipe_right";
	
	private static final String KEY_SWIPE_TO_SCREENSHOT  = "swipe_to_screenshot";
	private static final String KEY_TAP_TO_WAKE          = "tap_to_wake";

    private static final HashMap<String, Integer> mFPGestureKeyCodes = new HashMap<>();
    private static final HashMap<String, Integer> mFPGestureDefaults = new HashMap();
    private static final HashMap<String, String> mFPGestureSettings = new HashMap();

    static {
        mFPGestureKeyCodes.put(KEY_SINGLE_TAP, com.android.internal.R.integer.config_fpSingleTapKeyCode);
        mFPGestureKeyCodes.put(KEY_DOUBLE_TAP, com.android.internal.R.integer.config_fpDoubleTapKeyCode);
        mFPGestureKeyCodes.put(KEY_LONG_PRESS, com.android.internal.R.integer.config_fpLongpressKeyCode);
        mFPGestureKeyCodes.put(KEY_SWIPE_UP, com.android.internal.R.integer.config_fpSwipeUpKeyCode);
        mFPGestureKeyCodes.put(KEY_SWIPE_DOWN, com.android.internal.R.integer.config_fpSwipeDownKeyCode);
        mFPGestureKeyCodes.put(KEY_SWIPE_LEFT, com.android.internal.R.integer.config_fpSwipeLeftKeyCode);
        mFPGestureKeyCodes.put(KEY_SWIPE_RIGHT, com.android.internal.R.integer.config_fpSwipeRightKeyCode);
    }

    static {
        mFPGestureDefaults.put(KEY_SINGLE_TAP, com.android.internal.R.integer.config_fpSingleTapDefault);
        mFPGestureDefaults.put(KEY_DOUBLE_TAP, com.android.internal.R.integer.config_fpDoubleTapDefault);
        mFPGestureDefaults.put(KEY_LONG_PRESS, com.android.internal.R.integer.config_fpLongpressDefault);
        mFPGestureDefaults.put(KEY_SWIPE_UP, com.android.internal.R.integer.config_fpSwipeUpDefault);
        mFPGestureDefaults.put(KEY_SWIPE_DOWN, com.android.internal.R.integer.config_fpSwipeDownDefault);
        mFPGestureDefaults.put(KEY_SWIPE_LEFT, com.android.internal.R.integer.config_fpSwipeLeftDefault);
        mFPGestureDefaults.put(KEY_SWIPE_RIGHT, com.android.internal.R.integer.config_fpSwipeRightDefault);
    }

    static {
        mFPGestureSettings.put(KEY_SINGLE_TAP, Settings.System.FINGERPRINT_GESTURES_SINGLE_TAP);
        mFPGestureSettings.put(KEY_DOUBLE_TAP, Settings.System.FINGERPRINT_GESTURES_DOUBLE_TAP);
        mFPGestureSettings.put(KEY_LONG_PRESS, Settings.System.FINGERPRINT_GESTURES_LONGPRESS);
        mFPGestureSettings.put(KEY_SWIPE_UP, Settings.System.FINGERPRINT_GESTURES_SWIPE_UP);
        mFPGestureSettings.put(KEY_SWIPE_DOWN, Settings.System.FINGERPRINT_GESTURES_SWIPE_DOWN);
        mFPGestureSettings.put(KEY_SWIPE_LEFT, Settings.System.FINGERPRINT_GESTURES_SWIPE_LEFT);
        mFPGestureSettings.put(KEY_SWIPE_RIGHT, Settings.System.FINGERPRINT_GESTURES_SWIPE_RIGHT);
    }

    private PreferenceCategory mFingerprintGestures;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.gesture_settings;
    }

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

        mFingerprintGestures = (PreferenceCategory) findPreference(KEY_FINGERPRINT_GESTURES);
        boolean navBarEnabled = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.NAVIGATION_BAR_ENABLED, 0) != 0;
        for (String fpGestureKey : mFPGestureKeyCodes.keySet()) {
            if (getResources().getInteger(mFPGestureKeyCodes.get(fpGestureKey)) != 0) {
                ListPreference fpGesturePref = (ListPreference) screen.findPreference(fpGestureKey);
                fpGesturePref.setOnPreferenceChangeListener(this);
                int fpGestureDefault = getResources().getInteger(
                        mFPGestureDefaults.get(fpGestureKey));
                int fpGestureBehaviour = Settings.System.getInt(getContentResolver(),
                        mFPGestureSettings.get(fpGestureKey), fpGestureDefault);
                fpGesturePref.setValue(String.valueOf(fpGestureBehaviour));
                fpGesturePref.setEnabled(!navBarEnabled);
            } else {
                mFingerprintGestures.removePreference(findPreference(fpGestureKey));
            }
        }
        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.gesture_settings_summary);
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_about;
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mFPGestureKeyCodes.keySet().stream().allMatch(keyCode -> getResources().getInteger(
                mFPGestureKeyCodes.get(keyCode)) == 0)) {
            getPreferenceScreen().removePreference(mFingerprintGestures);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Settings.System.putInt(getContentResolver(),
                mFPGestureSettings.get(preference.getKey()),
                Integer.parseInt((String) newValue));
        return true;
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getActivity(), this /* fragment */,
                getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Activity activity, Fragment fragment, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        AmbientDisplayConfiguration ambientDisplayConfig = new AmbientDisplayConfiguration(context);
        controllers.add(new DoubleTapPowerPreferenceController(context));
        controllers.add(new DoubleTwistPreferenceController(context));
        controllers.add(new SwipeToNotificationPreferenceController(context));
		controllers.add(new SwipeToScreenshotPreferenceController(context, KEY_SWIPE_TO_SCREENSHOT));
        controllers.add(new TapToWakePreferenceController(context, KEY_TAP_TO_WAKE));
        controllers.add(new QuickTorchPreferenceController(context));
        return controllers;
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.gesture_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<AbstractPreferenceController> getPreferenceControllers(Context context) {
                    return buildPreferenceControllers(context, null /*activity */,
                            null /* fragment */, null /* lifecycle */);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    for (String fpGestureKey : mFPGestureKeyCodes.keySet()) {
                        if (context.getResources().getInteger(mFPGestureKeyCodes
                                .get(fpGestureKey)) == 0) {
                            keys.add(fpGestureKey);
                        }
                    }
                    return keys;
                }
            };
}