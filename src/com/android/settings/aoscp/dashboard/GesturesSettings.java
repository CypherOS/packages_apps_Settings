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
import android.content.res.Resources;
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
import com.android.settings.aoscp.gestures.TapToSleepPreferenceController;
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

    // Fingerprint Gestures
    private static final String KEY_FINGERPRINT_GESTURES = "fingerprint_gestures";
    private static final String KEY_SINGLE_TAP           = "single_tap";
    private static final String KEY_DOUBLE_TAP           = "double_tap";
    private static final String KEY_LONG_PRESS           = "long_press";
    private static final String KEY_SWIPE_UP             = "swipe_up";
    private static final String KEY_SWIPE_DOWN           = "swipe_down";
    private static final String KEY_SWIPE_LEFT           = "swipe_left";
    private static final String KEY_SWIPE_RIGHT          = "swipe_right";

    // Off-screen Gestures
    private static final String KEY_OFFSCREEN_GESTURES = "offscreen_gestures";
    private static final String KEY_DOUBLE_TAP_OFFSCREEN = "double_tap_offscreen";
    private static final String KEY_DRAW_V = "draw_v";
    private static final String KEY_DRAW_INVERSE_V = "draw_inverse_v";
    private static final String KEY_DRAW_O = "draw_o";
    private static final String KEY_DRAW_M = "draw_m";
    private static final String KEY_DRAW_W = "draw_w";
    private static final String KEY_DRAW_ARROW_LEFT = "draw_arrow_left";
    private static final String KEY_DRAW_ARROW_RIGHT = "draw_arrow_right";
    private static final String KEY_ONE_FINGER_SWIPE_UP = "one_finger_swipe_up";
    private static final String KEY_ONE_FINGER_SWIPE_RIGHT = "one_finger_swipe_right";
    private static final String KEY_ONE_FINGER_SWIPE_DOWN = "one_finger_swipe_down";
    private static final String KEY_ONE_FINGER_SWIPE_LEFT = "one_finger_swipe_left";
    private static final String KEY_TWO_FINGER_SWIPE = "two_finger_swipe";

    private static final String KEY_DOUBLE_TAP_POWER     = "double_tap_power";
    private static final String KEY_SWIPE_TO_SCREENSHOT  = "swipe_to_screenshot";
    private static final String KEY_TAP_TO_SLEEP         = "tap_to_sleep";
    private static final String KEY_TAP_TO_WAKE          = "tap_to_wake";

    // Fingerprint Gestures
    private static final HashMap<String, Integer> mFPGestureKeyCodes = new HashMap<>();
    private static final HashMap<String, Integer> mFPGestureDefaults = new HashMap();
    private static final HashMap<String, String> mFPGestureSettings = new HashMap();

    // Off-screen Gestures
    private static final HashMap<String, Integer> mGestureKeyCodes = new HashMap<>();
    private static final HashMap<String, Integer> mGestureDefaults = new HashMap();
    private static final HashMap<String, String> mGestureSettings = new HashMap();

    // Fingerprint Gestures
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

    // Off-screen Gestures
    static {
        mGestureKeyCodes.put(KEY_DOUBLE_TAP_OFFSCREEN, com.android.internal.R.integer.config_doubleTapKeyCode);
        mGestureKeyCodes.put(KEY_DRAW_V, com.android.internal.R.integer.config_drawVKeyCode);
        mGestureKeyCodes.put(KEY_DRAW_INVERSE_V, com.android.internal.R.integer.config_drawInverseVKeyCode);
        mGestureKeyCodes.put(KEY_DRAW_O, com.android.internal.R.integer.config_drawOKeyCode);
        mGestureKeyCodes.put(KEY_DRAW_M, com.android.internal.R.integer.config_drawMKeyCode);
        mGestureKeyCodes.put(KEY_DRAW_W, com.android.internal.R.integer.config_drawWKeyCode);
        mGestureKeyCodes.put(KEY_DRAW_ARROW_LEFT, com.android.internal.R.integer.config_drawArrowLeftKeyCode);
        mGestureKeyCodes.put(KEY_DRAW_ARROW_RIGHT, com.android.internal.R.integer.config_drawArrowRightKeyCode);
        mGestureKeyCodes.put(KEY_ONE_FINGER_SWIPE_UP, com.android.internal.R.integer.config_oneFingerSwipeUpKeyCode);
        mGestureKeyCodes.put(KEY_ONE_FINGER_SWIPE_RIGHT, com.android.internal.R.integer.config_oneFingerSwipeRightKeyCode);
        mGestureKeyCodes.put(KEY_ONE_FINGER_SWIPE_DOWN, com.android.internal.R.integer.config_oneFingerSwipeDownKeyCode);
        mGestureKeyCodes.put(KEY_ONE_FINGER_SWIPE_LEFT, com.android.internal.R.integer.config_oneFingerSwipeLeftKeyCode);
        mGestureKeyCodes.put(KEY_TWO_FINGER_SWIPE, com.android.internal.R.integer.config_twoFingerSwipeKeyCode);
    }

    static {
        mGestureDefaults.put(KEY_DOUBLE_TAP_OFFSCREEN, com.android.internal.R.integer.config_doubleTapDefault);
        mGestureDefaults.put(KEY_DRAW_V, com.android.internal.R.integer.config_drawVDefault);
        mGestureDefaults.put(KEY_DRAW_INVERSE_V, com.android.internal.R.integer.config_drawInverseVDefault);
        mGestureDefaults.put(KEY_DRAW_O, com.android.internal.R.integer.config_drawODefault);
        mGestureDefaults.put(KEY_DRAW_M, com.android.internal.R.integer.config_drawMDefault);
        mGestureDefaults.put(KEY_DRAW_W, com.android.internal.R.integer.config_drawWDefault);
        mGestureDefaults.put(KEY_DRAW_ARROW_LEFT, com.android.internal.R.integer.config_drawArrowLeftDefault);
        mGestureDefaults.put(KEY_DRAW_ARROW_RIGHT, com.android.internal.R.integer.config_drawArrowRightDefault);
        mGestureDefaults.put(KEY_ONE_FINGER_SWIPE_UP, com.android.internal.R.integer.config_oneFingerSwipeUpDefault);
        mGestureDefaults.put(KEY_ONE_FINGER_SWIPE_RIGHT, com.android.internal.R.integer.config_oneFingerSwipeRightDefault);
        mGestureDefaults.put(KEY_ONE_FINGER_SWIPE_DOWN, com.android.internal.R.integer.config_oneFingerSwipeDownDefault);
        mGestureDefaults.put(KEY_ONE_FINGER_SWIPE_LEFT, com.android.internal.R.integer.config_oneFingerSwipeLeftDefault);
        mGestureDefaults.put(KEY_TWO_FINGER_SWIPE, com.android.internal.R.integer.config_twoFingerSwipeDefault);
    }

    static {
        mGestureSettings.put(KEY_DOUBLE_TAP_OFFSCREEN, Settings.System.GESTURE_DOUBLE_TAP);
        mGestureSettings.put(KEY_DRAW_V, Settings.System.GESTURE_DRAW_V);
        mGestureSettings.put(KEY_DRAW_INVERSE_V, Settings.System.GESTURE_DRAW_INVERSE_V);
        mGestureSettings.put(KEY_DRAW_O, Settings.System.GESTURE_DRAW_O);
        mGestureSettings.put(KEY_DRAW_M, Settings.System.GESTURE_DRAW_M);
        mGestureSettings.put(KEY_DRAW_W, Settings.System.GESTURE_DRAW_W);
        mGestureSettings.put(KEY_DRAW_ARROW_LEFT, Settings.System.GESTURE_DRAW_ARROW_LEFT);
        mGestureSettings.put(KEY_DRAW_ARROW_RIGHT, Settings.System.GESTURE_DRAW_ARROW_RIGHT);
        mGestureSettings.put(KEY_ONE_FINGER_SWIPE_UP, Settings.System.GESTURE_ONE_FINGER_SWIPE_UP);
        mGestureSettings.put(KEY_ONE_FINGER_SWIPE_RIGHT, Settings.System.GESTURE_ONE_FINGER_SWIPE_RIGHT);
        mGestureSettings.put(KEY_ONE_FINGER_SWIPE_DOWN, Settings.System.GESTURE_ONE_FINGER_SWIPE_DOWN);
        mGestureSettings.put(KEY_ONE_FINGER_SWIPE_LEFT, Settings.System.GESTURE_ONE_FINGER_SWIPE_LEFT);
        mGestureSettings.put(KEY_TWO_FINGER_SWIPE, Settings.System.GESTURE_TWO_FINGER_SWIPE);
    }

    private GesturesEnabler mGesturesEnabler;
    private PreferenceCategory mFingerprintGestures;
    private PreferenceCategory mOffScreenGestures;

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

        mOffScreenGestures = (PreferenceCategory) findPreference(KEY_OFFSCREEN_GESTURES);
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

        for (String gestureKey : mGestureKeyCodes.keySet()) {
            if (getResources().getInteger(mGestureKeyCodes.get(gestureKey)) != 0) {
                screen.findPreference(gestureKey);
            } else {
                mOffScreenGestures.removePreference(findPreference(gestureKey));
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
    public void onDestroyView() {
        super.onDestroyView();

        if (mGesturesEnabler != null) {
            mGesturesEnabler.teardownSwitchBar();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mFPGestureKeyCodes.keySet().stream().allMatch(keyCode -> getResources().getInteger(
                mFPGestureKeyCodes.get(keyCode)) == 0)) {
            getPreferenceScreen().removePreference(mFingerprintGestures);
        }
        if (mGestureKeyCodes.keySet().stream().allMatch(keyCode -> getResources().getInteger(
                mGestureKeyCodes.get(keyCode)) == 0)) {
            getPreferenceScreen().removePreference(mOffScreenGestures);
        } else {
            SettingsActivity activity = (SettingsActivity) getActivity();
            mGesturesEnabler = new GesturesEnabler(activity.getSwitchBar());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGesturesEnabler != null) {
            mGesturesEnabler.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGesturesEnabler != null) {
            mGesturesEnabler.pause();
        }
    }

    private void enableGestures(boolean enable, boolean start) {
        for (String gestureKey : mGestureKeyCodes.keySet()) {
            if (getResources().getInteger(mGestureKeyCodes.get(gestureKey)) == 0) {
                continue;
            }
            ListPreference gesturePref = (ListPreference) findPreference(gestureKey);
            gesturePref.setOnPreferenceChangeListener(this);
            gesturePref.setEnabled(enable);
            if (start) {
                int gestureDefault = getResources().getInteger(
                        mGestureDefaults.get(gestureKey));
                int gestureBehaviour = Settings.System.getInt(getContentResolver(),
                        mGestureSettings.get(gestureKey), gestureDefault);
                gesturePref.setValue(String.valueOf(gestureBehaviour));
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Settings.System.putInt(getContentResolver(),
                mGestureSettings.get(preference.getKey()),
                Integer.parseInt((String) newValue));
        Settings.System.putInt(getContentResolver(),
                mFPGestureSettings.get(preference.getKey()),
                Integer.parseInt((String) newValue));
        return true;
    }

    public static boolean supportsGestures(Context context) {
        for (String gestureKey : mGestureKeyCodes.keySet()) {
            if (context.getResources().getInteger(mGestureKeyCodes
                    .get(gestureKey)) > 0) {
                return true;
            }
        }
        return false;
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
        controllers.add(new DoubleTapPowerPreferenceController(context, lifecycle, KEY_DOUBLE_TAP_POWER));
        controllers.add(new DoubleTwistPreferenceController(context));
        controllers.add(new SwipeToNotificationPreferenceController(context));
        controllers.add(new SwipeToScreenshotPreferenceController(context, KEY_SWIPE_TO_SCREENSHOT));
        controllers.add(new TapToSleepPreferenceController(context, KEY_TAP_TO_SLEEP));
        controllers.add(new TapToWakePreferenceController(context, KEY_TAP_TO_WAKE));
        controllers.add(new QuickTorchPreferenceController(context));
        return controllers;
    }

    private class GesturesEnabler implements SwitchBar.OnSwitchChangeListener {

        private final Context mContext;
        private final SwitchBar mSwitchBar;
        private boolean mListeningToOnSwitchChange;

        public GesturesEnabler(SwitchBar switchBar) {
            mContext = switchBar.getContext();
            mSwitchBar = switchBar;
            final Resources res = mContext.getResources();

            mSwitchBar.show();

            boolean gesturesEnabled = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.GESTURES_ENABLED, 0) != 0;
            mSwitchBar.setChecked(gesturesEnabled);
            GesturesSettings.this.enableGestures(gesturesEnabled, true);
        }

        public void teardownSwitchBar() {
            pause();
            mSwitchBar.hide();
        }

        public void resume() {
            if (!mListeningToOnSwitchChange) {
                mSwitchBar.addOnSwitchChangeListener(this);
                mListeningToOnSwitchChange = true;
            }
        }

        public void pause() {
            if (mListeningToOnSwitchChange) {
                mSwitchBar.removeOnSwitchChangeListener(this);
                mListeningToOnSwitchChange = false;
            }
        }

        @Override
        public void onSwitchChanged(Switch switchView, boolean isChecked) {
            Settings.System.putInt(
                    mContext.getContentResolver(),
                    Settings.System.GESTURES_ENABLED, isChecked ? 1 : 0);
            GesturesSettings.this.enableGestures(isChecked, false);
        }

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
                    for (String gestureKey : mGestureKeyCodes.keySet()) {
                        if (context.getResources().getInteger(mGestureKeyCodes
                                .get(gestureKey)) == 0) {
                            keys.add(gestureKey);
                        }
                    }
                    return keys;
                }
            };
}