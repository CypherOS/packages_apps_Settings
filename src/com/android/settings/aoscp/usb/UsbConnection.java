/*
 *  Copyright (C) 2017 CypherOS
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
package com.android.settings.aoscp.usb;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbManager;
import android.net.NetworkUtils;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.preference.SystemSettingSwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class UsbConnection extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {
			
    private static final String TAG = "UsbConnection";

    private static final String USB_CONFIGURATION_KEY = "select_usb_configuration";
	private static final String DEBUG_DEBUGGING_CATEGORY_KEY = "debug_debugging_category";
	
	// ADB Debugging
	private static final String ENABLE_ADB = "enable_adb";
    private static final String ADB_NOTIFY = "adb_notify";
    private static final String ADB_TCPIP = "adb_over_network";
    private static final String CLEAR_ADB_KEYS = "clear_adb_keys";

    private ListPreference mUsbConfiguration;
	
	// ADB Debugging
	private SwitchPreference mEnableAdb;
    private SwitchPreference mAdbNotify;
    private SwitchPreference mAdbOverNetwork;
    private Preference mClearAdbKeys;
	
	private UserManager mUm;
	// To track whether a confirmation dialog was clicked.
    private boolean mDialogClicked;
	
	private Dialog mAdbDialog;
    private Dialog mAdbTcpDialog;
    private Dialog mAdbKeysDialog;
	
	private final ArrayList<Preference> mAllPrefs = new ArrayList<Preference>();
	private final ArrayList<SwitchPreference> mResetSwitchPrefs
            = new ArrayList<SwitchPreference>();
	private final HashSet<Preference> mDisabledPrefs = new HashSet<Preference>();

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DEVICEINFO_STORAGE;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.usb_connection);

        mUsbConfiguration = (ListPreference) findPreference(USB_CONFIGURATION_KEY);
		
		// ADB Debugging
		final PreferenceGroup debugDebuggingCategory = (PreferenceGroup)
                findPreference(DEBUG_DEBUGGING_CATEGORY_KEY);
        mEnableAdb = findAndInitSwitchPref(ENABLE_ADB);
        mAdbNotify = findAndInitSwitchPref(ADB_NOTIFY);
        mAdbOverNetwork = findAndInitSwitchPref(ADB_TCPIP);
        mClearAdbKeys = findPreference(CLEAR_ADB_KEYS);
        if (!SystemProperties.getBoolean("ro.adb.secure", false)) {
            if (debugDebuggingCategory != null) {
                debugDebuggingCategory.removePreference(mClearAdbKeys);
            }
        }
        mAllPrefs.add(mClearAdbKeys);
		
		if (!mUm.isAdminUser()) {
            disableForUser(mEnableAdb);
            disableForUser(mClearAdbKeys);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        if (getActivity().registerReceiver(mUsbReceiver, filter) == null) {
            updateUsbConfigurationValues();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mUsbReceiver);
    }
	
	private void disableForUser(Preference pref) {
        if (pref != null) {
            pref.setEnabled(false);
            mDisabledPrefs.add(pref);
        }
    }
	
	private SwitchPreference findAndInitSwitchPref(String key) {
        SwitchPreference pref = (SwitchPreference) findPreference(key);
        if (pref == null) {
            throw new IllegalArgumentException("Cannot find preference with key = " + key);
        }
        mAllPrefs.add(pref);
        mResetSwitchPrefs.add(pref);
        return pref;
    }
	
	@Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mEnableAdb) {
            if (mEnableAdb.isChecked()) {
                mDialogClicked = false;
                if (mAdbDialog != null) {
                    dismissDialogs();
                }
                mAdbDialog = new AlertDialog.Builder(getActivity()).setMessage(
                        getActivity().getResources().getString(R.string.adb_warning_message))
                        .setTitle(R.string.adb_warning_title)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show();
                mAdbDialog.setOnDismissListener(this);
            } else {
                Settings.Global.putInt(getActivity().getContentResolver(),
                        Settings.Global.ADB_ENABLED, 0);
            }
        } else if (preference == mAdbNotify) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ADB_NOTIFY,
                    mAdbNotify.isChecked() ? 1 : 0);
        } else if (preference == mAdbOverNetwork) {
            if (mAdbOverNetwork.isChecked()) {
                if (mAdbTcpDialog != null) {
                    dismissDialogs();
                }
                mAdbTcpDialog = new AlertDialog.Builder(getActivity()).setMessage(
                        getResources().getString(R.string.adb_over_network_warning))
                        .setTitle(R.string.adb_over_network)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show();
                mAdbTcpDialog.setOnDismissListener(this);
            } else {
                Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.ADB_PORT, -1);
                updateAdbOverNetwork();
            }
        } else if (preference == mClearAdbKeys) {
            if (mAdbKeysDialog != null) dismissDialogs();
            mAdbKeysDialog = new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.adb_keys_warning_message)
                        .setPositiveButton(android.R.string.ok, this)
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
        } else {
            return super.onPreferenceTreeClick(preference);
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mUsbConfiguration) {
            writeUsbConfigurationOption(newValue);
            return true;
        }
        return false;
    }

    private void updateUsbConfigurationValues() {
        if (mUsbConfiguration != null) {
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

            String[] values = getResources().getStringArray(R.array.usb_configuration_values);
            String[] titles = getResources().getStringArray(R.array.usb_configuration_titles);
            int index = 0;
            for (int i = 0; i < titles.length; i++) {
                if (manager.isFunctionEnabled(values[i])) {
                    index = i;
                    break;
                }
            }
            mUsbConfiguration.setValue(values[index]);
            mUsbConfiguration.setSummary(titles[index]);
            mUsbConfiguration.setOnPreferenceChangeListener(this);
        }
    }

    private void writeUsbConfigurationOption(Object newValue) {
        UsbManager manager = (UsbManager)getActivity().getSystemService(Context.USB_SERVICE);
        String function = newValue.toString();
        if (function.equals("none")) {
            manager.setCurrentFunction(function, false);
        } else {
            manager.setCurrentFunction(function, true);
        }
    }
	
	void updateSwitchPreference(SwitchPreference switchPreference, boolean value) {
        switchPreference.setChecked(value);
    }
	
	private void updateAllOptions() {
        final Context context = getActivity();
        final ContentResolver cr = context.getContentResolver();
        updateSwitchPreference(mEnableAdb, Settings.Global.getInt(cr,
                Settings.Global.ADB_ENABLED, 0) != 0);
        updateSwitchPreference(mAdbNotify, Settings.Secure.getInt(cr,
                Settings.Secure.ADB_NOTIFY, 1) != 0);
        updateAdbOverNetwork();
    }
	
	private void updateAdbOverNetwork() {
        int port = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.ADB_PORT, 0);
        boolean enabled = port > 0;

        updateSwitchPreference(mAdbOverNetwork, enabled);

        WifiInfo wifiInfo = null;

        if (enabled) {
            IWifiManager wifiManager = IWifiManager.Stub.asInterface(
                    ServiceManager.getService(Context.WIFI_SERVICE));
            try {
                wifiInfo = wifiManager.getConnectionInfo();
            } catch (RemoteException e) {
                Log.e(TAG, "wifiManager, getConnectionInfo()", e);
            }
        }

        if (wifiInfo != null) {
            String hostAddress = NetworkUtils.intToInetAddress(
                    wifiInfo.getIpAddress()).getHostAddress();
            mAdbOverNetwork.setSummary(hostAddress + ":" + String.valueOf(port));
        } else {
            mAdbOverNetwork.setSummary(R.string.adb_over_network_summary);
        }
    }
	
	private void dismissDialogs() {
        if (mAdbDialog != null) {
            mAdbDialog.dismiss();
            mAdbDialog = null;
        }
        if (mAdbTcpDialog != null) {
            mAdbTcpDialog.dismiss();
            mAdbTcpDialog = null;
        }
        if (mAdbKeysDialog != null) {
            mAdbKeysDialog.dismiss();
            mAdbKeysDialog = null;
        }
    }
	
	public void onClick(DialogInterface dialog, int which) {
        if (dialog == mAdbDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                mDialogClicked = true;
                Settings.Global.putInt(getActivity().getContentResolver(),
                        Settings.Global.ADB_ENABLED, 1);
            }
        } else if (dialog == mAdbTcpDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.ADB_PORT, 5555);
            }
        } else if (dialog == mAdbKeysDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                try {
                    IBinder b = ServiceManager.getService(Context.USB_SERVICE);
                    IUsbManager service = IUsbManager.Stub.asInterface(b);
                    service.clearUsbDebuggingKeys();
                } catch (RemoteException e) {
                    Log.e(TAG, "Unable to clear adb keys", e);
                }
            }
        }
    }
	
	public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
        if (dialog == mAdbDialog) {
            if (!mDialogClicked) {
                mEnableAdb.setChecked(false);
            }
            mAdbDialog = null;
        } else if (dialog == mAdbTcpDialog) {
            updateAdbOverNetwork();
            mAdbTcpDialog = null;
        }
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUsbConfigurationValues();
       }
    };
}