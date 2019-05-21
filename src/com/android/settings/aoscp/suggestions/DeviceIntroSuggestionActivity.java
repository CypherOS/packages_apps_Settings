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

package com.android.settings.aoscp.suggestions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.annotation.VisibleForTesting;

import com.android.settings.R;

public class DeviceIntroSuggestionActivity extends Activity {

    private static final String TAG = "DeviceIntroSugg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getLaunchIntent(this);
        if (intent != null) {
            Settings.System.putInt(this.getContentResolver(), Settings.System.DEVICE_INTRODUCTION_COMPLETED, 1);
            startActivity(intent);
        }
        finish();
    }

    public static boolean isSuggestionComplete(Context context) {
        return isFinished(context);
    }

    private static boolean isSupported(Context context) {
        return true;
    }

    private static boolean isFinished(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(),
                Settings.System.DEVICE_INTRODUCTION_COMPLETED, 0, UserHandle.USER_CURRENT) != 0;
    }

    @VisibleForTesting
    static Intent getLaunchIntent(Context context) {
        return new Intent()
                .setAction(Settings.ACTION_DEVICE_INTRODUCTION)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }
}
