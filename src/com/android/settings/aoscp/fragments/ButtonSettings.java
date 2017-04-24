/*
 *  Copyright (C) 2017 CypherOS
 *  Copyright (C) 2014 The CyanogenMod Project
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.android.settings.aoscp.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.Context;
import android.media.AudioSystem;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.util.aoscp.AoscpUtils;

import org.aoscp.framework.internal.utils.DeviceUtils;

public class ButtonSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
	
	// Button Categories
    private static final String CATEGORY_HOME = "home_key";
	private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";

    // Hardware key configurations
    private static final String KEY_SHOW_NAVIGATION = "navigation_bar";
    private static final String KEY_HW_KEYS_DISABLE = "hardware_keys";
	private static final String KEY_BUTTON_BRIGHTNESS = "button_brightness";
	
	// Hardware Key Rebindings
    private static final String KEY_HOME_LONG_PRESS = "home_long_press";
    private static final String KEY_HOME_DOUBLE_TAP = "home_double_tap";
    private static final String KEY_MENU_PRESS = "menu_press";
    private static final String KEY_MENU_LONG_PRESS = "menu_long_press";
    private static final String KEY_ASSIST_PRESS = "assist_press";
    private static final String KEY_ASSIST_LONG_PRESS = "assist_long_press";
    private static final String KEY_APP_SWITCH_PRESS = "app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "app_switch_long_press";
	
	// General Features
	private static final String KEY_VOLUME_WAKE_SCREEN = "volume_wake_screen";
	private static final String KEY_VOLUME_MUSIC_CONTROLS = "volbtn_music_controls";
	private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
	private static final String KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons";

	// Available custom actions to perform on a key press.
    // Must match values for KEY_HOME_LONG_PRESS_ACTION in:
    // frameworks/base/core/java/android/provider/Settings.java
    private static final int ACTION_NOTHING = 0;
    private static final int ACTION_MENU = 1;
    private static final int ACTION_APP_SWITCH = 2;
    private static final int ACTION_SEARCH = 3;
    private static final int ACTION_VOICE_SEARCH = 4;
    private static final int ACTION_IN_APP_SEARCH = 5;
    private static final int ACTION_SLEEP = 7;
    private static final int ACTION_LAST_APP = 8;

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
	
	private Map<String, Integer> mKeyPrefs = new HashMap<String, Integer>();
	
    private PreferenceCategory mHomeCategory;
	private PreferenceCategory mBackCategory;
    private PreferenceCategory mMenuCategory;
    private PreferenceCategory mAppSwitchCategory;
    private PreferenceCategory mAssistCategory;
	
	private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;
	
	private ListPreference mVolumeKeyCursorControl;
	private SwitchPreference mVolumeMusicControls;
	private SwitchPreference mVolumeWakeScreen;
	private SwitchPreference mSwapVolumeButtons;
	
	
	private SwitchPreference mNavigationBar;
    private SwitchPreference mDisableHwKeys;
    private Preference mButtonBrightness;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ADDITIONS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.button_settings);

        final ContentResolver resolver = getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();

        final int deviceHwKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
				
        final boolean hasHomeKey = (deviceHwKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceHwKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceHwKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceHwKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceHwKeys & KEY_MASK_APP_SWITCH) != 0;

        mHomeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
		mBackCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
        mMenuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        mAssistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
        mAppSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);
				
		mNavigationBar = (SwitchPreference) prefScreen.findPreference(KEY_SHOW_NAVIGATION);
        mDisableHwKeys = (SwitchPreference) prefScreen.findPreference(KEY_HW_KEYS_DISABLE);
		mButtonBrightness = (Preference) prefScreen.findPreference(KEY_BUTTON_BRIGHTNESS);

        if (deviceHwKeys == 0) {
            prefScreen.removePreference(mHomeCategory);
			prefScreen.removePreference(mBackCategory);
            prefScreen.removePreference(mMenuCategory);
            prefScreen.removePreference(mAssistCategory);
            prefScreen.removePreference(mAppSwitchCategory);
			prefScreen.removePreference(mNavigationBar);
			prefScreen.removePreference(mDisableHwKeys);
			prefScreen.removePreference(mButtonBrightness);
        } else {
            mHomeLongPressAction = (ListPreference) prefScreen.findPreference(
                    KEY_HOME_LONG_PRESS);
            mHomeDoubleTapAction = (ListPreference) prefScreen.findPreference(
                    KEY_HOME_DOUBLE_TAP);
            mMenuPressAction = (ListPreference) prefScreen.findPreference(
                    KEY_MENU_PRESS);
            mMenuLongPressAction = (ListPreference) prefScreen.findPreference(
                    KEY_MENU_LONG_PRESS);
            mAssistPressAction = (ListPreference) prefScreen.findPreference(
                    KEY_ASSIST_PRESS);
            mAssistLongPressAction = (ListPreference) prefScreen.findPreference(
                    KEY_ASSIST_LONG_PRESS);
            mAppSwitchPressAction = (ListPreference) prefScreen.findPreference(
                    KEY_APP_SWITCH_PRESS);
            mAppSwitchLongPressAction = (ListPreference) prefScreen.findPreference(
                    KEY_APP_SWITCH_LONG_PRESS);

            if (hasHomeKey) {

                int homeLongPressAction;
                int longPressOnHomeBehavior = res.getInteger(
                        com.android.internal.R.integer.config_longPressOnHomeBehavior);

                if (longPressOnHomeBehavior == 1) {
                    longPressOnHomeBehavior = ACTION_APP_SWITCH;
                } else if (longPressOnHomeBehavior == 2) {
                    longPressOnHomeBehavior = ACTION_SEARCH;
                } else {
                    longPressOnHomeBehavior = ACTION_NOTHING;
                }

                if (hasAppSwitchKey) {
                    homeLongPressAction = Settings.System.getInt(resolver,
                            Settings.System.KEY_HOME_LONG_PRESS_ACTION, ACTION_NOTHING);
                } else {
                    int defaultAction = ACTION_NOTHING;
                    homeLongPressAction = Settings.System.getInt(resolver,
                            Settings.System.KEY_HOME_LONG_PRESS_ACTION, longPressOnHomeBehavior);
                }
                mHomeLongPressAction.setValue(Integer.toString(homeLongPressAction));
                mHomeLongPressAction.setSummary(mHomeLongPressAction.getEntry());
                mHomeLongPressAction.setOnPreferenceChangeListener(this);

                mKeyPrefs.put(Settings.System.KEY_HOME_LONG_PRESS_ACTION, homeLongPressAction);

                int doubleTapOnHomeBehavior = res.getInteger(
                        com.android.internal.R.integer.config_doubleTapOnHomeBehavior);

                int homeDoubleTapAction = Settings.System.getInt(resolver,
                            Settings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                            doubleTapOnHomeBehavior == 1 ? ACTION_APP_SWITCH : ACTION_NOTHING);

                mHomeDoubleTapAction.setValue(Integer.toString(homeDoubleTapAction));
                mHomeDoubleTapAction.setSummary(mHomeDoubleTapAction.getEntry());
                mHomeDoubleTapAction.setOnPreferenceChangeListener(this);

                mKeyPrefs.put(Settings.System.KEY_HOME_DOUBLE_TAP_ACTION, homeDoubleTapAction);

            } else {
                prefScreen.removePreference(mHomeCategory);
            }

            if (hasMenuKey) {
                int menuPressAction = Settings.System.getInt(resolver,
                        Settings.System.KEY_MENU_ACTION, ACTION_MENU);
                mMenuPressAction.setValue(Integer.toString(menuPressAction));
                mMenuPressAction.setSummary(mMenuPressAction.getEntry());
                mMenuPressAction.setOnPreferenceChangeListener(this);

                mKeyPrefs.put(Settings.System.KEY_MENU_ACTION, menuPressAction);

                int menuLongPressAction = ACTION_NOTHING;
                if (!hasAssistKey) {
                    menuLongPressAction = ACTION_SEARCH;
                }

                menuLongPressAction = Settings.System.getInt(resolver,
                            Settings.System.KEY_MENU_LONG_PRESS_ACTION, menuLongPressAction);

                mMenuLongPressAction.setValue(Integer.toString(menuLongPressAction));
                mMenuLongPressAction.setSummary(mMenuLongPressAction.getEntry());
                mMenuLongPressAction.setOnPreferenceChangeListener(this);

                mKeyPrefs.put(Settings.System.KEY_MENU_LONG_PRESS_ACTION, menuLongPressAction);
            } else {
                prefScreen.removePreference(mMenuCategory);
            }

            if (hasAssistKey) {
                int assistPressAction = Settings.System.getInt(resolver,
                        Settings.System.KEY_ASSIST_ACTION, ACTION_SEARCH);
                mAssistPressAction.setValue(Integer.toString(assistPressAction));
                mAssistPressAction.setSummary(mAssistPressAction.getEntry());
                mAssistPressAction.setOnPreferenceChangeListener(this);

                mKeyPrefs.put(Settings.System.KEY_ASSIST_ACTION, assistPressAction);

                int assistLongPressAction = Settings.System.getInt(resolver,
                        Settings.System.KEY_ASSIST_LONG_PRESS_ACTION, ACTION_VOICE_SEARCH);
                mAssistLongPressAction.setValue(Integer.toString(assistLongPressAction));
                mAssistLongPressAction.setSummary(mAssistLongPressAction.getEntry());
                mAssistLongPressAction.setOnPreferenceChangeListener(this);

                mKeyPrefs.put(Settings.System.KEY_ASSIST_LONG_PRESS_ACTION, assistLongPressAction);
            } else {
                prefScreen.removePreference(mAssistCategory);
            }

            if (hasAppSwitchKey) {
                int appSwitchPressAction = Settings.System.getInt(resolver,
                        Settings.System.KEY_APP_SWITCH_ACTION, ACTION_APP_SWITCH);
                mAppSwitchPressAction.setValue(Integer.toString(appSwitchPressAction));
                mAppSwitchPressAction.setSummary(mAppSwitchPressAction.getEntry());
                mAppSwitchPressAction.setOnPreferenceChangeListener(this);

                mKeyPrefs.put(Settings.System.KEY_APP_SWITCH_ACTION, appSwitchPressAction);

                int appSwitchLongPressAction = Settings.System.getInt(resolver,
                        Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, ACTION_NOTHING);
                mAppSwitchLongPressAction.setValue(Integer.toString(appSwitchLongPressAction));
                mAppSwitchLongPressAction.setSummary(mAppSwitchLongPressAction.getEntry());
                mAppSwitchLongPressAction.setOnPreferenceChangeListener(this);

                mKeyPrefs.put(Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, appSwitchLongPressAction);
            } else {
                prefScreen.removePreference(mAppSwitchCategory);
            }
        }
		
		boolean showNavBarDefault = AoscpUtils.deviceSupportNavigationBar(getActivity());
        boolean showNavBar = Settings.System.getInt(resolver,
                    Settings.System.NAVIGATION_BAR_SHOW, showNavBarDefault ? 1:0) == 1;
        mNavigationBar.setChecked(showNavBar);

        boolean hardwareKeysDisable = Settings.System.getInt(resolver,
                    Settings.System.HARDWARE_KEYS_DISABLE, 0) == 1;
        mDisableHwKeys.setChecked(hardwareKeysDisable);
			
	    updateDisableHWKeyEnablement(hardwareKeysDisable);
		
		mVolumeKeyCursorControl = (ListPreference) findPreference(KEY_VOLUME_KEY_CURSOR_CONTROL);
	    mVolumeKeyCursorControl.setOnPreferenceChangeListener(this);
        int cursorControlAction = Settings.System.getInt(resolver,
                Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
        mVolumeKeyCursorControl.setValue(String.valueOf(cursorControlAction));
        updateCursorActionSummary(cursorControlAction);
			
        mVolumeWakeScreen = (SwitchPreference) findPreference(KEY_VOLUME_WAKE_SCREEN);
	    mVolumeMusicControls = (SwitchPreference) findPreference(KEY_VOLUME_MUSIC_CONTROLS);
			
	    if (mVolumeWakeScreen != null) {
            if (mVolumeMusicControls != null) {
                mVolumeMusicControls.setDependency(KEY_VOLUME_WAKE_SCREEN);
                mVolumeWakeScreen.setDisableDependentsState(true);
            }
        }
			
        int swapVolumeKeys = Settings.System.getInt(getContentResolver(),
                Settings.System.SWAP_VOLUME_KEYS_ON_ROTATION, 0);
        mSwapVolumeButtons = (SwitchPreference)
                prefScreen.findPreference(KEY_SWAP_VOLUME_BUTTONS);
        if (mSwapVolumeButtons != null) {
            mSwapVolumeButtons.setChecked(swapVolumeKeys > 0);
        }
    }
	
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mNavigationBar) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW, checked ? 1:0);
            // remove hw button disable if we disable navbar
            if (!checked) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.HARDWARE_KEYS_DISABLE, 0);
                mDisableHwKeys.setChecked(false);
            }
            return true;
        } else if (preference == mDisableHwKeys) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HARDWARE_KEYS_DISABLE, checked ? 1:0);
            updateDisableHWKeyEnablement(checked);
            return true;
		} else if (preference == mSwapVolumeButtons) {
            int value = mSwapVolumeButtons.isChecked()
                    ? (DeviceUtils.isTablet(getActivity()) ? 2 : 1) : 0;
            if (value == 2) {
                Display defaultDisplay = getActivity().getWindowManager().getDefaultDisplay();

                DisplayInfo displayInfo = new DisplayInfo();
                defaultDisplay.getDisplayInfo(displayInfo);

                // Not all tablets are landscape
                if (displayInfo.getNaturalWidth() < displayInfo.getNaturalHeight()) {
                    value = 1;
                }
            }
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SWAP_VOLUME_KEYS_ON_ROTATION, value);
        }
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
		ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mHomeLongPressAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mHomeLongPressAction.findIndexOfValue((String) newValue);
            mHomeLongPressAction.setSummary(
                    mHomeLongPressAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION, value);
            mKeyPrefs.put(Settings.System.KEY_HOME_LONG_PRESS_ACTION, value);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mHomeDoubleTapAction.findIndexOfValue((String) newValue);
            mHomeDoubleTapAction.setSummary(
                    mHomeDoubleTapAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.KEY_HOME_DOUBLE_TAP_ACTION, value);
            mKeyPrefs.put(Settings.System.KEY_HOME_DOUBLE_TAP_ACTION, value);
            return true;
        } else if (preference == mMenuPressAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mMenuPressAction.findIndexOfValue((String) newValue);
            mMenuPressAction.setSummary(
                    mMenuPressAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.KEY_MENU_ACTION, value);
            mKeyPrefs.put(Settings.System.KEY_MENU_ACTION, value);
            return true;
        } else if (preference == mMenuLongPressAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mMenuLongPressAction.findIndexOfValue((String) newValue);
            mMenuLongPressAction.setSummary(
                    mMenuLongPressAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.KEY_MENU_LONG_PRESS_ACTION, value);
            mKeyPrefs.put(Settings.System.KEY_MENU_LONG_PRESS_ACTION, value);
            return true;
        } else if (preference == mAssistPressAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mAssistPressAction.findIndexOfValue((String) newValue);
            mAssistPressAction.setSummary(
                    mAssistPressAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.KEY_ASSIST_ACTION, value);
            mKeyPrefs.put(Settings.System.KEY_ASSIST_ACTION, value);
            return true;
        } else if (preference == mAssistLongPressAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mAssistLongPressAction.findIndexOfValue((String) newValue);
            mAssistLongPressAction.setSummary(
                    mAssistLongPressAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION, value);
            mKeyPrefs.put(Settings.System.KEY_ASSIST_LONG_PRESS_ACTION, value);
            return true;
        } else if (preference == mAppSwitchPressAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mAppSwitchPressAction.findIndexOfValue((String) newValue);
            mAppSwitchPressAction.setSummary(
                    mAppSwitchPressAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.KEY_APP_SWITCH_ACTION, value);
            mKeyPrefs.put(Settings.System.KEY_APP_SWITCH_ACTION, value);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mAppSwitchLongPressAction.findIndexOfValue((String) newValue);
            mAppSwitchLongPressAction.setSummary(
                    mAppSwitchLongPressAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, value);
            mKeyPrefs.put(Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, value);
            return true;
		} else if (preference == mVolumeKeyCursorControl) {
            int cursorAction = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver, Settings.System.VOLUME_KEY_CURSOR_CONTROL,
                    cursorAction);
            updateCursorActionSummary(cursorAction);
            return true;
        }
        return false;
    }
	
	private void updateCursorActionSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // quick pulldown deactivated
            mVolumeKeyCursorControl.setSummary(res.getString(R.string.volbtn_cursor_control_off));
        } else if (value == 1) {
            // quick pulldown always
            mVolumeKeyCursorControl.setSummary(res.getString(R.string.volbtn_cursor_control_on));
		} else if (value == 2) {
            // quick pulldown always
            mVolumeKeyCursorControl.setSummary(res.getString(R.string.volbtn_cursor_control_on_reverse));
        } else {
            String direction = res.getString(value == 1
                    ? R.string.volbtn_cursor_control_on
                    : R.string.volbtn_cursor_control_on_reverse);
            mVolumeKeyCursorControl.setSummary(res.getString(R.string.volbtn_cursor_control_title_summary, direction));
        }
	}

    private void updateDisableHWKeyEnablement(boolean hardwareKeysDisable) {
        mButtonBrightness.setEnabled(!hardwareKeysDisable);
        mHomeCategory.setEnabled(!hardwareKeysDisable);
		mBackCategory.setEnabled(!hardwareKeysDisable);
        mMenuCategory.setEnabled(!hardwareKeysDisable);
        mAppSwitchCategory.setEnabled(!hardwareKeysDisable);
        mAssistCategory.setEnabled(!hardwareKeysDisable);
    }
}