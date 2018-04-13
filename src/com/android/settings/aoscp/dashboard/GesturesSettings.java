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
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
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
    private static final String KEY_DOUBLE_TAP           = "double_tap";
    private static final String KEY_LONG_PRESS           = "long_press";
    private static final String KEY_SWIPE_UP             = "swipe_up";
    private static final String KEY_SWIPE_DOWN           = "swipe_down";
    private static final String KEY_SWIPE_LEFT           = "swipe_left";
    private static final String KEY_SWIPE_RIGHT          = "swipe_right";

    private static final HashMap<String, Integer> mFPGestureKeyCodes = new HashMap<>();
    private static final HashMap<String, Integer> mFPGestureDefaults = new HashMap();
    private static final HashMap<String, String> mFPGestureSettings = new HashMap();

    static {
        mFPGestureKeyCodes.put(KEY_DOUBLE_TAP, com.android.internal.R.integer.config_fpDoubleTapKeyCode);
        mFPGestureKeyCodes.put(KEY_LONG_PRESS, com.android.internal.R.integer.config_fpLongpressKeyCode);
        mFPGestureKeyCodes.put(KEY_SWIPE_UP, com.android.internal.R.integer.config_fpSwipeUpKeyCode);
        mFPGestureKeyCodes.put(KEY_SWIPE_DOWN, com.android.internal.R.integer.config_fpSwipeDownKeyCode);
        mFPGestureKeyCodes.put(KEY_SWIPE_LEFT, com.android.internal.R.integer.config_fpSwipeLeftKeyCode);
        mFPGestureKeyCodes.put(KEY_SWIPE_RIGHT, com.android.internal.R.integer.config_fpSwipeRightKeyCode);
    }

    static {
        mFPGestureDefaults.put(KEY_DOUBLE_TAP, com.android.internal.R.integer.config_fpDoubleTapDefault);
        mFPGestureDefaults.put(KEY_LONG_PRESS, com.android.internal.R.integer.config_fpLongpressDefault);
        mFPGestureDefaults.put(KEY_SWIPE_UP, com.android.internal.R.integer.config_fpSwipeUpDefault);
        mFPGestureDefaults.put(KEY_SWIPE_DOWN, com.android.internal.R.integer.config_fpSwipeDownDefault);
        mFPGestureDefaults.put(KEY_SWIPE_LEFT, com.android.internal.R.integer.config_fpSwipeLeftDefault);
        mFPGestureDefaults.put(KEY_SWIPE_RIGHT, com.android.internal.R.integer.config_fpSwipeRightDefault);
    }

    static {
        mFPGestureSettings.put(KEY_DOUBLE_TAP, Settings.System.FINGERPRINT_GESTURES_DOUBLE_TAP);
        mFPGestureSettings.put(KEY_LONG_PRESS, Settings.System.FINGERPRINT_GESTURES_LONGPRESS);
        mFPGestureSettings.put(KEY_SWIPE_UP, Settings.System.FINGERPRINT_GESTURES_SWIPE_UP);
        mFPGestureSettings.put(KEY_SWIPE_DOWN, Settings.System.FINGERPRINT_GESTURES_SWIPE_DOWN);
        mFPGestureSettings.put(KEY_SWIPE_LEFT, Settings.System.FINGERPRINT_GESTURES_SWIPE_LEFT);
        mFPGestureSettings.put(KEY_SWIPE_RIGHT, Settings.System.FINGERPRINT_GESTURES_SWIPE_RIGHT);
    }

    private GesturesEnabler mGesturesEnabler;
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

        for (String gestureKey : mFPGestureKeyCodes.keySet()) {
            if (getResources().getInteger(mFPGestureKeyCodes.get(gestureKey)) != 0) {
                screen.findPreference(gestureKey);
            } else {
                mFingerprintGestures.removePreference(findPreference(gestureKey));
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
        for (String gestureKey : mFPGestureKeyCodes.keySet()) {
            if (getResources().getInteger(mFPGestureKeyCodes.get(gestureKey)) == 0) {
                continue;
            }
            ListPreference gesturePref = (ListPreference) findPreference(gestureKey);
            gesturePref.setOnPreferenceChangeListener(this);
            gesturePref.setEnabled(enable);
            if (start) {
                int gestureDefault = getResources().getInteger(
                        mFPGestureDefaults.get(gestureKey));
                int gestureBehaviour = Settings.System.getInt(getContentResolver(),
                        mFPGestureSettings.get(gestureKey), gestureDefault);
                gesturePref.setValue(String.valueOf(gestureBehaviour));
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Settings.System.putInt(getContentResolver(),
                mFPGestureSettings.get(preference.getKey()),
                Integer.parseInt((String) newValue));
        return true;
    }

    public static boolean supportsGestures(Context context) {
        for (String gestureKey : mFPGestureKeyCodes.keySet()) {
            if (context.getResources().getInteger(mFPGestureKeyCodes
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
        return controllers;
    }

    private class GesturesEnabler implements SwitchBar.OnSwitchChangeListener {

        private final Context mContext;
        private final SwitchBar mSwitchBar;
        private boolean mListeningToOnSwitchChange;

        public GesturesEnabler(SwitchBar switchBar) {
            mContext = switchBar.getContext();
            mSwitchBar = switchBar;

            mSwitchBar.show();

            boolean gesturesEnabled = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.FINGERPRINT_GESTURES_ENABLED, 0) != 0;
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
                    Settings.System.FINGERPRINT_GESTURES_ENABLED, isChecked ? 1 : 0);
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
                    for (String gestureKey : mFPGestureKeyCodes.keySet()) {
                        if (context.getResources().getInteger(mFPGestureKeyCodes
                                .get(gestureKey)) == 0) {
                            keys.add(gestureKey);
                        }
                    }
                    return keys;
                }
            };
}