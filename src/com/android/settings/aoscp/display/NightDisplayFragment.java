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

package com.android.settings.aoscp.display;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.TimePicker;

import com.android.internal.app.NightDisplayController;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.aoscp.display.NightDisplayPreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.ThemePreferenceController;
import com.android.settings.widget.RadioButtonPreference;
import com.android.settings.widget.SeekBarPreference;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.text.DateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

public class NightDisplayFragment extends DashboardFragment implements NightDisplayController.Callback, 
        RadioButtonPreference.OnClickListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "NightDisplay";
	
	private static final String KEY_NIGHT_DISPLAY              = "night_display_activated";
	private static final String KEY_NIGHT_DISPLAY_TEMPERATURE  = "night_display_temperature";
	
	private static final String KEY_NIGHT_DISPLAY_AUTO_MODE    = "night_display_auto_mode";
    private static final String KEY_NIGHT_DISPLAY_START_TIME   = "night_display_start_time";
    private static final String KEY_NIGHT_DISPLAY_END_TIME     = "night_display_end_time";

	private static final String KEY_NIGHT_DISPLAY_HIGH         = "night_display_high";
	private static final String KEY_NIGHT_DISPLAY_MID          = "night_display_mid";
    private static final String KEY_NIGHT_DISPLAY_LOW          = "night_display_low";
	private static final String KEY_NIGHT_DISPLAY_CUSTOM       = "night_display_custom";

	public static final int NIGHT_DISPLAY_HIGH = 2596;
	public static final int NIGHT_DISPLAY_MID = 3339;
	public static final int NIGHT_DISPLAY_LOW = 4082;
	public static final int NIGHT_DISPLAY_CUSTOM = 2700; // Random value for use later
	
	private static final int DIALOG_START_TIME = 0;
    private static final int DIALOG_END_TIME = 1;

	private RadioButtonPreference mModes;
    List<RadioButtonPreference> mNightDisplayModes = new ArrayList<>();

    private Context mContext;
	private DateFormat mTimeFormatter;
	private NightDisplayController mController;
	
	private DropDownPreference mAutoModePreference;
    private Preference mStartTimePreference;
    private Preference mEndTimePreference;
	private SeekBarPreference mTemperaturePreference;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.NIGHT_DISPLAY_SETTINGS;
    }
    
    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.night_display_settings;
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    @Override
    public void displayResourceTiles() {
        final int resId = getPreferenceScreenResId();
        if (resId <= 0) {
            return;
        }
        addPreferencesFromResource(resId);
        final PreferenceScreen screen = getPreferenceScreen();
        Collection<AbstractPreferenceController> controllers = mPreferenceControllers.values();
        for (AbstractPreferenceController controller : controllers) {
            controller.displayPreference(screen);
        }
		mController = new NightDisplayController(getContext());
		mTimeFormatter = android.text.format.DateFormat.getTimeFormat(context);
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		mAutoModePreference = (DropDownPreference) findPreference(KEY_NIGHT_DISPLAY_AUTO_MODE);
		mAutoModePreference.setEntries(new CharSequence[] {
                getString(R.string.night_display_auto_mode_never),
                getString(R.string.night_display_auto_mode_custom),
                getString(R.string.night_display_auto_mode_twilight)
        });
        mAutoModePreference.setEntryValues(new CharSequence[] {
                String.valueOf(NightDisplayController.AUTO_MODE_DISABLED),
                String.valueOf(NightDisplayController.AUTO_MODE_CUSTOM),
                String.valueOf(NightDisplayController.AUTO_MODE_TWILIGHT)
        });
        mAutoModePreference.setOnPreferenceChangeListener(this);

		mStartTimePreference = findPreference(KEY_NIGHT_DISPLAY_START_TIME);
        mEndTimePreference = findPreference(KEY_NIGHT_DISPLAY_END_TIME);
		
		mTemperaturePreference = (SeekBarPreference) findPreference(KEY_NIGHT_DISPLAY_TEMPERATURE);
		mTemperaturePreference.setOnPreferenceChangeListener(this);
		mTemperaturePreference.setMax(convertTemperature(mController.getMinimumColorTemperature()));
        mTemperaturePreference.setContinuousUpdates(true);

        for (int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference pref = screen.getPreference(i);
            if (pref instanceof RadioButtonPreference) {
                mModes = (RadioButtonPreference) pref;
                mModes.setOnClickListener(this);
                mNightDisplayModes.add(mModes);
            }
        }

		int colorTemperature = mController.getColorTemperature();
		if (colorTemperature == NIGHT_DISPLAY_HIGH) {
			updatePresets(KEY_NIGHT_DISPLAY_HIGH, false);
		} else if (colorTemperature == NIGHT_DISPLAY_MID) {
			updatePresets(KEY_NIGHT_DISPLAY_MID, false);
		} else if (colorTemperature == NIGHT_DISPLAY_LOW) {
			updatePresets(KEY_NIGHT_DISPLAY_LOW, false);
		} else if (colorTemperature != NIGHT_DISPLAY_HIGH || 
		           colorTemperature != NIGHT_DISPLAY_MID || 
				   colorTemperature != NIGHT_DISPLAY_LOW) {
			// If the given value from seekbar returns anything but the preset
			// modes, then check the custom option if not already
			updatePresets(KEY_NIGHT_DISPLAY_CUSTOM, true);
		}
    }

	@Override
    public void onStart() {
        super.onStart();
        // Listen for changes only while visible.
        mController.setListener(this);

        // Update the current state since it have changed while not visible.
        onActivated(mController.isActivated());
		onAutoModeChanged(mController.getAutoMode());
        onCustomStartTimeChanged(mController.getCustomStartTime());
        onCustomEndTimeChanged(mController.getCustomEndTime());
        onColorTemperatureChanged(mController.getColorTemperature());
    }
	
	@Override
    public void onStop() {
        super.onStop();
        // Stop listening for state changes.
        mController.setListener(null);
    }
	
	@Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mStartTimePreference) {
            showDialog(DIALOG_START_TIME);
            return true;
        } else if (preference == mEndTimePreference) {
            showDialog(DIALOG_END_TIME);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
	
	@Override
    public Dialog onCreateDialog(final int dialogId) {
        if (dialogId == DIALOG_START_TIME || dialogId == DIALOG_END_TIME) {
            final LocalTime initialTime;
            if (dialogId == DIALOG_START_TIME) {
                initialTime = mController.getCustomStartTime();
            } else {
                initialTime = mController.getCustomEndTime();
            }

            final Context context = getContext();
            final boolean use24HourFormat = android.text.format.DateFormat.is24HourFormat(context);
            return new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    final LocalTime time = LocalTime.of(hourOfDay, minute);
                    if (dialogId == DIALOG_START_TIME) {
                        mController.setCustomStartTime(time);
                    } else {
                        mController.setCustomEndTime(time);
                    }
                }
            }, initialTime.getHour(), initialTime.getMinute(), use24HourFormat);
        }
        return super.onCreateDialog(dialogId);
    }
	
	@Override
    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case DIALOG_START_TIME:
                return MetricsEvent.DIALOG_NIGHT_DISPLAY_SET_START_TIME;
            case DIALOG_END_TIME:
                return MetricsEvent.DIALOG_NIGHT_DISPLAY_SET_END_TIME;
            default:
                return 0;
        }
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
		controllers.add(new NightDisplayPreferenceController(context, KEY_NIGHT_DISPLAY));
        return controllers;
    }

    private void updatePresets(String selectionKey, boolean isCustom) {
        for (RadioButtonPreference pref : mNightDisplayModes) {
            if (selectionKey.equals(pref.getKey())) {
                pref.setChecked(true);
            } else {
                pref.setChecked(false);
            }
			if (isCustom) {
				mTemperaturePreference.setEnabled(true);
			} else {
				mTemperaturePreference.setEnabled(false);
			}
        }
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference pref) {
        switch (pref.getKey()) {
            case KEY_NIGHT_DISPLAY_HIGH:
                mController.setColorTemperature(NIGHT_DISPLAY_HIGH);
				updatePresets(pref.getKey(), false);
                break;
            case KEY_NIGHT_DISPLAY_MID:
                mController.setColorTemperature(NIGHT_DISPLAY_MID);
				updatePresets(pref.getKey(), false);
                break;
            case KEY_NIGHT_DISPLAY_LOW:
                mController.setColorTemperature(NIGHT_DISPLAY_LOW);
				updatePresets(pref.getKey(), false);
                break;
			case KEY_NIGHT_DISPLAY_CUSTOM:
			    mController.setColorTemperature(NIGHT_DISPLAY_CUSTOM);
				updatePresets(pref.getKey(), true);
                break;
        }
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

	/**
     * Inverts and range-adjusts a raw value from the SeekBar (i.e. [0, maxTemp-minTemp]), or
     * converts an inverted and range-adjusted value to the raw SeekBar value, depending on the
     * adjustment status of the input.
     */
    private int convertTemperature(int temperature) {
        return mController.getMaximumColorTemperature() - temperature;
    }
	
	@Override
    public void onAutoModeChanged(int autoMode) {
        mAutoModePreference.setValue(String.valueOf(autoMode));

        final boolean showCustomSchedule = autoMode == NightDisplayController.AUTO_MODE_CUSTOM;
        mStartTimePreference.setVisible(showCustomSchedule);
        mEndTimePreference.setVisible(showCustomSchedule);
    }

	@Override
    public void onColorTemperatureChanged(int colorTemperature) {
        mTemperaturePreference.setProgress(convertTemperature(colorTemperature));
    }
	
	@Override
    public void onCustomStartTimeChanged(LocalTime startTime) {
        mStartTimePreference.setSummary(getFormattedTimeString(startTime));
    }

    @Override
    public void onCustomEndTimeChanged(LocalTime endTime) {
        mEndTimePreference.setSummary(getFormattedTimeString(endTime));
    }
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mAutoModePreference) {
            return mController.setAutoMode(Integer.parseInt((String) newValue));
        } else if (preference == mTemperaturePreference) {
            return mController.setColorTemperature(convertTemperature((Integer) newValue));
        }
        return false;
    }
}