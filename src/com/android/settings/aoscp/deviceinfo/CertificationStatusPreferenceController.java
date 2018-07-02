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
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class CertificationStatusPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String TAG = "CertificationStatusPreference";
    private static final String KEY_CERTIFICATION_STATUS = "certification_status";
	
	private Context mContext;
	private String mStatus;
	
	private Preference mPreference;

    public CertificationStatusPreferenceController(Context context) {
        super(context);
		mContext = context;
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
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
		mStatus = SystemProperties.get("ro.aoscp.releasetype");
		updateDeviceStatus();
    }

	@Override
    public void updateState(Preference preference) {
        super.updateState(preference);
		updateDeviceStatus();
    }

	private void updateDeviceStatus() {
        Drawable certified = mContext.getResources().getDrawable(R.drawable.ic_certification_status_certified);
		certified.setTint(Utils.getColorAccent(mContext));
		Drawable uncertified = mContext.getResources().getDrawable(R.drawable.ic_certification_status_uncertified);
		uncertified.setTint(mContext.getResources().getColor(R.color.certification_status_uncertified));
        if ("unofficial".equals(mStatus)) {
			mPreference.setIcon(uncertified);
            mPreference.setTitle(R.string.device_certification_status_uncertified);
        } else if ("official".equals(mStatus)) {
			mPreference.setIcon(certified);
            mPreference.setTitle(R.string.device_certification_status_certified);
        } else if ("beta".equals(mStatus)) {
			mPreference.setIcon(uncertified);
            mPreference.setTitle(R.string.device_certification_status_uncertified);
        } else {
			mPreference.setIcon(uncertified);
            mPreference.setTitle(R.string.device_certification_status_unknown);
        }
    }
}