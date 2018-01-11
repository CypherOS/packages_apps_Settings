/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.dashboard.conditional;

import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.util.Log;
import android.os.UserHandle;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.aoscp.network.NetworkTrafficMonitoring;

public final class TrafficMonitorCondition extends Condition {

    public static String TAG = "NTM_Condition";

    TrafficMonitorCondition(ConditionManager manager) {
        super(manager);
    }

    @Override
    public void refreshState() {
        Log.d(TAG, "NTM condition refreshed");
        setActive(isTrafficMonitorEnabled());
    }

    @Override
    public int getMetricsConstant() {
        return -1;
    }

    @Override
    public Icon getIcon() {
        return Icon.createWithResource(mManager.getContext(), R.drawable.ic_traffic_monitor);
    }

    @Override
    public CharSequence getTitle() {
        return mManager.getContext().getString(R.string.condition_network_traffic_title);
    }

    @Override
    public CharSequence getSummary() {
        return mManager.getContext().getString(R.string.condition_network_traffic_summary);
    }

    @Override
    public CharSequence[] getActions() {
        return new CharSequence[] { mManager.getContext().getString(R.string.condition_turn_off) };
    }

    @Override
    public void onPrimaryClick() {
        Utils.startWithFragment(mManager.getContext(), NetworkTrafficMonitoring.class.getName(), null,
                null, 0, R.string.network_traffic_title, null, MetricsEvent.DASHBOARD_SUMMARY);
    }

    @Override
    public void onActionClick(int index) {
        if (index == 0) {
            setActive(setTrafficMonitorEnabled(false));
            refreshState();
        } else {
            throw new IllegalArgumentException("Unexpected index " + index);
        }
    }

    public boolean isTrafficMonitorEnabled() {
        return Settings.System.getIntForUser(mManager.getContext().getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, 0, UserHandle.USER_CURRENT) == 1;
    }

    public boolean setTrafficMonitorEnabled(boolean enabled) {
        return Settings.System.putInt(mManager.getContext().getContentResolver(), Settings.System.NETWORK_TRAFFIC_STATE, enabled ? 1 : 0);
    }
}
