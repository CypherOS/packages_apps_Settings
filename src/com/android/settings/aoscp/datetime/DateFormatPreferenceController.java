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
package com.android.settings.aoscp.datetime;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.EditText;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.Date;

public class DateFormatPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "DateFormatPref";
	private static final String CLOCK_DATE_FORMAT = "clock_date_format";
	
	public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
    public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
	private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;
  
    private ListPreference mDateFormat;

    public DateFormatPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return CLOCK_DATE_FORMAT;
    }

    @Override
    public void updateState(Preference pref) {
        final ListPreference mDateFormat = (ListPreference) pref;
        final Resources res = mContext.getResources();
        if (mDateFormat != null) {
			mDateFormat.setOnPreferenceChangeListener(this);
			if (mDateFormat.getValue() == null) {
				mDateFormat.setValue("EEE");
			}
			if (dateIndicatorDisabled()) {
				((ListPreference) pref).setEnabled(false);
			} else { 
			    ((ListPreference) pref).setEnabled(true);
			}
			updateDateFormats();
        }
    }
	
	protected boolean dateIndicatorDisabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, 0) != 0;
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
		AlertDialog dialog;
        if (pref == mDateFormat) {
            int index = mDateFormat.findIndexOfValue((String) newValue);
			if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                alert.setTitle(R.string.clock_date_string_edittext_title);
                alert.setMessage(R.string.clock_date_string_edittext_summary);

                final EditText input = new EditText(mContext);
                String oldText = Settings.System.getString(
                    mContext.getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_FORMAT);
                if (oldText != null) {
                    input.setText(oldText);
                }
                alert.setView(input);
  
                alert.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        String value = input.getText().toString();
                        if (value.equals("")) {
                            return;
                        }
                        Settings.System.putString(mContext.getContentResolver(),
                            Settings.System.STATUSBAR_CLOCK_DATE_FORMAT, value);
						return;
                    }
                });
  
                alert.setNegativeButton(R.string.menu_cancel,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });
                dialog = alert.create();
                dialog.show();
            } else {
                if ((String) newValue != null) {
                    Settings.System.putString(mContext.getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_DATE_FORMAT, (String) newValue);
                }
            }
            return true;
		}
        return false;
    }
	
	private void updateDateFormats() {
        String[] dateEntries = mContext.getResources().getStringArray(
                R.array.clock_date_format_entries_values);
        CharSequence parsedDateEntries[];
        parsedDateEntries = new String[dateEntries.length];
        Date now = new Date();

        int lastEntry = dateEntries.length - 1;
        int dateFormat = Settings.System.getInt(mContext.getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_STYLE, 0);
        for (int i = 0; i < dateEntries.length; i++) {
            if (i == lastEntry) {
                parsedDateEntries[i] = dateEntries[i];
            } else {
                String newDate;
                CharSequence dateString = DateFormat.format(dateEntries[i], now);
                if (dateFormat == CLOCK_DATE_STYLE_LOWERCASE) {
                    newDate = dateString.toString().toLowerCase();
                } else if (dateFormat == CLOCK_DATE_STYLE_UPPERCASE) {
                    newDate = dateString.toString().toUpperCase();
                } else {
                    newDate = dateString.toString();
                }
                parsedDateEntries[i] = newDate;
            }
        }
        mDateFormat.setEntries(parsedDateEntries);
    }
}
