/**
 * Copyright (C) 2016 The CyanogenMod project
 *               2017 The LineageOS Project
 *               2017 CypherOS
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

package com.android.settings.aoscp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceManager;

import aoscp.hardware.LunaHardwareManager;
import aoscp.hardware.OffscreenGesture;

import com.android.internal.aoscp.OffscreenGestureConstants;
import com.android.server.policy.KeyHandler;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.aoscp.utils.ResourceUtils;

import java.lang.System;

public class GestureSettings extends SettingsPreferenceFragment {
    private static final String KEY_OFFSCREEN_GESTURE = "offscreen_gesture";
    private static final String OFFSCREEN_GESTURE_TITLE = KEY_OFFSCREEN_GESTURE + "_%s_title";

    private OffscreenGesture[] mOffscreenGestures;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.gesture_settings);

        if (isOffscreenGesturesSupported(getContext())) {
            initOffscreenGestures();
        }
    }

    private void initOffscreenGestures() {
        final LunaHardwareManager manager = LunaHardwareManager.getInstance(getContext());
        mOffscreenGestures = manager.getOffscreenGestures();
        final int[] actions = getDefaultGestureActions(getContext(), mOffscreenGestures);
        for (final OffscreenGesture gesture : mOffscreenGestures) {
            getPreferenceScreen().addPreference(new OffscreenGesturePreference(
                    getContext(), gesture, actions[gesture.id]));
        }
    }

    private class OffscreenGesturePreference extends ListPreference {
        private final Context mContext;
        private final OffscreenGesture mGesture;

        public OffscreenGesturePreference(final Context context,
                                            final OffscreenGesture gesture,
                                            final int defaultAction) {
            super(context);
            mContext = context;
            mGesture = gesture;

            setKey(buildPreferenceKey(gesture));
            setEntries(R.array.offscreen_gesture_action_entries);
            setEntryValues(R.array.offscreen_gesture_action_values);
            setDefaultValue(String.valueOf(defaultAction));

            setSummary("%s");
            setDialogTitle(R.string.offscreen_gesture_action_dialog_title);
            setTitle(ResourceUtils.getLocalizedString(
                    context.getResources(), gesture.name, OFFSCREEN_GESTURE_TITLE));
        }

        @Override
        public boolean callChangeListener(final Object newValue) {
            final int action = Integer.parseInt(String.valueOf(newValue));
            final LunaHardwareManager manager = LunaHardwareManager.getInstance(mContext);
            if (!manager.setOffscreenGestureEnabled(mGesture, action > 0)) {
                return false;
            }
            return super.callChangeListener(newValue);
        }

        @Override
        protected boolean persistString(String value) {
            if (!super.persistString(value)) {
                return false;
            }
            sendUpdateBroadcast(mContext, mOffscreenGestures);
            return true;
        }
    }

    public static void restoreOffscreenGestureStates(final Context context) {
        if (!isOffscreenGesturesSupported(context)) {
            return;
        }

        final LunaHardwareManager manager = LunaHardwareManager.getInstance(context);
        final OffscreenGesture[] gestures = manager.getOffscreenGestures();
        final int[] actionList = buildActionList(context, gestures);
        for (final OffscreenGesture gesture : gestures) {
            manager.setOffscreenGestureEnabled(gesture, actionList[gesture.id] > 0);
        }

        sendUpdateBroadcast(context, gestures);
    }

    private static boolean isOffscreenGesturesSupported(final Context context) {
        final LunaHardwareManager manager = LunaHardwareManager.getInstance(context);
        return manager.isSupported(LunaHardwareManager.FEATURE_OFFSCREEN_GESTURES);
    }

    private static int[] getDefaultGestureActions(final Context context,
            final OffscreenGesture[] gestures) {
        final int[] defaultActions = context.getResources().getIntArray(
                R.array.config_defaultOffscreenGestureActions);
        if (defaultActions.length >= gestures.length) {
            return defaultActions;
        }

        final int[] filledDefaultActions = new int[gestures.length];
        System.arraycopy(defaultActions, 0, filledDefaultActions, 0, defaultActions.length);
        return filledDefaultActions;
    }

    private static int[] buildActionList(final Context context,
            final OffscreenGesture[] gestures) {
        final int[] result = new int[gestures.length];
        final int[] defaultActions = getDefaultGestureActions(context, gestures);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (final OffscreenGesture gesture : gestures) {
            final String key = buildPreferenceKey(gesture);
            final String defaultValue = String.valueOf(defaultActions[gesture.id]);
            result[gesture.id] = Integer.parseInt(prefs.getString(key, defaultValue));
        }
        return result;
    }

    private static String buildPreferenceKey(final OffscreenGesture gesture) {
        return "offscreen_gesture_" + gesture.id;
    }

    private static void sendUpdateBroadcast(final Context context,
            final OffscreenGesture[] gestures) {
        final Intent intent = new Intent(OffscreenGestureConstants.UPDATE_PREFS_ACTION);
        final int[] keycodes = new int[gestures.length];
        final int[] actions = buildActionList(context, gestures);
        for (final OffscreenGesture gesture : gestures) {
            keycodes[gesture.id] = gesture.keycode;
        }
        intent.putExtra(OffscreenGestureConstants.UPDATE_EXTRA_KEYCODE_MAPPING, keycodes);
        intent.putExtra(OffscreenGestureConstants.UPDATE_EXTRA_ACTION_MAPPING, actions);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }
}