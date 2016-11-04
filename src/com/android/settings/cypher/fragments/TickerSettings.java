/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

package com.android.settings.cypher.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen; 
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.utils.Utils;

import com.android.settings.preference.BaseSystemSettingSwitchBar;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class TickerSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SHOW_TICKER =
            "status_bar_show_ticker";
    private static final String CAT_COLORS =
            "ticker_colors";
    private static final String TEXT_COLOR =
            "status_bar_ticker_text_color";
    private static final String ICON_COLOR =
            "status_bar_ticker_icon_color";

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET  = 0;

    private ColorPickerPreference mTextColor;
    private ColorPickerPreference mIconColor;
	
	private BaseSystemSettingSwitchBar mEnabledSwitch;
	
	private ViewGroup mPrefsTickContainer;
    private View mDisabledTickText;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshSettings();
    }

    public void refreshSettings() {
        PreferenceScreen pref = getPreferenceScreen();

        addPreferencesFromResource(R.xml.ticker_settings);
        mResolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

        PreferenceCategory catColors =
                (PreferenceCategory) findPreference(CAT_COLORS);

        if (showTicker) {
            mTextColor =
                    (ColorPickerPreference) findPreference(TEXT_COLOR);
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_TICKER_TEXT_COLOR,
                    0xffffffff); 
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTextColor.setSummary(hexColor);
			mTextColor.setNewPreviewColor(intColor);
            mTextColor.setOnPreferenceChangeListener(this);

            mIconColor =
                    (ColorPickerPreference) findPreference(ICON_COLOR);
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_TICKER_ICON_COLOR,
                    0xffffffff); 
            mIconColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mIconColor.setSummary(hexColor);
            mIconColor.setNewPreviewColor(intColor);
            mIconColor.setOnPreferenceChangeListener(this);
        } else {
            removePreference(CAT_COLORS);
        }

        setHasOptionsMenu(true);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ticker_fragment, container, false);
        mPrefsTickContainer = (ViewGroup) v.findViewById(R.id.prefstick_container);
        mDisabledTickText = v.findViewById(R.id.disabledtick_text);

        View prefs = super.onCreateView(inflater, mPrefsTickContainer, savedInstanceState);
        mPrefsTickContainer.addView(prefs);

        return v;
    }
	
	@Override
    public void onStart() {
        super.onStart();
        final SettingsActivity activity = (SettingsActivity) getActivity();
        mEnabledSwitch = new BaseSystemSettingSwitchBar(activity, activity.getSwitchBar(),
                Settings.System.STATUS_BAR_SHOW_TICKER, true, this);
    }
	
	@Override
    public void onResume() {
        super.onResume();

        final SettingsActivity activity = (SettingsActivity) getActivity();
        if (mEnabledSwitch != null) {
            mEnabledSwitch.resume(activity);
        }

        if (!Utils.isTablet(getActivity())) {
            mPrefsTickContainer.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mEnabledSwitch != null) {
            mEnabledSwitch.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mEnabledSwitch != null) {
            mEnabledSwitch.teardownSwitchBar();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_action_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialogInner(DLG_RESET);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String hex;
        int intHex;

        if (preference == mShowTicker) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_SHOW_TICKER,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_TICKER_TEXT_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_TICKER_ICON_COLOR,
                    intHex);
            preference.setSummary(hex);
            return true;
        }
        return false;
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        TickerSettings getOwner() {
            return (TickerSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.reset_color_message)
                    .setNegativeButton(R.string.reset_no, null)
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_SHOW_TICKER, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_TEXT_COLOR,
                                    WHITE);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_ICON_COLOR,
                                    WHITE);
                            getOwner().refreshSettings();
                        }
                    })
                    .setPositiveButton(R.string.reset_yes,
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_SHOW_TICKER, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_TEXT_COLOR,
                                    WHITE);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_ICON_COLOR,
                                    WHITE);
                            getOwner().refreshSettings();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ADDITIONS;
    }
	
	private boolean getUserTickerState() {
        return Settings.System.getIntForUser(getContentResolver(),
               Settings.System.STATUS_BAR_SHOW_TICKER,
               UserHandle.USER_CURRENT) != 0;
    }

    private void setUserTickerState(int val) {
        Settings.System.putIntForUser(getContentResolver(),
                 Settings.System.STATUS_BAR_SHOW_TICKER,
                 val, UserHandle.USER_CURRENT);
    }

    private void updateEnabledState() {
        mPrefsTickContainer.setVisibility(getUserTickerState() ? View.VISIBLE : View.GONE);
        mDisabledTickText.setVisibility(getUserTickerState() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEnablerChanged(boolean isEnabled) {
        setUserTickerState(getUserTickerState() ? 1 : 0);
        updateEnabledState();
    }
}