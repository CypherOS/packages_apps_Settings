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

package com.android.settings.aoscp.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public abstract class IllustrationPreferenceController extends TogglePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private IllustrationPreference mIllustrationPreference;

    public IllustrationPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mIllustrationPreference = (IllustrationPreference) screen.findPreference(getIllustrationKey());
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            preference.setEnabled(canHandleClicks());
        }
    }

    @Override
    public CharSequence getSummary() {
        return mContext.getText(
                isChecked() ? R.string.gesture_setting_on : R.string.gesture_setting_off);
    }

    protected abstract String getIllustrationKey();

    protected boolean canHandleClicks() {
        return true;
    }
}
