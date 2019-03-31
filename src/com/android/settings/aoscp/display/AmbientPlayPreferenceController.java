/*
 * Copyright (C) 2019 CypherOS
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

import static android.provider.Settings.System.AMBIENT_RECOGNITION;
import static android.provider.Settings.System.AMBIENT_RECOGNITION_KEYGUARD;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class AmbientPlayPreferenceController extends BasePreferenceController {

    private static final String KEY_AMBIENT_PLAY = "ambient_play";

    private Context mContext;

    public AmbientPlayPreferenceController(Context context) {
        super(context, KEY_AMBIENT_PLAY);
        mContext = context;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        CharSequence summary;
        boolean enabled = Settings.System.getInt(mContext.getContentResolver(),
                AMBIENT_RECOGNITION, 0) != 0;
        boolean allowedOnKeyguard = Settings.System.getInt(mContext.getContentResolver(),
                AMBIENT_RECOGNITION_KEYGUARD, 1) != 0;
        if (enabled) {
            summary = mContext.getText(R.string.ambient_play_summary_on);
        } else if (enabled && allowedOnKeyguard) {
            summary = mContext.getText(R.string.ambient_play_summary_on_keyguard);
        } else {
            summary = mContext.getText(R.string.ambient_play_summary_off);
        }
        return summary;
    }
}