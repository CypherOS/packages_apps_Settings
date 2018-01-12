/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.aoscp.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.widget.SummaryUpdater;

/**
 * Helper class that listeners to wifi callback and notify client when there is update in
 * wifi summary info.
 */
public final class NetworkTrafficSummaryUpdater extends SummaryUpdater {

    private final BroadcastReceiver mReceiver;
	
	private NetworkTrafficMonitoring mTrafficMonitor;

    public NetworkTrafficSummaryUpdater(Context context, OnSummaryChangeListener listener) {
        this(context, listener);
    }

    @VisibleForTesting
    public NetworkTrafficSummaryUpdater(Context context, OnSummaryChangeListener listener) {
        super(context, listener);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyChangeIfNeeded();
            }
        };
    }

    @Override
    public void register(boolean register) {
        if (register) {
            mContext.registerReceiver(mReceiver);
        } else {
            mContext.unregisterReceiver(mReceiver);
        }
    }

    @Override
    public String getSummary() {
        if (mTrafficMonitor.isTrafficMonitorEnabled()) {
            return mContext.getString(R.string.network_traffic_summary_on);
        } else {
			return mContext.getString(R.string.network_traffic_summary_off);
        }
    }

}
