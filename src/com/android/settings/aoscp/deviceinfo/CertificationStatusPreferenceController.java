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
package com.android.settings.aoscp.deviceinfo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class CertificationStatusPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String TAG = "CertificationStatusPreference";
    private static final String KEY_CERTIFICATION_STATUS = "certification_status";
	
	private static String mStatus;

    public CertificationStatusPreferenceController(Context context) {
        super(context);
		mStatus = SystemProperties.get("ro.aoscp.releasetype");
    }

    @Override
    public String getPreferenceKey() {
        return KEY_CERTIFICATION_STATUS;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

	@Override
    public void updateState(Preference preference) {
        super.updateState(preference);
		Drawable certified = getContext().getResources().getDrawable(R.drawable.ic_certification_status_certified);
		Drawable uncertified = getContext().getResources().getDrawable(R.drawable.ic_certification_status_uncertified);
        if ("unofficial".equals(mStatus)) {
			preference.setIcon(uncertified);
            preference.setSummary(R.string.device_certification_status_uncertified);
        } else if ("official".equals(mStatus)) {
			preference.setIcon(certified);
            preference.setSummary(R.string.device_certification_status_certified);
        } else if ("beta".equals(mStatus)) {
			preference.setIcon(uncertified);
            preference.setSummary(R.string.device_certification_status_uncertified);
        } else {
			preference.setIcon(uncertified);
            preference.setSummary(R.string.device_certification_status_unknown);
        }
    }
}