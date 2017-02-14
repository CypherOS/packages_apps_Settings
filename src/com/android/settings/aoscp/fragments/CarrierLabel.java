/*
 * Copyright (C) 2016 CypherOS
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
package com.android.settings.aoscp.fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.TextUtils;
import android.widget.EditText;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class CarrierLabel extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String KEY_CARRIER_SHOW =  "status_bar_show_carrier";
	private static final String KEY_CARRIER_INPUT = "custom_carrier_label";

    private PreferenceScreen mCustomInput;
    private ListPreference mShowCarrierLabel;
    private String mCustomInputText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.carrierlabel_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mShowCarrierLabel = (ListPreference) findPreference(KEY_CARRIER_SHOW);
        int showCarrierLabel = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_CARRIER, 1);
        mShowCarrierLabel.setValue(String.valueOf(showCarrierLabel));
        mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntry());
        mShowCarrierLabel.setOnPreferenceChangeListener(this);
		
		createDynamicPreference();
    }
	
	@Override
    public void onResume() {
        super.onResume();
        createDynamicPreference();
		updateInputSummary();
    }

    private void createDynamicPreference() {
        mCustomInput = (EditTextPreference) findPreference(KEY_CARRIER_INPUT);
			
        if (mShowCarrierLabel != Settings.System.getInt(resolver, Settings.System.STATUS_BAR_SHOW_CARRIER, 0) {
            mCustomInput.setEnabled(false);
        } else {
			if (mCustomInput.setEnabled(true)) {
                mCustomInput.setOnPreferenceClickListener(
                      new OnPreferenceClickListener() {
                          @Override
                          public boolean onPreferenceClick(Preference preference) {
                              CarrierLabelDialog.show(CarrierLabel.this);
                              return true;
                          }
                      });
            }
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ADDITIONS;
    }

    private void updateInputSummary() {
        mCustomInputText = Settings.System.getString(
            getContentResolver(), Settings.System.KEY_CARRIER_INPUT);

        if (TextUtils.isEmpty(mCustomInputText)) {
            mCustomInput.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomInput.setSummary(mCustomInputText);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
		ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mShowCarrierLabel) {
            int showCarrierLabel = Integer.valueOf((String) newValue);
            int index = mShowCarrierLabel.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.
                STATUS_BAR_SHOW_CARRIER, showCarrierLabel);
            mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntries()[index]);
            return true;
         }
         return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference.getKey().equals(KEY_CARRIER_INPUT)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.KEY_CARRIER_INPUT_title);
            alert.setMessage(R.string.KEY_CARRIER_INPUT_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(TextUtils.isEmpty(mCustomInputText) ? "" : mCustomInputText);
            input.setSelection(input.getText().length());
            alert.setView(input);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = ((Spannable) input.getText()).toString().trim();
                            Settings.System.putString(resolver, Settings.System.KEY_CARRIER_INPUT, value);
                            updateCustomLabelTextSummary();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_KEY_CARRIER_INPUT_CHANGED);
                            getActivity().sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
        }
        return super.onPreferenceTreeClick(preference);
    }
}
