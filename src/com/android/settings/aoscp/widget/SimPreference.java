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

package com.android.settings.aoscp.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.settings.R;
import com.android.settings.aoscp.sim.SimManagementTool;

import org.codeaurora.internal.IExtTelephony;

import java.lang.NoClassDefFoundError;
import java.util.List;

public class SimPreference extends Preference implements OnCheckedChangeListener {

    private static final String TAG = "SimPreference";
	
	private static final int EVT_UPDATE = 1;
    private static final int EVT_SHOW_RESULT_DLG = 2;
    private static final int EVT_SHOW_PROGRESS_DLG = 3;
    private static final int EVT_PROGRESS_DLG_TIME_OUT = 4;

    private static final int CONFIRM_ALERT_DLG_ID = 1;
    private static final int ERROR_ALERT_DLG_ID = 2;
    private static final int RESULT_ALERT_DLG_ID = 3;
	
	private static final int NOT_PROVISIONED = 0;
	private static final int PROVISIONED = 1;
	private static final int INVALID_STATE = -1;

	private IExtTelephony mExtTelephony;
    private boolean mCurrentUiccProvisionState;
    private boolean mIsChecked;
    private boolean mCmdInProgress = false;
	private int mSlotId;
    private CompoundButton mSwitch;
    //Delay for progress dialog to dismiss
    private static final int PROGRESS_DLG_TIME_OUT = 30000;
    private static final int MSG_DELAY_TIME = 2000;
	
	private int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
	private int[] mUiccProvisionStatus = new int[mPhoneCount];
	
	private AlertDialog mAlertDialog = null;
	private boolean mIsAlertDialog = false;
	private ProgressDialog mProgressDialog = null;
	private boolean mIsProgressDialog = false;
	private boolean mConfirmDialog = false;
	private boolean mDialogSuccess = false;
	
	private String mAction;
	private String mMessage;
	
	private SubscriptionInfo mSubInfoRecord;
	private SubscriptionManager mSubscriptionManager;
    private Context mContext;

    public SimPreference(Context context, SubscriptionInfo subInfoRecord) {
        this(context, null, com.android.internal.R.attr.checkBoxPreferenceStyle, 
		    subInfoRecord);
    }

    public SimPreference(Context context, AttributeSet attrs, int defStyle,
            SubscriptionInfo subInfoRecord) {
        super(context, attrs, defStyle);
		setLayoutResource(R.layout.preference_sim);
        mContext = context;
        mSubInfoRecord = subInfoRecord;
        setKey("sim_status" + mSlotId);
		setTitle(R.string.sim_management_tool_enable_sim_title);
		setSummary(R.string.sim_management_tool_enable_sim_summary);
		try {
            mExtTelephony = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
        } catch (NoClassDefFoundError ex) {
            // ignore, device does not compile telephony-ext.
        }
		mSubscriptionManager = SubscriptionManager.from(mContext);
        update();
    }

	public void setSlotId(int slotId) {
		mSlotId = slotId;
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
        mSwitch = (CompoundButton) holder.findViewById(R.id.switch_widget);
        mSwitch.setOnCheckedChangeListener(this);
		
		mSwitch.setVisibility(View.VISIBLE);
        mSwitch.setEnabled(!isAirplaneModeOn() && isValid());
        setChecked(getProvisionStatus(mSlotId) == PROVISIONED);
    }

    public void update() {
		final Resources res = mContext.getResources();
        if (mExtTelephony != null) {
            try {
                //get current provision state of the SIM.
                mUiccProvisionStatus[mSlotId] =
                        mExtTelephony.getCurrentUiccCardProvisioningStatus(mSlotId);
            } catch (RemoteException ex) {
                mUiccProvisionStatus[mSlotId] = INVALID_STATE;
            }
        } else {
            // if we don't have telephony-ext, assume provisioned state
            mUiccProvisionStatus[mSlotId] = PROVISIONED;
        }
    }
	
	// This method returns true if SubScription record corresponds to this
    // Preference screen has a valid SIM and slot index/SubId.
    protected boolean isValid() {
        return getProvisionStatus(mSlotId) >= 0;
    }
	
	// Based on the received SIM provision state this method
    // sets the check box on Sim Preference UI and updates new
    // state to mCurrentUiccProvisionState.
    private void setChecked(boolean uiccProvisionState) {
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
            showDialog(ERROR_ALERT_DLG_ID, R.string.sim_enabler_airplane_on);
            return;
        }
        for (int i = 0; i < mPhoneCount; i++) {
            int[] subId = SubscriptionManager.getSubId(i);
            //when voice call in progress, subscription can't be activate/deactivate.
            if (TelephonyManager.getDefault().getCallState(subId[0])
                    != TelephonyManager.CALL_STATE_IDLE) {
                showDialog(ERROR_ALERT_DLG_ID, R.string.sim_enabler_in_call);
                return;
            }
        }

        if (!mIsChecked) {
            if (getNumOfSubsProvisioned() > 0) {
                showDialog(CONFIRM_ALERT_DLG_ID, 0);
            } else {
                showDialog(ERROR_ALERT_DLG_ID, R.string.sim_enabler_both_inactive);
                return;
            }
        } else {
            sendUiccProvisioningRequest();
        }
    }
	
	public void sendUiccProvisioningRequest() {
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
            } catch (NullPointerException ex) {
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
	
	private void showDialog(int dialogId, int msgId) {
		mIsAlertDialog = true;
		dismissDialog(mIsAlertDialog, false);
        dismissDialog(false, mIsProgressDialog);
		switch(dialogId) {
            case CONFIRM_ALERT_DLG_ID:
			    mConfirmDialog = true;
                if (mContext.getResources().getBoolean(
                        R.bool.confirm_to_switch_data_service)) {
                    if (SubscriptionManager.getDefaultDataSubscriptionId() ==
                            mSubInfoRecord.getSubscriptionId()) {
                        mMessage = mContext.getString(
                                R.string.sim_enabler_need_switch_data_service,
                                getProvisionedSlotId());
                    } else {
                        mMessage = mContext.getString(R.string.sim_enabler_need_disable_sim);
                    }
                } else {
                    mMessage = mContext.getString(R.string.sim_enabler_need_disable_sim);
                }
				mAction = mContext.getString(R.string.sim_management_dialog_continue);
                break;
            case ERROR_ALERT_DLG_ID:
			    mMessage = mContext.getString(msgId);
				mAction = mContext.getString(android.R.string.ok);
                break;

            case RESULT_ALERT_DLG_ID:
                mDialogSuccess = true;
                break;
            default:
                break;
        }
		if (mDialogSuccess) {
			showSuccessDialog();
		} else {
			SimManagementTool.showDialog(mConfirmDialog, mMessage, mAction);
		}
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
		mIsProgressDialog = true;
        String msg = mContext.getString(mIsChecked ? R.string.sim_enabler_enabling
                : R.string.sim_enabler_disabling);
        dismissDialog(false, mIsProgressDialog);
		SimManagementTool.showProgressDialog(msg);
        sendMessage(EVT_PROGRESS_DLG_TIME_OUT, mHandler, PROGRESS_DLG_TIME_OUT);
    }

	private void showSuccessDialog() {
		String msg = mCurrentUiccProvisionState ?
                mContext.getString(R.string.sub_activate_success) :
                mContext.getString(R.string.sub_deactivate_success);
        dismissDialog(false, mIsProgressDialog);
		SimManagementTool.showSuccessDialog(msg);
    }

    private void dismissDialog(boolean isAlert, boolean isProgress) {
        if (isAlert) SimManagementTool.dismissDialog();
		if (isProgress) SimManagementTool.dismissDialog();
    }

    public void cleanUpPendingDialogs() {
        dismissDialog(false, mIsProgressDialog);
        dismissDialog(mIsAlertDialog, false);
    }

    private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch(msg.what) {
                    case EVT_UPDATE:
                    case EVT_SHOW_RESULT_DLG:
                        int result = msg.arg1;
                        int newProvisionedState = msg.arg2;
                        update();
                        if (result != 0) {
                            int msgId = (newProvisionedState == PROVISIONED) ?
                                    R.string.sub_activate_failed :
                                    R.string.sub_deactivate_failed;
                            showDialog(ERROR_ALERT_DLG_ID, msgId);
                        } else {
                            mCurrentUiccProvisionState = newProvisionedState == PROVISIONED;
                            showDialog(RESULT_ALERT_DLG_ID, 0);
                        }
                        mHandler.removeMessages(EVT_PROGRESS_DLG_TIME_OUT);
                        break;
                    case EVT_SHOW_PROGRESS_DLG:
                        showProgressDialog();
                        break;
                    case EVT_PROGRESS_DLG_TIME_OUT:
                        dismissDialog(false, mIsProgressDialog);
                        // Must update UI when time out
                        update();
                        break;
                    default:
                        break;
                }
            }
        };
}
