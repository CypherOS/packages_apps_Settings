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

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.NonNull;

import aoscp.hardware.DeviceHardwareManager;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.ArrayList;
import java.util.List;

public class TouchscreenGesturePreferenceController extends BasePreferenceController {

    public TouchscreenGesturePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
		final DeviceHardwareManager manager = DeviceHardwareManager.getInstance(context);
        return manager.isSupported(DeviceHardwareManager.FEATURE_TOUCHSCREEN_GESTURES) 
		        ? AVAILABLE : UNAVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        return mContext.getText(R.string.touchscreen_gesture_settings_summary);
    }
}