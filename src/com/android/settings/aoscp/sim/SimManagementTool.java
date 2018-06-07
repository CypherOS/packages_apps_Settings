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

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.aoscp.widget.SimPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.RadioButtonPreference;
import com.android.settingslib.aoscp.FooterConfirm;
import com.android.settingslib.aoscp.FooterConfirm.onActionClickListener;
import com.android.settingslib.aoscp.FooterConfirmMixin;

import org.codeaurora.internal.IExtTelephony;

import java.util.ArrayList;
import java.util.List;

public class SimManagementTool extends SettingsPreferenceFragment implements Indexable {
	
    private static final String TAG = "SimManagementTool";
    private static final boolean DEBUG = false;
	
	private static final String KEY_COLOR_TEAL = "color_teal";
    private static final String KEY_COLOR_BLUE = "color_blue";
    private static final String KEY_COLOR_INDIGO = "color_indigo";
    private static final String KEY_COLOR_PURPLE = "color_purple";
	private static final String KEY_COLOR_PINK = "color_pink";
	private static final String KEY_COLOR_RED = "color_red";
	
	List<RadioButtonPreference> mColors = new ArrayList<>();
	
	private static Context mContext;
	private int mSlotId;
	
	private PreferenceScreen mScreen;
	private SubscriptionInfo mSubInfoRecord;
	private SubscriptionManager mSubscriptionManager;
	
	private static SimPreference mSimStatus;

	public SimManagementTool() {
    }

    public SimManagementTool(Context context, int slotId) {
		mContext = context;
		mSlotId = slotId;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.SIM;
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        mContext = getContext();
        addPreferencesFromResource(R.xml.sim_management_tool_settings);
		mScreen = getPreferenceScreen();

		mSubscriptionManager = SubscriptionManager.from(mContext);
		mSubInfoRecord = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(mSlotId);
		mScreen.setTitle(String.format(mContext.getResources().getString(
		            R.string.sim_management_tool_title), mSubInfoRecord.getCarrierName()));
		mSimStatus = new SimPreference(getPrefContext(), mSubInfoRecord);
		mScreen.addPreference(mSimStatus);
		
		for (int i = 0; i < mScreen.getPreferenceCount(); i++) {
            Preference pref = mScreen.getPreference(i);
            if (pref instanceof RadioButtonPreference) {
                RadioButtonPreference colorPref = (RadioButtonPreference) pref;
                colorPref.setOnClickListener(this);
                mColors.add(colorPref);
            }
        }
		
		switch (mSubInfoRecord.getIconTint()) {
            case 0:
                updateColorItems(KEY_COLOR_TEAL);
                break;
            case 1:
                updateColorItems(KEY_COLOR_BLUE);
                break;
            case 2:
                updateColorItems(KEY_COLOR_INDIGO);
                break;
            case 3:
                updateColorItems(KEY_COLOR_PURPLE);
                break;
			case 4:
                updateColorItems(KEY_COLOR_PINK);
                break;
			case 5:
                updateColorItems(KEY_COLOR_RED);
                break;
        }
		
		updateSubscriptionState();
    }

	private void updateSubscriptionState() {
		mSimStatus.setSlotId(mSlotId);
		mSimStatus.update();
    }
	
	private void updateColorItems(String selectionKey) {
        for (RadioButtonPreference pref : mColors) {
            if (selectionKey.equals(pref.getKey())) {
                pref.setChecked(true);
            } else {
                pref.setChecked(false);
            }
        }
    }

	@Override
    public void onPause() {
        super.onPause();
		((SimPreference)mSimStatus).cleanUpPendingDialogs();
    }
	
	@Override
    public void onResume() {
        super.onResume();
		updateSubscriptionState();
    }
	
	public static void showDialog(boolean confirm, String msg, String action) {
		FooterConfirmMixin.show(FooterConfirm.with(mContext)
            .setMessage(msg)
            .setAction(true)
            .setActionTitle(action)
            .setActionListener(new onActionClickListener() {
                @Override
                public void onActionClicked(FooterConfirm footerConfirm) {
                    if (confirm) {
						mSimStatus.sendUiccProvisioningRequest();
					} else {
						mSimStatus.update();
					}
                }
            }));
		
	}
	
	public static void showProgressDialog(String msg) {
		FooterConfirmMixin.show(FooterConfirm.with(mContext)
            .setMessage(msg));
	}

	public static void showSuccessDialog(String msg) {
		FooterConfirmMixin.show(FooterConfirm.with(mContext)
            .setMessage(msg));
	}
	
	public static void dismissDialog() {
		FooterConfirmMixin.dismiss();
	}
	
	@Override
    public void onRadioButtonClicked(RadioButtonPreference pref) {
        switch (pref.getKey()) {
            case KEY_COLOR_TEAL:
                updateSimColor(0);
                break;
            case KEY_COLOR_BLUE:
                updateSimColor(1);
                break;
            case KEY_COLOR_INDIGO:
                updateSimColor(2);
                break;
            case KEY_COLOR_PURPLE:
                updateSimColor(3);
                break;
			case KEY_COLOR_PINK:
                updateSimColor(4);
                break;
			case KEY_COLOR_RED:
                updateSimColor(5);
                break;
        }
        updateColorItems(pref.getKey());
    }

	private void updateSimColor(int color) {
		int subscriptionId = mSubInfoRecord.getSubscriptionId();
		mSubInfoRecord.setIconTint(color);
        mSubscriptionManager.setIconTint(color, subscriptionId);
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
                        sir.xmlResId = R.xml.sim_management_tool_settings;
                        result.add(sir);
                    }

                    return result;
                }
            };
}
