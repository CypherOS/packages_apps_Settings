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
package com.android.settings.aoscp.deviceinfo;

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

public class MaintenancePatchPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_MAINTENANCE_PATCH = "maintenance_patch";
    private static final String TAG = "MaintenancePatchPref";

    /* Returns the maintenance patch level */
    private static String mPatch;
    private final PackageManager mPackageManager;

    public MaintenancePatchPreferenceController(Context context) {
        super(context);
        mPackageManager = mContext.getPackageManager();
        mPatch = SystemProperties.get("ro.aoscp.maintenance_patch", "");
    }

    @Override
    public boolean isAvailable() {
        return !TextUtils.isEmpty(mPatch);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_MAINTENANCE_PATCH;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference pref = screen.findPreference(KEY_MAINTENANCE_PATCH);
        if (pref != null) {
            pref.setSummary(getMaintenancePatch());
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }

    public static String getMaintenancePatch() {
        if (!"".equals(mPatch)) {
            try {
                SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd");
                Date patchDate = template.parse(mPatch);
                String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                mPatch = DateFormat.format(format, patchDate).toString();
            } catch (ParseException e) {}
        }
        return mPatch;
    }
}
