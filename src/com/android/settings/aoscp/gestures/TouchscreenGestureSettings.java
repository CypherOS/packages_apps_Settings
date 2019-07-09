/*
 * Copyright (C) 2019 CypherOS
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

package com.android.settings.aoscp.gestures;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;

import aoscp.hardware.DeviceHardwareManager;
import aoscp.hardware.TouchscreenGesture;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.aoscp.utils.ResourceUtils;
import com.android.settings.dashboard.DashboardFragment;

import java.lang.System;
import java.util.ArrayList;
import java.util.List;

public class TouchscreenGestureSettings extends DashboardFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "TouchscreenGestureSettings";

    private static final String KEY_TOUCHSCREEN_GESTURE = "touchscreen_gesture";
    private static final String TOUCHSCREEN_GESTURE_TITLE = KEY_TOUCHSCREEN_GESTURE + "_%s_title";

	private DeviceHardwareManager mHwManager;
    private TouchscreenGesture[] mTouchscreenGestures;
	List<ListPreference> mAvailableGestures = new ArrayList<>();

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

	@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mHwManager = DeviceHardwareManager.getInstance(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mHwManager != null) {
            mHwManager = null;
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.touchscreen_gesture_settings;
    }

    @Override
    public void displayResourceTiles() {
        super.displayResourceTiles();
		final PreferenceScreen screen = getPreferenceScreen();
        mTouchscreenGestures = mHwManager.getTouchscreenGestures();
        final int[] actions = getDefaultActions(getContext(), mTouchscreenGestures);

		for (final TouchscreenGesture gesture : mTouchscreenGestures) {
            ListPreference pref = new ListPreference(getPrefContext());
            pref.setKey(buildPreferenceKey(gesture));
			pref.setEntries(R.array.touchscreen_gesture_action_entries);
			pref.setEntryValues(R.array.touchscreen_gesture_action_values);
            pref.setDefaultValue(String.valueOf(actions[gesture.id]));

			pref.setSummary("%s");
			pref.setDialogTitle(R.string.touchscreen_gesture_action_dialog_title);
            pref.setTitle(ResourceUtils.getLocalizedString(
                    getContext().getResources(), gesture.name, TOUCHSCREEN_GESTURE_TITLE));
            pref.setOnPreferenceChangeListener(this);
            mAvailableGestures.add(pref);
        }

        for (ListPreference gesturePref : mAvailableGestures) {
            screen.addPreference(gesturePref);
        }
    }

	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt(String.valueOf(newValue));
		for (TouchscreenGesture gesture : mTouchscreenGestures) {
			mHwManager.setTouchscreenGestureEnabled(gesture, value > 0);
			sendUpdateBroadcast(getContext(), mTouchscreenGestures);
			break;
		}
        return true;
    }

	public static void restoreTouchscreenGestureStates(final Context context) {
        final DeviceHardwareManager manager = DeviceHardwareManager.getInstance(context);
        final TouchscreenGesture[] gestures = manager.getTouchscreenGestures();
        final int[] actions = buildActions(context, gestures);
        for (final TouchscreenGesture gesture : gestures) {
            manager.setTouchscreenGestureEnabled(gesture, actions[gesture.id] > 0);
        }
        sendUpdateBroadcast(context, gestures);
    }

	private static int[] getDefaultActions(final Context context,
            final TouchscreenGesture[] gestures) {
        final int[] defaultActions = context.getResources().getIntArray(
                R.array.config_defaultTouchscreenGestureActions);
        if (defaultActions.length >= gestures.length) {
            return defaultActions;
        }

        final int[] filledDefaultActions = new int[gestures.length];
        System.arraycopy(defaultActions, 0, filledDefaultActions, 0, defaultActions.length);
        return filledDefaultActions;
    }

	private static int[] buildActions(final Context context,
            final TouchscreenGesture[] gestures) {
        final int[] result = new int[gestures.length];
        final int[] defaultActions = getDefaultActions(context, gestures);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (final TouchscreenGesture gesture : gestures) {
            final String key = buildPreferenceKey(gesture);
            final String defaultValue = String.valueOf(defaultActions[gesture.id]);
            result[gesture.id] = Integer.parseInt(prefs.getString(key, defaultValue));
        }
        return result;
    }

    private static String buildPreferenceKey(final TouchscreenGesture gesture) {
        return "touchscreen_gesture_" + gesture.id;
    }

    private static void sendUpdateBroadcast(final Context context,
            final TouchscreenGesture[] gestures) {
        final Intent intent = new Intent(GestureConstants.UPDATE_GESTURE_ACTIONS);
        final int[] keycodes = new int[gestures.length];
        final int[] actions = buildActions(context, gestures);
        for (final TouchscreenGesture gesture : gestures) {
            keycodes[gesture.id] = gesture.keycode;
        }
        intent.putExtra(GestureConstants.UPDATE_EXTRA_KEYCODE_MAPPING, keycodes);
        intent.putExtra(GestureConstants.UPDATE_EXTRA_ACTION_MAPPING, actions);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }
}
