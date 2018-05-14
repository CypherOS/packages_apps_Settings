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

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;

import com.android.internal.app.NightDisplayController;
import com.android.settings.R;
import com.android.settings.aoscp.widget.IllustrationPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import java.text.DateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;

public abstract class NightDisplayIllustrationPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener, NightDisplayController.Callback {

    private IllustrationPreference mIllustrationPreference;
	private Preference mPreference;
	private NightDisplayController mController;
    private DateFormat mTimeFormatter;

    protected final int ON = 1;
    protected final int OFF = 0;
	
	private boolean mEnabled;

    public NightDisplayIllustrationPreferenceController(Context context) {
        super(context);
		mController = new NightDisplayController(context);
        mTimeFormatter = android.text.format.DateFormat.getTimeFormat(context);
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
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
		mPreference = preference;
        mEnabled = isSwitchPrefEnabled();
        if (preference != null) {
            if (preference instanceof TwoStatePreference) {
                ((TwoStatePreference) preference).setChecked(mEnabled);
            } else {
				updateSummary(preference);
            }
            preference.setEnabled(canHandleClicks());
        }
    }
	
	@Override
    public void onAttached() {
        super.onAttached();
        // Listen for changes only while attached.
        mController.setListener(this);

        // Update the summary since the state may have changed while not attached.
        updateSummary(mPreference);
    }

    @Override
    public void onDetached() {
        super.onDetached();
        // Stop listening for state changes.
        mController.setListener(null);
    }
	
	private String getFormattedTimeString(LocalTime localTime) {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(mTimeFormatter.getTimeZone());
        c.set(Calendar.HOUR_OF_DAY, localTime.getHour());
        c.set(Calendar.MINUTE, localTime.getMinute());
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return mTimeFormatter.format(c.getTime());
    }
	
	private void updateSummary(Preference preference) {
        final boolean isActivated = mController.isActivated();
        final int autoMode = mController.getAutoMode();

        final String autoModeSummary;
        switch (autoMode) {
            default:
            case NightDisplayController.AUTO_MODE_DISABLED:
                autoModeSummary = mContext.getString(isActivated
                        ? R.string.night_display_summary_on_auto_mode_never
                        : R.string.night_display_summary_off_auto_mode_never);
                break;
            case NightDisplayController.AUTO_MODE_CUSTOM:
                if (isActivated) {
                    autoModeSummary = mContext.getString(
                            R.string.night_display_summary_on_auto_mode_custom,
                            getFormattedTimeString(mController.getCustomEndTime()));
                } else {
                    autoModeSummary = mContext.getString(
                            R.string.night_display_summary_off_auto_mode_custom,
                            getFormattedTimeString(mController.getCustomStartTime()));
                }
                break;
            case NightDisplayController.AUTO_MODE_TWILIGHT:
                autoModeSummary = mContext.getString(isActivated
                        ? R.string.night_display_summary_on_auto_mode_twilight
                        : R.string.night_display_summary_off_auto_mode_twilight);
                break;
        }

        final int summaryFormatResId = isActivated ? R.string.night_display_summary_on
                : R.string.night_display_summary_off;
        preference.setSummary(mContext.getString(summaryFormatResId, autoModeSummary));
    }

    protected abstract String getIllustrationKey();

    protected abstract boolean isSwitchPrefEnabled();

    protected boolean canHandleClicks() {
        return true;
    }
	
	@Override
    public void onActivated(boolean activated) {
        updateSummary(mPreference);
    }

    @Override
    public void onAutoModeChanged(int autoMode) {
        updateSummary(mPreference);
    }

    @Override
    public void onCustomStartTimeChanged(LocalTime startTime) {
        updateSummary(mPreference);
    }

    @Override
    public void onCustomEndTimeChanged(LocalTime endTime) {
        updateSummary(mPreference);
    }
}
