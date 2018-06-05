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

package com.android.settings.aoscp.sim;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Utils;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import org.codeaurora.internal.IExtTelephony;

import java.lang.NoClassDefFoundError;
import java.util.ArrayList;
import java.util.List;

public class SimManagement extends RestrictedSettingsFragment implements Indexable {
	
    private static final String TAG = "SimManagement";
    private static final boolean DEBUG = false;

	private static final String KEY_SIM_HEADER = "sim_header";
    private static final String DISALLOW_CONFIG_SIM = "no_config_sim";
	
	private Context mContext;
	
	private ImageView mSimImage1;
	private ImageView mSimImage2;
	private LayoutPreference mSimHeader;

    public SimManagement() {
        super(DISALLOW_CONFIG_SIM);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.SIM;
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        mContext = getContext();
        addPreferencesFromResource(R.xml.sim_management_settings);

        mScreen = getPreferenceScreen();
		updateSimHeader();
    }
	
	@Override
    public void onResume() {
        super.onResume();
        updateSimHeader();
    }

	private void updateSimHeader() {
        if (mContext == null) {
            return;
        }
        mSimHeader = (LayoutPreference) findPreference(KEY_SIM_HEADER);
        mSimImage1 = (ImageView) mSimHeader.findViewById(R.id.sim_1);
		mSimImage2 = (ImageView) mSimHeader.findViewById(R.id.sim_2);

		mSimImage1.setDrawable(R.drawable.ic_settings_sim);
		mSimImage2.setDrawable(R.drawable.ic_settings_sim);
    }
	
	/**
     * For search
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    if (Utils.showSimCardTile(context)) {
                        SearchIndexableResource sir = new SearchIndexableResource(context);
                        sir.xmlResId = R.xml.sim_management_settings;
                        result.add(sir);
                    }

                    return result;
                }
            };
}
