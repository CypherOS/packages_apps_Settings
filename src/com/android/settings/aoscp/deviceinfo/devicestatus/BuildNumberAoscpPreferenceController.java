/*
 * Copyright (C) 2017 The Android Open Source Project
 * Copyright (C) 2018 CypherOS
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
package com.android.settings.aoscp.deviceinfo.devicestatus;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BuildNumberAoscpPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_BUILD_NUMBER_AOSCP = "build_number_aoscp";
    private static final String TAG = "BuildNumberAoscpPref";

    /* Returns the aoscp build number */
    private static String mBuildNumber;

    public BuildNumberAoscpPreferenceController(Context context) {
        super(context);
        mBuildNumber = Build.AOSCP.BUILD_NUMBER;
    }

    @Override
    public boolean isAvailable() {
        return !TextUtils.isEmpty(mBuildNumber);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BUILD_NUMBER_AOSCP;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference pref = screen.findPreference(KEY_BUILD_NUMBER_AOSCP);
        if (pref != null) {
            pref.setSummary(mBuildNumber);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }
}
