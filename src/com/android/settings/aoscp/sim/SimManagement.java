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

import android.annotation.ColorInt;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.telecom.PhoneAccount;
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
import android.widget.ImageView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.TextView;

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
import java.util.Iterator;
import java.util.List;

public class SimManagement extends RestrictedSettingsFragment implements Indexable {
	
    private static final String TAG = "SimManagement";
    private static final boolean DEBUG = false;

	private static final String KEY_SIM_HEADER = "sim_header";
    private static final String DISALLOW_CONFIG_SIM = "no_config_sim";
	private static final String SETTING_USER_PREF_DATA_SUB = "user_preferred_data_sub";

	private static final String KEY_SIM_DATA = "sim_data";
    private static final String KEY_SIM_CALLS = "sim_calls";
    private static final String KEY_SIM_SMS = "sim_sms";
	
	private static final int NOT_PROVISIONED = 0;
	private static final int PROVISIONED = 1;
	private static final int INVALID_STATE = -1;
	
	public static final int DIALOG_DATA = 0;
    public static final int DIALOG_CALLS = 1;
    public static final int DIALOG_SMS = 2;

	private Context mContext;
	private FragmentManager mFragmentManager;
	
	private IExtTelephony mExtTelephony;
	private SubscriptionManager mSubscriptionManager;
	
	private Drawable mSimCard;
	private Drawable mSimCardDisabled;
	
	private ImageView mSlot1;
	private ImageView mSlot2;
	private TextView mSlot1Carrier;
	private TextView mSlot2Carrier;
	
	private LayoutPreference mSimHeader;
	private PreferenceScreen mScreen;
	
	private Preference mSimDataPreference;
	private Preference mSimCallsPreference;
	private Preference mSimSmsPreference;
	
	private List<SubscriptionInfo> mAvailableSubInfos = null;
    private List<SubscriptionInfo> mSubInfoList = null;
	private List<SubscriptionInfo> mSelectableSubInfos = null;
	private int mNumSlots;
	private int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
    private int[] mCallState = new int[mPhoneCount];
	private PhoneStateListener[] mPhoneStateListener = new PhoneStateListener[mPhoneCount];
	private int[] mUiccProvisionStatus = new int[mPhoneCount];
	
	private static final String ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED =
            "org.codeaurora.intent.action.ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED";
	private static final String EXTRA_NEW_PROVISION_STATE = "newProvisionState";

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
		
		final Bundle extras = getIntent().getExtras();
		final TelephonyManager tm =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		try {
            mExtTelephony = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
        } catch (NoClassDefFoundError ex) {
            // ignore, device does not compile telephony-ext.
        }
		mSubscriptionManager = SubscriptionManager.from(mContext);
		
		mNumSlots = tm.getSimCount();
		mAvailableSubInfos = new ArrayList<SubscriptionInfo>(mNumSlots);
		mSelectableSubInfos = new ArrayList<SubscriptionInfo>();
		
		mSimCard = mContext.getResources().getDrawable(R.drawable.ic_settings_sim);
		mSimCard.setTint(Utils.getColorAccent(mContext));
		mSimCardDisabled = mContext.getResources().getDrawable(R.drawable.ic_settings_sim_disabled);
		mSimCardDisabled.setTint(getColorAttr(mContext, android.R.attr.colorControlNormal));
		
		mSimDataPreference = mScreen.findPreference(KEY_SIM_DATA);
		mSimCallsPreference = mScreen.findPreference(KEY_SIM_CALLS);
		mSimSmsPreference = mScreen.findPreference(KEY_SIM_SMS);

		IntentFilter intentFilter = new IntentFilter(ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED);
		mContext.registerReceiver(mReceiver, intentFilter);
		
		if (mFragmentManager == null) {
            mFragmentManager = getFragmentManager();
        }
		
		updateSimHeader();
		updateAllOptions();
    }

	@Override
    public void onResume() {
        super.onResume();
		mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
        updateSubscriptions();
        final TelephonyManager tm =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (mSelectableSubInfos.size() > 1) {
            Log.d(TAG, "Register for call state change");
            for (int i = 0; i < mPhoneCount; i++) {
                int subId = mSelectableSubInfos.get(i).getSubscriptionId();
                tm.listen(getPhoneStateListener(i, subId),
                        PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
        updateSimHeader();
    }

	private void updateSimHeader() {
        if (mContext == null) {
            return;
        }
        mSimHeader = (LayoutPreference) findPreference(KEY_SIM_HEADER);
        mSlot1 = (ImageView) mSimHeader.findViewById(R.id.sim_1);
		mSlot2 = (ImageView) mSimHeader.findViewById(R.id.sim_2);
		
		mSlot1Carrier = (TextView) mSimHeader.findViewById(R.id.sim_1_carrier);
		mSlot2Carrier = (TextView) mSimHeader.findViewById(R.id.sim_2_carrier);
		
		updateSlots();
    }
	
	@Override
    public void onDestroy() {
        mContext.unregisterReceiver(mReceiver);
        Log.d(TAG,"on onDestroy");
        super.onDestroy();
    }
	
	private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener
            = new SubscriptionManager.OnSubscriptionsChangedListener() {
        @Override
        public void onSubscriptionsChanged() {
            //if (DBG) log("onSubscriptionsChanged:");
            if (isAdded()) {
                updateSubscriptions();
            }
        }
    };

	private void updateSubscriptions() {
        mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
        mAvailableSubInfos.clear();
        mSelectableSubInfos.clear();

        for (int i = 0; i < mNumSlots; ++i) {
            final SubscriptionInfo sir = mSubscriptionManager
                    .getActiveSubscriptionInfoForSimSlotIndex(i);
            mAvailableSubInfos.add(sir);
            if (sir != null && mUiccProvisionStatus[i] == PROVISIONED) {
                mSelectableSubInfos.add(sir);
            }
        }
        updateAllOptions();
    }
	
	public void updateSlot1ProvisionStatus() {
        //Log.d("Update Slot 1");
        if (mExtTelephony != null) {
            try {
                //get current provision state of the SIM.
                mUiccProvisionStatus[0] =
                        mExtTelephony.getCurrentUiccCardProvisioningStatus(0);
            } catch (RemoteException ex) {
                mUiccProvisionStatus[0] = INVALID_STATE;
                //Log.e("Failed to get pref, slotId: "+ 0 +" Exception: " + ex);
            }
        } else {
            // if we don't have telephony-ext, assume provisioned state
            mUiccProvisionStatus[0] = PROVISIONED;
        }
    }
	
	public void updateSlot2ProvisionStatus() {
        //Log.d("Update Slot 2");
        if (mExtTelephony != null) {
            try {
                //get current provision state of the SIM.
                mUiccProvisionStatus[1] =
                        mExtTelephony.getCurrentUiccCardProvisioningStatus(1);
            } catch (RemoteException ex) {
                mUiccProvisionStatus[1] = INVALID_STATE;
                //Log.e("Failed to get pref, slotId: "+ 1 +" Exception: " + ex);
            }
        } else {
            // if we don't have telephony-ext, assume provisioned state
            mUiccProvisionStatus[1] = PROVISIONED;
        }
    }
	
	private void updateAllOptions() {
		updateSlot1ProvisionStatus();
		updateSlot2ProvisionStatus();
		updateSimDataPreference();
		updateSimCallsPreference();
		updateSimSmsPreference();
	}
	
	private void updateSimDataPreference() {
        final SubscriptionInfo sir = mSubscriptionManager.getDefaultDataSubscriptionInfo();
        //if (DBG) log("[updateCellularDataValues] mSubInfoList=" + mSubInfoList);

        boolean callStateIdle = isCallStateIdle();
        final boolean ecbMode = SystemProperties.getBoolean(
                TelephonyProperties.PROPERTY_INECM_MODE, false);
        if (sir != null) {
            mSimDataPreference.setSummary(sir.getCarrierName());
            mSimDataPreference.setEnabled((mSelectableSubInfos.size() > 1) 
			        && callStateIdle && !ecbMode);
        } else if (sir == null) {
            mSimDataPreference.setSummary(R.string.sim_selection_required_pref);
            mSimDataPreference.setEnabled((mSelectableSubInfos.size() >= 1) 
			        && callStateIdle && !ecbMode);
        }
    }
	
	private void updateSimCallsPreference() {
        final TelecomManager telecomManager = TelecomManager.from(mContext);
        final PhoneAccountHandle phoneAccount =
            telecomManager.getUserSelectedOutgoingPhoneAccount();
        final List<PhoneAccountHandle> allPhoneAccounts =
            telecomManager.getCallCapablePhoneAccounts();

        mSimCallsPreference.setSummary(phoneAccount == null
                ? mContext.getResources().getString(R.string.sim_calls_ask_first_prefs_title)
                : (String)telecomManager.getPhoneAccount(phoneAccount).getLabel());
        mSimCallsPreference.setEnabled(allPhoneAccounts.size() > 1);
    }
	
	private void updateSimSmsPreference() {
        final SubscriptionInfo sir = mSubscriptionManager.getDefaultSmsSubscriptionInfo();
        //if (DBG) log("[updateSmsValues] mSubInfoList=" + mSubInfoList);

        if (sir != null) {
            mSimSmsPreference.setSummary(sir.getCarrierName());
            mSimSmsPreference.setEnabled(mSelectableSubInfos.size() > 1);
        } else if (sir == null) {
            mSimSmsPreference.setSummary(R.string.sim_selection_required_pref);
            mSimSmsPreference.setEnabled(mSelectableSubInfos.size() >= 1);
        }
    }

	@Override
    public void onPause() {
        super.onPause();
        mSubscriptionManager.removeOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        for (int i = 0; i < mPhoneCount; i++) {
            if (mPhoneStateListener[i] != null) {
                tm.listen(mPhoneStateListener[i], PhoneStateListener.LISTEN_NONE);
                mPhoneStateListener[i] = null;
            }
        }
    }
	
	private PhoneStateListener getPhoneStateListener(int phoneId, int subId) {
        // Disable Sim selection for Data when voice call is going on as changing the default data
        // sim causes a modem reset currently and call gets disconnected
        // ToDo : Add subtext on disabled preference to let user know that default data sim cannot
        // be changed while call is going on
        final int i = phoneId;
        mPhoneStateListener[phoneId]  = new PhoneStateListener(subId) {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                //if (DBG) log("PhoneStateListener.onCallStateChanged: state=" + state);
                mCallState[i] = state;
                updateSimDataPreference();
            }
        };
        return mPhoneStateListener[phoneId];
    }

	@Override
    public boolean onPreferenceTreeClick(final Preference pref) {
		if (pref == mSimDataPreference) {
			showOptions(mContext, DIALOG_DATA).show();
		} else if (pref == mSimCallsPreference) {
			showOptions(mContext, DIALOG_CALLS).show();
		} else if (pref == mSimSmsPreference) {
			showOptions(mContext, DIALOG_SMS).show();
		}
        return true;
    }

	private void updateSlots() {
		updateSlot1();
		updateSlot2();
	}

	private void updateSlot1() {
		final SubscriptionInfo sir = 
		        mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);
		if (sir != null) {
		    mSlot1.setImageDrawable(mSimCard);
		    mSlot1Carrier.setText(sir.getCarrierName());
			mSlot1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
					mFragmentManager.beginTransaction()
                            .replace(android.R.id.content, new SimManagementTool(mContext, 0))
                            .commit();
                }
            });
		} else {
			mSlot1.setImageDrawable(mSimCardDisabled);
            mSlot1Carrier.setText(R.string.sim_slot_empty);
        }
	}

	private void updateSlot2() {
		final SubscriptionInfo sir = 
		        mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1);
		if (sir != null) {
			mSlot2.setImageDrawable(mSimCard);
			mSlot2Carrier.setText(sir.getCarrierName());
			mSlot2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
					mFragmentManager.beginTransaction()
                            .replace(android.R.id.content, new SimManagementTool(mContext, 1))
                            .commit();
                }
            });
        } else {
			mSlot2.setImageDrawable(mSimCardDisabled);
            mSlot2Carrier.setText(R.string.sim_slot_empty);
        }
	}
	
	@ColorInt
    public static int getColorAttr(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        @ColorInt int colorAttr = ta.getColor(0, 0);
        ta.recycle();
        return colorAttr;
    }
	
	private boolean isCallStateIdle() {
        boolean callStateIdle = true;
        for (int i = 0; i < mCallState.length; i++) {
            if (TelephonyManager.CALL_STATE_IDLE != mCallState[i]) {
                callStateIdle = false;
            }
        }
        Log.d(TAG, "isCallStateIdle " + callStateIdle);
        return callStateIdle;
    }
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Intent received: " + action);
            if (ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED.equals(action)) {
                int phoneId = intent.getIntExtra(PhoneConstants.PHONE_KEY,
                        SubscriptionManager.INVALID_SUBSCRIPTION_ID);
                int newProvisionedState = intent.getIntExtra(EXTRA_NEW_PROVISION_STATE,
                        NOT_PROVISIONED);
                 updateSubscriptions();
                 Log.d(TAG, "Received ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED on phoneId: "
                         + phoneId + " new sub state " + newProvisionedState);
            }
        }
    };
	
	private Dialog showOptions(final Context context, final int id) {
		final ArrayList<String> list = new ArrayList<String>();
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        final List<SubscriptionInfo> subInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        final int selectableSubInfoLength = subInfoList == null ? 0 : subInfoList.size();

        final DialogInterface.OnClickListener selectionListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int value) {

                        final SubscriptionInfo sir;

                        switch (id) {
                            case DIALOG_DATA:
                                sir = subInfoList.get(value);
                                setDefaultDataSubId(context, sir.getSubscriptionId());
                                setUserPrefDataSubIdInDb(sir.getSubscriptionId());
                                break;
                            case DIALOG_CALLS:
                                final TelecomManager telecomManager =
                                        TelecomManager.from(context);
                                final List<PhoneAccountHandle> phoneAccountsList =
                                        telecomManager.getCallCapablePhoneAccounts();
                                setUserSelectedOutgoingPhoneAccount(
                                        value < 1 ? null : phoneAccountsList.get(value - 1));
                                break;
                            case DIALOG_SMS:
                                sir = subInfoList.get(value);
                                setDefaultSmsSubId(context, sir.getSubscriptionId());
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid dialog type "
                                        + id + " in SIM dialog.");
                        }
						updateAllOptions();
                        dialog.dismiss();
                    }
                };

        Dialog.OnKeyListener keyListener = new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                    }
                    return true;
                }
            };

        ArrayList<SubscriptionInfo> callsSubInfoList = new ArrayList<SubscriptionInfo>();
        if (id == DIALOG_CALLS) {
            final TelecomManager telecomManager = TelecomManager.from(context);
            final TelephonyManager telephonyManager = TelephonyManager.from(context);
            final Iterator<PhoneAccountHandle> phoneAccounts =
                    telecomManager.getCallCapablePhoneAccounts().listIterator();

            list.add(getResources().getString(R.string.sim_calls_ask_first_prefs_title));
            callsSubInfoList.add(null);
            while (phoneAccounts.hasNext()) {
                final PhoneAccount phoneAccount =
                        telecomManager.getPhoneAccount(phoneAccounts.next());
                list.add((String)phoneAccount.getLabel());
                int subId = telephonyManager.getSubIdForPhoneAccount(phoneAccount);
                if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    final SubscriptionInfo sir = SubscriptionManager.from(context)
                            .getActiveSubscriptionInfo(subId);
                    callsSubInfoList.add(sir);
                } else {
                    callsSubInfoList.add(null);
                }
            }
        } else {
            for (int i = 0; i < selectableSubInfoLength; ++i) {
                final SubscriptionInfo sir = subInfoList.get(i);
                CharSequence displayName = sir.getDisplayName();
                if (displayName == null) {
                    displayName = "";
                }
                list.add(displayName.toString());
            }
        }

        String[] arr = list.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        ListAdapter adapter = new SelectAccountListAdapter(
                id == DIALOG_CALLS ? callsSubInfoList : subInfoList,
                builder.getContext(),
                R.layout.sim_management_account_chooser,
                arr, id);

        switch (id) {
            case DIALOG_DATA:
                builder.setTitle(R.string.sim_management_data_title);
                break;
            case DIALOG_CALLS:
                builder.setTitle(R.string.sim_management_calls_title);
                break;
            case DIALOG_SMS:
                builder.setTitle(R.string.sim_management_sms_title);
                break;
            default:
                throw new IllegalArgumentException("Invalid dialog type "
                        + id + " in SIM dialog.");
        }

        Dialog dialog = builder.setAdapter(adapter, selectionListener).create();
        dialog.setOnKeyListener(keyListener);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

	private class SelectAccountListAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private int mResId;
        private int mDialogId;
        private List<SubscriptionInfo> mSubInfoList;

        public SelectAccountListAdapter(List<SubscriptionInfo> subInfoList,
                Context context, int resource, String[] arr, int dialogId) {
            super(context, resource, arr);
            mContext = context;
            mResId = resource;
            mDialogId = dialogId;
            mSubInfoList = subInfoList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView;
            final ViewHolder holder;

            if (convertView == null) {
                // Cache views for faster scrolling
                rowView = inflater.inflate(mResId, null);
                holder = new ViewHolder();
                holder.title = (TextView) rowView.findViewById(R.id.title);
                holder.summary = (TextView) rowView.findViewById(R.id.summary);
                rowView.setTag(holder);
            } else {
                rowView = convertView;
                holder = (ViewHolder) rowView.getTag();
            }

            final SubscriptionInfo sir = mSubInfoList.get(position);
            if (sir == null) {
                holder.title.setText(getItem(position));
                holder.summary.setText("");
            } else {
                holder.title.setText(sir.getDisplayName());
                holder.summary.setText(sir.getNumber());
            }
            return rowView;
        }

        private class ViewHolder {
            TextView title;
            TextView summary;
        }
    }
	
	private static void setDefaultDataSubId(final Context context, final int subId) {
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        subscriptionManager.setDefaultDataSubId(subId);
    }
	
	private void setUserPrefDataSubIdInDb(int subId) {
        android.provider.Settings.Global.putInt(mContext.getContentResolver(),
                SETTING_USER_PREF_DATA_SUB, subId);
        Log.d(TAG, "updating data subId: " + subId + " in DB");
    }

	private void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle phoneAccount) {
        final TelecomManager telecomManager = TelecomManager.from(mContext);
        telecomManager.setUserSelectedOutgoingPhoneAccount(phoneAccount);
    }
	
	private static void setDefaultSmsSubId(final Context context, final int subId) {
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        subscriptionManager.setDefaultSmsSubId(subId);
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
