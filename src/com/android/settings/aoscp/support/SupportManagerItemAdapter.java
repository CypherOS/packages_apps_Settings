/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.accounts.Account;
import android.annotation.DrawableRes;
import android.annotation.LayoutRes;
import android.annotation.StringRes;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.overlay.SupportFeatureProvider;
import java.util.ArrayList;
import java.util.List;
import static com.android.settings.overlay.SupportFeatureProvider.SupportType.CHAT;
import static com.android.settings.overlay.SupportFeatureProvider.SupportType.EMAIL;
import static com.android.settings.overlay.SupportFeatureProvider.SupportType.PHONE;
/**
 * Item adapter for support tiles.
 */
public final class SupportManagerItemAdapter extends RecyclerView.Adapter<SupportItemAdapter.ViewHolder> {
	
    private static final int TYPE_TITLE = R.layout.support_manager_item_title;
    private static final int TYPE_SUBTITLE = R.layout.support_manager_item_subtitle;
    private static final int TYPE_SUPPORT_TILE = R.layout.support_manager_tile;
	private static final int TYPE_SUPPORT_CARD = R.layout.support_manager_card;
	
    private final Activity mActivity;
    private final View.OnClickListener mItemClickListener;
    private final List<SupportData> mSupportData;
	
    public SupportItemAdapter(Activity activity, View.OnClickListener itemClickListener) {
        mActivity = activity;
        mItemClickListener = itemClickListener;
        mSupportData = new ArrayList<>();
        refreshData();
    }
	
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                viewType, parent, false));
    }
	
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final SupportData data = mSupportData.get(position);
		
        if (holder.iconView != null) {
            holder.iconView.setImageResource(data.icon);
        }
        if (holder.titleView != null) {
            holder.titleView.setText(data.title);
        }
        if (holder.summaryView != null) {
            holder.summaryView.setText(data.summary);
        }
        holder.itemView.setOnClickListener(mItemClickListener);
    }
	
    @Override
    public int getItemViewType(int position) {
        return mSupportData.get(position).type;
    }
	
    @Override
    public int getItemCount() {
        return mSupportData.size();
    }
	
    /**
     * Called when a support item is clicked.
     */
    public void onItemClicked(int position) {
        if (position >= 0 && position < mSupportData.size()) {
            final SupportData data = mSupportData.get(position);
            if (data.intent != null) {
                mActivity.startActivityForResult(data.intent, 0);
            }
        }
    }
	
    /**
     * Create data for the adapter. If there is already data in the adapter, they will be
     * destroyed and recreated.
     */
    public void refreshData() {
        mSupportData.clear();
        addMoreHelpItems();
        notifyDataSetChanged();
    }
	
    private void addSupportCards() {
        mSupportData.add(new SupportData(TYPE_TITLE, 0 /* icon */,
                R.string.support_welcome_title, R.string.support_welcome_title_summary,
                null /* intent */));
    }
	
    private void addMoreHelpItems() {
        mSupportData.add(new SupportData(TYPE_SUPPORT_TILE, R.drawable.ic_help_24dp,
                R.string.support_help_locate, 0 /* summary */, null /*intent */));
    }
	
    /**
     * {@link RecyclerView.ViewHolder} for support items.
     */
    static final class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView titleView;
        final TextView summaryView;
        ViewHolder(View itemView) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(android.R.id.icon);
            titleView = (TextView) itemView.findViewById(android.R.id.title);
            summaryView = (TextView) itemView.findViewById(android.R.id.summary);
        }
    }
    /**
     * Data for a single support item.
     */
    private static final class SupportData {
        final Intent intent;
        @LayoutRes
        final int type;
        @DrawableRes
        final int icon;
        @StringRes
        final int text1;
        @StringRes
        final int text2;
		
        SupportData(@LayoutRes int type, @DrawableRes int icon, @StringRes int title,
                @StringRes int summary, Intent intent) {
            this.type = type;
            this.icon = icon;
            this.title = title;
            this.summary = summary;
            this.intent = intent;
        }
    }
}