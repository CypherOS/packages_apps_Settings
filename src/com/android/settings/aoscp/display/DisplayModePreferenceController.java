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

import aoscp.hardware.DisplayEngineController;

import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class DisplayModePreferenceController extends BasePreferenceController {
    private static final String TAG = "DisplayModePreference";
    private static final String KEY_DISPLAY_MODE = "display_mode";

    private DisplayEngineController mController;

    public DisplayModePreferenceController(Context context) {
        super(context, KEY_DISPLAY_MODE);
    }

    @Override
    public int getAvailabilityStatus() {
        return getController.isAvailable() ?
                AVAILABLE : DISABLED_FOR_USER;
    }

    @Override
    public CharSequence getSummary() {
        final int displayMode = getController().getCurrentMode();
        if (displayMode == 3) {
            return getController().getModeEntry(3);
        }
        if (displayMode == 2) {
            return getController().getModeEntry(2);
        }
        if (displayMode == 1) {
            return getController().getModeEntry(1);
        }
        return getController().getModeEntry(0);
    }

    @VisibleForTesting
    DisplayEngineController getController() {
        if (mController == null) {
            mController = new DisplayEngineController(mContext);
        }
        return mController;
    }
}
