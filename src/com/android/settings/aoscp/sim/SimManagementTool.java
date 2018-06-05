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

public class SimManagementTool extends RestrictedSettingsFragment implements Indexable {
	
    private static final String TAG = "SimManagementTool";
    private static final boolean DEBUG = false;

	private static final String KEY_SIM_HEADER = "sim_header";
    private static final String DISALLOW_CONFIG_SIM = "no_config_sim";
	
	private Context mContext;
	private int mSlotId;
	private int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
	private int[] mUiccProvisionStatus = new int[mPhoneCount];
	
	private static final int NOT_PROVISIONED = 0;
	private static final int PROVISIONED = 1;
	private static final int INVALID_STATE = -1;
	
	private SimPreference mSimStatus;

    public SimManagementTool(Context context, int slotId) {
        super(DISALLOW_CONFIG_SIM);
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
		
		try {
            mExtTelephony = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
        } catch (NoClassDefFoundError ex) {
            // ignore, device does not compile telephony-ext.
        }
		mSubscriptionManager = SubscriptionManager.from(mContext);
		final SubscriptionInfo sir = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(mSlotId);
		mScreen.setTitle(String.format(mContext.getResources().getString(
		            R.string.sim_management_tool_title), sir.getCarrierName()));
		updateSubscriptionState();
    }

	private void updateSubscriptionState() {
		final SubscriptionInfo sir = mSubscriptionManager
                    .getActiveSubscriptionInfoForSimSlotIndex(mSlotId);
        mSimStatus = new SimEnablerPreference(getPrefContext(), sir, mSlotId);
        mScreen.addPreference(mSimStatus);
    }

	@Override
    public void onPause() {
        super.onPause();
        mSimStatus.cleanUpPendingDialogs();
    }
	
	@Override
    public void onResume() {
        super.onResume();
        updateSubscriptions();
    }
	
	private class SimPreference extends Preference {
        SubscriptionInfo mSubInfoRecord;
        int mSlotId;
        Context mContext;

        public SimPreference(Context context, SubscriptionInfo subInfoRecord, int slotId) {
            this(context, null, 0, subInfoRecord, slotId);
        }

        public SimPreference(Context context, AttributeSet attrs, int defStyle,
                SubscriptionInfo subInfoRecord, int slotId) {
            super(context, attrs, defStyle);
            mContext = context;
            mSubInfoRecord = subInfoRecord;
            mSlotId = slotId;
            setKey("sim" + mSlotId);
            update();
        }

        public void update() {
            final Resources res = mContext.getResources();
            setTitle(res.getString(R.string.sim_management_tool_enable_sim_title));
            setSummary(res.getString(R.string.sim_management_tool_enable_sim_summary));
        }
    }
	
	// This is to show SIM Enable options on/off on UI for user selection.
    //  User can activate/de-activate through SIM on/off options.
    private class SimEnablerPreference extends SimPreference implements OnCheckedChangeListener {
        private static final int EVT_UPDATE = 1;
        private static final int EVT_SHOW_RESULT_DLG = 2;
        private static final int EVT_SHOW_PROGRESS_DLG = 3;
        private static final int EVT_PROGRESS_DLG_TIME_OUT = 4;

        private static final int CONFIRM_ALERT_DLG_ID = 1;
        private static final int ERROR_ALERT_DLG_ID = 2;
        private static final int RESULT_ALERT_DLG_ID = 3;

        private boolean mCurrentUiccProvisionState;
        private boolean mIsChecked;
        private boolean mCmdInProgress = false;
        private CompoundButton mSwitch;
        //Delay for progress dialog to dismiss
        private static final int PROGRESS_DLG_TIME_OUT = 30000;
        private static final int MSG_DELAY_TIME = 2000;

        public SimEnablerPreference(Context context, SubscriptionInfo sir, int slotId) {
            this(context, null, com.android.internal.R.attr.checkBoxPreferenceStyle, sir, slotId);
            setWidgetLayoutResource(R.layout.sim_switch);
        }
		
        private void sendMessage(int event, Handler handler, int delay) {
            Message message = handler.obtainMessage(event);
            handler.sendMessageDelayed(message, delay);
        }

        private void sendMessage(int event, Handler handler, int delay, int arg1, int arg2) {
            Message message = handler.obtainMessage(event, arg1, arg2);
            handler.sendMessageDelayed(message, delay);
        }

        private boolean hasCard() {
            return TelephonyManager.getDefault().hasIccCard(mSlotId);
        }

        private boolean isAirplaneModeOn() {
            return (Settings.Global.getInt(mContext.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0);
        }

        private int getProvisionStatus(int slotId) {
            return mUiccProvisionStatus[slotId];
        }

        @Override
        public void onBindViewHolder(PreferenceViewHolder holder) {
            super.onBindViewHolder(holder);
            logd("onBindView....");
            mSwitch = (CompoundButton) holder.findViewById(R.id.sub_switch_widget);
            mSwitch.setOnCheckedChangeListener(this);

            // Hide manual provisioning if the extphone framework
            // is not present, as the operation relies on said framework.
            if (mExtTelephony == null ||
                   !mContext.getResources().getBoolean(R.bool.config_enableManualSubProvisioning)) {
                mSwitch.setVisibility(View.GONE);
            } else {
                mSwitch.setVisibility(View.VISIBLE);
                mSwitch.setEnabled(!isAirplaneModeOn() && isValid());
                setChecked(getProvisionStatus(mSlotId) == PROVISIONED);
            }
        }

        @Override
        public void update() {
            final Resources res = mContext.getResources();
            logd("update()" + mSubInfoRecord);

            if (mExtTelephony != null) {
                try {
                    //get current provision state of the SIM.
                    mUiccProvisionStatus[mSlotId] =
                            mExtTelephony.getCurrentUiccCardProvisioningStatus(mSlotId);
                } catch (RemoteException ex) {
                    mUiccProvisionStatus[mSlotId] = INVALID_STATE;
                    loge("Failed to get pref, slotId: "+ mSlotId +" Exception: " + ex);
                }
            } else {
                // if we don't have telephony-ext, assume provisioned state
                mUiccProvisionStatus[mSlotId] = PROVISIONED;
            }

            super.update();
        }

        // This method returns true if SubScription record corresponds to this
        // Preference screen has a valid SIM and slot index/SubId.
        @Override
        protected boolean isValid() {
            return super.isValid() && getProvisionStatus(mSlotId) >= 0;
        }

        // Based on the received SIM provision state this method
        // sets the check box on Sim Preference UI and updates new
        // state to mCurrentUiccProvisionState.
        private void setChecked(boolean uiccProvisionState) {
            logd("setChecked: uiccProvisionState " + uiccProvisionState + "sir:" + mSubInfoRecord);
            if (mSwitch != null) {
                mSwitch.setOnCheckedChangeListener(null);
                // Do not update update checkstatus again in progress
                if (!mCmdInProgress) {
                    mSwitch.setChecked(uiccProvisionState);
                }
                mSwitch.setOnCheckedChangeListener(this);
                mCurrentUiccProvisionState = uiccProvisionState;
            }
        }

        @Override
        protected CharSequence determineSummary() {
            if (getProvisionStatus(mSlotId) != PROVISIONED) {
                CharSequence state = mContext.getString(
                        hasCard() ? R.string.sim_disabled : R.string.sim_missing);
                return mContext.getString(R.string.sim_enabler_summary,
                        mSubInfoRecord.getDisplayName(), state);
            } else {
                return super.determineSummary();
            }
        }

        /**
        * get number of Subs provisioned on the device
        * @param context
        * @return
        */
        public int getNumOfSubsProvisioned() {
            int activeSubInfoCount = 0;
            List<SubscriptionInfo> subInfoLists =
                    mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subInfoLists != null) {
                for (SubscriptionInfo subInfo : subInfoLists) {
                    if (getProvisionStatus(subInfo.getSimSlotIndex()) == PROVISIONED) {
                        activeSubInfoCount++;
                    }
                }
            }
            return activeSubInfoCount;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mIsChecked = isChecked;
            logd("onClick: " + isChecked);

            handleUserRequest();
        }

        // This internal method called when user changes preference from UI
        // 1. For activation/deactivation request from User, if device in APM mode
        //    OR if voice call active on any SIM it dispay error dialog and returns.
        // 2. For deactivation request it returns error dialog if only one SUB in
        //    active state.
        // 3. In other cases it sends user request to framework.
        private void handleUserRequest() {
            if (isAirplaneModeOn()) {
                // do nothing but warning
                logd("APM is on, EXIT!");
                showAlertDialog(ERROR_ALERT_DLG_ID, R.string.sim_enabler_airplane_on);
                return;
            }
            for (int i = 0; i < mPhoneCount; i++) {
                int[] subId = SubscriptionManager.getSubId(i);
                //when voice call in progress, subscription can't be activate/deactivate.
                if (TelephonyManager.getDefault().getCallState(subId[0])
                        != TelephonyManager.CALL_STATE_IDLE) {
                    logd("Call state for phoneId: " + i + " is not idle, EXIT!");
                    showAlertDialog(ERROR_ALERT_DLG_ID, R.string.sim_enabler_in_call);
                    return;
                }
            }

            if (!mIsChecked) {
                if (getNumOfSubsProvisioned() > 1) {
                    logd("More than one sub is active, Deactivation possible.");
                    showAlertDialog(CONFIRM_ALERT_DLG_ID, 0);
                } else {
                    logd("Only one sub is active. Deactivation not possible.");
                    showAlertDialog(ERROR_ALERT_DLG_ID, R.string.sim_enabler_both_inactive);
                    return;
                }
            } else {
                logd("Activate the sub");
                sendUiccProvisioningRequest();
            }
        }

        private void sendUiccProvisioningRequest() {
            if (!mSwitch.isEnabled()) {
                return;
            }
            new SimEnablerDisabler().execute();
        }

        private class SimEnablerDisabler extends AsyncTask<Void, Void, Integer> {

            int newProvisionedState = NOT_PROVISIONED;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mCmdInProgress = true;
                showProgressDialog();
                setEnabled(false);
            }

            @Override
            protected Integer doInBackground(Void... params) {
                int result = -1;
                newProvisionedState = NOT_PROVISIONED;
                try {
                    if (mIsChecked) {
                        result = mExtTelephony.activateUiccCard(mSlotId);
                        newProvisionedState = PROVISIONED;
                    } else {
                        result = mExtTelephony.deactivateUiccCard(mSlotId);
                    }
                } catch (RemoteException ex) {
                    loge("Activate  sub failed " + result + " phoneId " + mSlotId);
                } catch (NullPointerException ex) {
                    loge("Failed to activate sub Exception: " + ex);
                }
                return result;
            }

            @Override
            protected void onPostExecute(Integer result) {
                processSetUiccDone(result.intValue(), newProvisionedState);
            }
        }

        private void processSetUiccDone(int result, int newProvisionedState) {
            sendMessage(EVT_UPDATE, mHandler, MSG_DELAY_TIME);
            sendMessage(EVT_SHOW_RESULT_DLG, mHandler, MSG_DELAY_TIME, result, newProvisionedState);
            mCmdInProgress = false;
        }

        private void showAlertDialog(int dialogId, int msgId) {
            String title = mSubInfoRecord.getDisplayName().toString();
            // Confirm only one AlertDialog instance to show.
            dismissDialog(mAlertDialog);
            dismissDialog(mProgressDialog);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                    .setTitle(title);

            switch(dialogId) {
                case CONFIRM_ALERT_DLG_ID:
                    String message;
                    if (mContext.getResources().getBoolean(
                            R.bool.confirm_to_switch_data_service)) {
                        if (SubscriptionManager.getDefaultDataSubscriptionId() ==
                                mSubInfoRecord.getSubscriptionId()) {
                            message = mContext.getString(
                                    R.string.sim_enabler_need_switch_data_service,
                                    getProvisionedSlotId());
                        } else {
                            message = mContext.getString(R.string.sim_enabler_need_disable_sim);
                        }
                        builder.setTitle(R.string.sim_enabler_will_disable_sim_title);
                    } else {
                        message = mContext.getString(R.string.sim_enabler_need_disable_sim);
                    }
                    builder.setMessage(message);
                    builder.setPositiveButton(android.R.string.ok, mDialogClickListener);
                    builder.setNegativeButton(android.R.string.no, mDialogClickListener);
                    builder.setOnCancelListener(mDialogCanceListener);
                    break;

                case ERROR_ALERT_DLG_ID:
                    builder.setMessage(mContext.getString(msgId));
                    builder.setNeutralButton(android.R.string.ok, mDialogClickListener);
                    builder.setCancelable(false);
                    break;

                case RESULT_ALERT_DLG_ID:
                    String msg = mCurrentUiccProvisionState ?
                             mContext.getString(R.string.sub_activate_success) :
                            mContext.getString(R.string.sub_deactivate_success);
                    builder.setMessage(msg);
                    builder.setNeutralButton(android.R.string.ok, null);
                    break;
                default:
                    break;
            }

            mAlertDialog = builder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
            mAlertDialog.show();
        }

        private int getProvisionedSlotId() {
            int activeSlotId = -1;
            List<SubscriptionInfo> subInfoLists =
                    mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subInfoLists != null) {
                for (SubscriptionInfo subInfo : subInfoLists) {
                    if (getProvisionStatus(subInfo.getSimSlotIndex()) == PROVISIONED
                            && subInfo.getSubscriptionId() != mSubInfoRecord.getSubscriptionId())
                        activeSlotId = subInfo.getSimSlotIndex() + 1;
                }
            }
            return activeSlotId;
        }

        private void showProgressDialog() {
            String title = mSubInfoRecord.getDisplayName().toString();

            String msg = mContext.getString(mIsChecked ? R.string.sim_enabler_enabling
                    : R.string.sim_enabler_disabling);
            dismissDialog(mProgressDialog);
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setTitle(title);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            sendMessage(EVT_PROGRESS_DLG_TIME_OUT, mHandler, PROGRESS_DLG_TIME_OUT);
        }

        private void dismissDialog(Dialog dialog) {
            if((dialog != null) && (dialog.isShowing())) {
                dialog.dismiss();
                dialog = null;
            }
        }

        public void cleanUpPendingDialogs() {
            dismissDialog(mProgressDialog);
            dismissDialog(mAlertDialog);
        }

        private DialogInterface.OnClickListener mDialogClickListener = new DialogInterface
                .OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            dismissDialog(mAlertDialog);
                            sendUiccProvisioningRequest();
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                            update();
                        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                            update();
                        }
                    }
                };

        private DialogInterface.OnCancelListener mDialogCanceListener = new DialogInterface
                .OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        update();
                    }
                };


        private Handler mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {

                    switch(msg.what) {
                        case EVT_UPDATE:
                            simEnablerUpdate();

                        case EVT_SHOW_RESULT_DLG:
                            int result = msg.arg1;
                            int newProvisionedState = msg.arg2;
                            logd("EVT_SHOW_RESULT_DLG result: " + result +
                                    " new provisioned state " + newProvisionedState);
                            update();
                            if (result != 0) {
                                int msgId = (newProvisionedState == PROVISIONED) ?
                                        R.string.sub_activate_failed :
                                        R.string.sub_deactivate_failed;
                                showAlertDialog(ERROR_ALERT_DLG_ID, msgId);
                            } else {
                                mCurrentUiccProvisionState = newProvisionedState == PROVISIONED;
                                showAlertDialog(RESULT_ALERT_DLG_ID, 0);
                            }
                            mHandler.removeMessages(EVT_PROGRESS_DLG_TIME_OUT);
                            break;

                        case EVT_SHOW_PROGRESS_DLG:
                            logd("EVT_SHOW_PROGRESS_DLG");
                            showProgressDialog();
                            break;

                        case EVT_PROGRESS_DLG_TIME_OUT:
                            logd("EVT_PROGRESS_DLG_TIME_OUT");
                            dismissDialog(mProgressDialog);
                            // Must update UI when time out
                            update();
                            break;

                        default:
                        break;
                    }
                }
            };

        private void logd(String msg) {
            if (DBG) Log.d(TAG + "(" + mSlotId + ")", msg);
        }

        private void loge(String msg) {
            if (DBG) Log.e(TAG + "(" + mSlotId + ")", msg);
        }
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
