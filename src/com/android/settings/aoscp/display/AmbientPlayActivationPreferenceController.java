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

package com.android.settings.aoscp.display;

import static android.provider.Settings.System.AMBIENT_RECOGNITION;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.TogglePreferenceController;

public class AmbientPlayActivationPreferenceController extends TogglePreferenceController {

    private Button mTurnOffButton;
    private Button mTurnOnButton;

    private final OnClickListener mOnListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
			Settings.System.putInt(mContext.getContentResolver(), AMBIENT_RECOGNITION, 1);
            updateStateInternal();
        }
    };
	
	private final OnClickListener mOffListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
			Settings.System.putInt(mContext.getContentResolver(), AMBIENT_RECOGNITION, 0);
            updateStateInternal();
        }
    };

    public AmbientPlayActivationPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "ambient_play_activated");
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        final LayoutPreference preference = (LayoutPreference) screen.findPreference(
                getPreferenceKey());
        mTurnOnButton = preference.findViewById(R.id.ambient_play_turn_on_button);
        mTurnOnButton.setOnClickListener(mOnListener);
        mTurnOffButton = preference.findViewById(R.id.ambient_play_turn_off_button);
        mTurnOffButton.setOnClickListener(mOffListener);
    }

    @Override
    public final void updateState(Preference preference) {
        updateStateInternal();
    }

    /** FOR SLICES */

    @Override
    public boolean isChecked() {
		boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(), 
			        AMBIENT_RECOGNITION, 1) != 0;
        return isEnabled;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return setEnabled(isChecked);
    }

	private boolean setEnabled(boolean isChecked) {
		if (isChecked) {
			Settings.System.putInt(mContext.getContentResolver(), AMBIENT_RECOGNITION, 1);
			return true;
		}
		Settings.System.putInt(mContext.getContentResolver(), AMBIENT_RECOGNITION, 0);
		return false;
	}

    private void updateStateInternal() {
        if (mTurnOnButton == null || mTurnOffButton == null) {
            return;
        }
		boolean isEnabled = Settings.System.getInt(mContext.getContentResolver(), 
			    AMBIENT_RECOGNITION, 1) != 0;
		String buttonText;
		buttonText = mContext.getString(isEnabled
                ? R.string.ambient_play_activation_off
                : R.string.ambient_play_activation_on);

        if (isEnabled) {
            mTurnOnButton.setVisibility(View.GONE);
            mTurnOffButton.setVisibility(View.VISIBLE);
            mTurnOffButton.setText(buttonText);
        } else {
            mTurnOnButton.setVisibility(View.VISIBLE);
            mTurnOffButton.setVisibility(View.GONE);
            mTurnOnButton.setText(buttonText);
        }
    }
}
