/*
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

package com.android.settings.aoscp.preference;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;

import com.android.settings.R;
import com.android.settings.aoscp.widget.IllustrationPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.core.AbstractPreferenceController;

public abstract class IllustrationPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private IllustrationPreference mPreference;

    protected final int ON = 1;
    protected final int OFF = 0;

    private boolean mEnabled;

    public IllustrationPreferenceController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
		final Activity activity = getActivity();
        if (isAvailable()) {
            mPreference = (IllustrationPreference) screen.findPreference(getIllustrationKey());
			EntityHeaderController.newInstance(activity, this, mPreference.findViewById(R.id.illustration_view))
                .styleActionBar(activity);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        mEnabled = isSwitchPrefEnabled();
        if (preference != null) {
            if (preference instanceof TwoStatePreference) {
                ((TwoStatePreference) preference).setChecked(mEnabled);
            } else {
                preference.setSummary(getSummary());
            }
            preference.setEnabled(canHandleClicks());
        }
    }

    protected abstract String getIllustrationKey();

    protected abstract boolean isSwitchPrefEnabled();

    protected boolean canHandleClicks() {
        return true;
    }

    protected int getSummary() {
        return mEnabled
            ? R.string.gesture_setting_on
            : R.string.gesture_setting_off;
    }
}
