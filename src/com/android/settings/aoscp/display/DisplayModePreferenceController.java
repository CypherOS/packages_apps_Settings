/*
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.aoscp.display;

import android.content.Context;
import android.util.Log;

import aoscp.hardware.DisplayMode;
import aoscp.hardware.DeviceHardwareManager;

import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class DisplayModePreferenceController extends BasePreferenceController {
    private static final String TAG = "DisplayModePreference";
    private static final String KEY_DISPLAY_MODE = "display_mode";

    private Context mContext;

    public DisplayModePreferenceController(Context context) {
        super(context, KEY_DISPLAY_MODE);
        mContext = context;
    }

    @Override
    public int getAvailabilityStatus() {
        final DeviceHardwareManager hwManager = DeviceHardwareManager.getInstance(mContext);
        return hwManager.isSupported(DeviceHardwareManager.FEATURE_DISPLAY_MODES) 
                && hwManager.getDisplayModes() != null ? AVAILABLE : DISABLED_FOR_USER;
    }

    @Override
    public CharSequence getSummary() {
        final DeviceHardwareManager hwManager = DeviceHardwareManager.getInstance(mContext);
        final DisplayMode mode = hwManager.getCurrentDisplayMode() != null
                ? hwManager.getCurrentDisplayMode() : hwManager.getDefaultDisplayMode();
        return mode.name;
    }
}
