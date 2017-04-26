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

import android.annotation.DrawableRes;
import android.annotation.LayoutRes;
import android.annotation.StringRes;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto;
import com.android.settings.aoscp.support.actions.SupportBugReportFragment;
import com.android.settings.aoscp.support.web.Weblinks;
import com.android.settings.aoscp.support.SupportManagerCallback;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.android.settings.aoscp.support.SupportManagerCallback.SupportType.EMAIL;
import static com.android.settings.aoscp.support.SupportManagerCallback.SupportType.REPORT;

/**
 * Item adapter for support tiles.
 */
public final class SupportManagerItemAdapter extends RecyclerView.Adapter<SupportManagerItemAdapter.ViewHolder> {

    private static final int TYPE_ESCALATION_OPTIONS = R.layout.support_escalation_options;
    private static final int TYPE_SUPPORT_TILE = R.layout.support_manager_tile;
    private static final int TYPE_SUPPORT_TILE_SPACER = R.layout.support_tile_spacer;

    private final Activity mActivity;
	private final EscalationClickListener mEscalationClickListener;
    private final SpinnerItemSelectListener mSpinnerItemSelectListener;
	private final SupportManagerCallback mSupportManagerCallback;
    private final View.OnClickListener mItemClickListener;
    private final List<SupportData> mSupportData;

    public SupportManagerItemAdapter(Activity activity, SupportManagerCallback supportManagerCallback, 
	        View.OnClickListener itemClickListener) {
        mActivity = activity;
		mSupportManagerCallback = supportManagerCallback;
        mItemClickListener = itemClickListener;
		mEscalationClickListener = new EscalationClickListener();
        mSpinnerItemSelectListener = new SpinnerItemSelectListener();
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
        switch (holder.getItemViewType()) {
            case TYPE_ESCALATION_OPTIONS:
                bindEscalationOptions(holder, (EscalationData) data);
                break;
            case TYPE_SUPPORT_TILE_SPACER:
                break;
            default:
                bindSupportTile(holder, data);
                break;
        }
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
            if (data.intent != null &&
                    mActivity.getPackageManager().resolveActivity(data.intent, 0) != null) {
                if (data.metricsEvent >= 0) {
                    MetricsLogger.action(mActivity, data.metricsEvent);
                }
                mActivity.startActivityForResult(data.intent, 0);
            }
        }
    }

    /**
     * Create data for the adapter. If there is already data in the adapter, they will be
     * destroyed and recreated.
     */
    private void refreshData() {
        mSupportData.clear();
        addEscalationCards();
        addMoreHelpItems();
        notifyDataSetChanged();
    }

    /**
     * Adds 1 escalation card. Based on current phone state, the escalation card can display
     * different content.
     */
    private void addEscalationCards() {
        addOnlineEscalationCards();
    }

    /**
     * Finds and refreshes escalation card data.
     */
    private void refreshEscalationCards() {
        if (getItemCount() > 0) {
            final int itemType = getItemViewType(0 /* position */);
		    if (itemType == TYPE_ESCALATION_OPTIONS) {
                mSupportData.remove(0 /* position */);
                addEscalationCards();
                notifyItemChanged(0 /* position */);
            }
        }
    }

    private void addOnlineEscalationCards() {
		final boolean hasEmailOperation =
                mSupportManagerCallback.isSupportTypeEnabled(mActivity, EMAIL);
		final boolean hasReportOperation =
                mSupportManagerCallback.isSupportTypeEnabled(mActivity, REPORT);
        final EscalationData.Builder builder = new EscalationData.Builder(mActivity);
		if (!hasEmailOperation && !hasReportOperation) {
            // Support is unavailable
            builder.setTileTitle(R.string.support_welcome_title)
                   .setTileSummary(R.string.support_welcome_title_summary_unavailable);
		} else if (mSupportManagerCallback.isOperatingNow(EMAIL)
                || mSupportManagerCallback.isOperatingNow(REPORT)) {
            // Support is available for EMAIL and REPORT actions
            builder.setTileTitle(R.string.support_welcome_title)
                   .setTileSummary(R.string.support_welcome_title_summary);
		} else {
			// Add welcome header anyway
            builder.setTileTitle(R.string.support_welcome_title)
                   .setTileSummary(R.string.support_welcome_title_summary);
	    }
		if (hasEmailOperation) {
            builder.setText1(R.string.support_escalation_by_email)
                   .setEnabled1(mSupportManagerCallback.isOperatingNow(EMAIL));
        }
		if (hasReportOperation) {
            builder.setText2(R.string.support_escalation_by_report)
                   .setEnabled2(mSupportManagerCallback.isOperatingNow(REPORT));
        }
        mSupportData.add(0 /* index */, builder.build());
    }

    private void addMoreHelpItems() {
        mSupportData.add(new SupportData.Builder(mActivity, TYPE_SUPPORT_TILE_SPACER).build());
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://cypheros.co/help"));
        mSupportData.add(new SupportData.Builder(mActivity, TYPE_SUPPORT_TILE)
                .setIcon(R.drawable.ic_help_24dp)
                .setTileTitle(R.string.support_help_locate)
				.startActivity(intent)
                .setMetricsEvent(MetricsProto.MetricsEvent.ACTION_SUPPORT_HELP_AND_FEEDBACK)
                .build());
		Intent weblinks = mSupportManagerCallback.getWeblinksIntent();
		mSupportData.add(new SupportData.Builder(mActivity, TYPE_SUPPORT_TILE)
                .setIcon(R.drawable.ic_settings_ontheweb)
                .setTileTitle(R.string.weblinks_settings_title)
                .setIntent(weblinks)
                .setMetricsEvent(MetricsProto.MetricsEvent.ACTION_SUPPORT_HELP_AND_FEEDBACK)
                .build());
    }

    private void bindEscalationOptions(ViewHolder holder, EscalationData data) {
        holder.tileTitleView.setText(data.tileTitle);
        holder.tileTitleView.setContentDescription(data.tileTitleDescription);
        holder.tileSummaryView.setText(data.tileSummary);
        if (data.text1 == 0) {
            holder.text1View.setVisibility(View.GONE);
        } else {
            holder.text1View.setText(data.text1);
			holder.text1View.setOnClickListener(mEscalationClickListener);
            holder.text1View.setEnabled(data.enabled1);
            holder.text1View.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(data.text2)) {
            holder.text2View.setVisibility(View.GONE);
        } else {
            holder.text2View.setText(data.text2);
			holder.text2View.setOnClickListener(mEscalationClickListener);
            holder.text2View.setEnabled(data.enabled2);
            holder.text2View.setVisibility(View.VISIBLE);
        }
        if (holder.summary1View != null) {
            holder.summary1View.setText(data.summary1);
            holder.summary1View.setVisibility(!TextUtils.isEmpty(data.summary1)
                    ? View.VISIBLE : View.GONE);
        }
        if (holder.summary2View != null) {
            holder.summary2View.setText(data.summary2);
            holder.summary2View.setVisibility(!TextUtils.isEmpty(data.summary2)
                    ? View.VISIBLE : View.GONE);
        }
    }
	
    private void bindSupportTile(ViewHolder holder, SupportData data) {
        if (holder.iconView != null) {
            holder.iconView.setImageResource(data.icon);
        }
        if (holder.tileTitleView != null) {
            holder.tileTitleView.setText(data.tileTitle);
            holder.tileTitleView.setContentDescription(data.tileTitleDescription);
        }
        if (holder.tileSummaryView != null) {
            holder.tileSummaryView.setText(data.tileSummary);
        }
        holder.itemView.setOnClickListener(mItemClickListener);
    }
	
	/**
     * Show Bug Report chooser
     */
    private void startBugReportCaseChooser(final @SupportManagerCallback.SupportType int type) {
		if (mSupportManagerCallback.shouldShowBugreportAction(mActivity)) {
            DialogFragment fragment = SupportBugReportFragment.newInstance(type);
            fragment.show(mActivity.getFragmentManager(), SupportBugReportFragment.TAG);
            return;
		}
    }
	
	/**
     * Launch email action client
     */
    private void startEmailActionClient(final @SupportManagerCallback.SupportType int type) {
		if (mSupportManagerCallback.shouldShowEmailAction(mActivity)) {
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto: cypherosdevs@cypheros.co"));
            getActivity().startActivity(Intent.createChooser(emailIntent, "Send feedback"));
            return;
		}
    }
	
	/**
     * Click handler for starting escalation options.
     */
    private final class EscalationClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
				case android.R.id.text1:
                    MetricsLogger.action(mActivity,
                            MetricsProto.MetricsEvent.ACTION_SUPPORT_PHONE);
                    startEmailActionClient(EMAIL);
                    break;
                case android.R.id.text2:
                    MetricsLogger.action(mActivity,
                            MetricsProto.MetricsEvent.ACTION_SUPPORT_PHONE);
                    startBugReportCaseChooser(REPORT);
                    break;
			}
        }
    }
	
    private final class SpinnerItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            refreshEscalationCards();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    /**
     * {@link RecyclerView.ViewHolder} for support items.
     */
    static final class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView iconView;
        final TextView tileTitleView;
        final TextView tileSummaryView;
        final TextView text1View;
        final TextView text2View;
        final TextView summary1View;
        final TextView summary2View;

        ViewHolder(View itemView) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(android.R.id.icon);
            tileTitleView = (TextView) itemView.findViewById(R.id.tile_title);
            tileSummaryView = (TextView) itemView.findViewById(R.id.tile_summary);
            text1View = (TextView) itemView.findViewById(android.R.id.text1);
            text2View = (TextView) itemView.findViewById(android.R.id.text2);
            summary1View = (TextView) itemView.findViewById(R.id.summary1);
            summary2View = (TextView) itemView.findViewById(R.id.summary2);
        }
    }

    /**
     * Data for a single support item.
     */
    private static class SupportData {

        final Intent intent;
        final int metricsEvent;
        @LayoutRes
        final int type;
        @DrawableRes
        final int icon;
        @StringRes
        final int tileTitle;
        final CharSequence tileTitleDescription;
        final CharSequence tileSummary;


        private SupportData(Builder builder) {
            this.type = builder.mType;
            this.icon = builder.mIcon;
            this.tileTitle = builder.mTileTitle;
            this.tileTitleDescription = builder.mTileTitleDescription;
            this.tileSummary = builder.mTileSummary;
            this.intent = builder.mIntent;
            this.metricsEvent = builder.mMetricsEvent;
        }

        static class Builder {

            protected final Context mContext;
            @LayoutRes
            private final int mType;
            @DrawableRes
            private int mIcon;
            @StringRes
            private int mTileTitle;
            private CharSequence mTileTitleDescription;
            private CharSequence mTileSummary;
            private Intent mIntent;
            private int mMetricsEvent = -1;

            Builder(Context context, @LayoutRes int type) {
                mContext = context;
                mType = type;
            }

            Builder setIcon(@DrawableRes int icon) {
                mIcon = icon;
                return this;
            }

            Builder setTileTitle(@StringRes int title) {
                mTileTitle = title;
                return this;
            }

            Builder setTileTitleDescription(@StringRes int titleDescription) {
                mTileTitleDescription = mContext.getString(titleDescription);
                return this;
            }

            Builder setTileSummary(@StringRes int summary) {
                mTileSummary = mContext.getString(summary);
                return this;
            }

            Builder setTileSummary(CharSequence summary) {
                mTileSummary = summary;
                return this;
            }

            Builder setMetricsEvent(int metricsEvent) {
                mMetricsEvent = metricsEvent;
                return this;
            }

            Builder setIntent(Intent intent) {
                mIntent = intent;
                return this;
            }
			
			Builder startActivity(Intent intent) {
                mIntent = intent;
                return this;
            }

            SupportData build() {
                return new SupportData(this);
            }
        }
    }

    /**
     * Data model for escalation cards.
     */
    private static class EscalationData extends SupportData {

        @StringRes
        final int text1;
        final CharSequence text2;
        final boolean enabled1;
        final boolean enabled2;
        final CharSequence summary1;
        final CharSequence summary2;

        private EscalationData(Builder builder) {
            super(builder);
            this.text1 = builder.mText1;
            this.text2 = builder.mText2;
            this.summary1 = builder.mSummary1;
            this.summary2 = builder.mSummary2;
            this.enabled1 = builder.mEnabled1;
            this.enabled2 = builder.mEnabled2;
        }

        static class Builder extends SupportData.Builder {

            @StringRes
            private int mText1;
            private CharSequence mText2;
            private CharSequence mSummary1;
            private CharSequence mSummary2;
            private boolean mEnabled1;
            private boolean mEnabled2;

            protected Builder(Context context, @LayoutRes int type) {
                super(context, type);
            }

            Builder(Context context) {
                this(context, TYPE_ESCALATION_OPTIONS);
            }

            Builder setEnabled1(boolean enabled) {
                mEnabled1 = enabled;
                return this;
            }

            Builder setText1(@StringRes int text1) {
                mText1 = text1;
                return this;
            }

            Builder setText2(@StringRes int text2) {
                mText2 = mContext.getString(text2);
                return this;
            }

            Builder setText2(CharSequence text2) {
                mText2 = text2;
                return this;
            }

            Builder setSummary1(String summary1) {
                mSummary1 = summary1;
                return this;
            }

            Builder setEnabled2(boolean enabled) {
                mEnabled2 = enabled;
                return this;
            }

            Builder setSummary2(String summary2) {
                mSummary2 = summary2;
                return this;
            }

            EscalationData build() {
                return new EscalationData(this);
            }
        }
    }
}