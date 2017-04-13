/*
 * Copyright (C) 2017 CypherOS
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
package com.android.settings.aoscp.support;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.logging.MetricsProto;
import com.android.settings.InstrumentedFragment;
import com.android.settings.aoscp.support.SupportManagerCallback;
import com.android.settings.aoscp.support.SupportManagerItemAdapter;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.R;

/**
 * Fragment for support tab in aoscp settings.
 */
public final class SupportManager extends InstrumentedFragment implements View.OnClickListener {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    
    private Activity mActivity;
    private View mContent;
    private RecyclerView mRecyclerView;
	private SupportManagerCallback mSupportManagerCallback;
    private SupportManagerItemAdapter mSupportManagerItemAdapter;

    @Override
    protected int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SUPPORT_FRAGMENT;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
		mSupportManagerCallback =
                FeatureFactory.getFactory(mActivity).getSupportManagerCallback(mActivity);
        mSupportManagerItemAdapter = new SupportManagerItemAdapter(mActivity, mSupportManagerCallback, 
		        this /* itemClickListener */);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.support_manager, container, false);
        mRecyclerView = (RecyclerView) mContent.findViewById(R.id.support_manager_items);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false /* reverseLayout */));
        mRecyclerView.setAdapter(mSupportManagerItemAdapter);
        return mContent;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        final SupportManagerItemAdapter.ViewHolder vh =
                (SupportManagerItemAdapter.ViewHolder) mRecyclerView.getChildViewHolder(v);
        mSupportManagerItemAdapter.onItemClicked(vh.getAdapterPosition());
    }
}