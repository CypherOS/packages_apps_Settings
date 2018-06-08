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
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.aoscp.widget.SimColorPreference;
import com.android.settings.aoscp.widget.SimPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.aoscp.FooterConfirm;
import com.android.settingslib.aoscp.FooterConfirm.onActionClickListener;
import com.android.settingslib.aoscp.FooterConfirmMixin;

import org.codeaurora.internal.IExtTelephony;

import java.lang.Integer;
import java.lang.NumberFormatException;
import java.util.ArrayList;
import java.util.List;

public class SimManagementTool extends SettingsPreferenceFragment implements Indexable {
	
    private static final String TAG = "SimManagementTool";
    private static final boolean DEBUG = false;
	
	private static final String KEY_SIM_COLOR = "sim_color";
	private static final String TINT_POS = "tint_pos";

	private SimColorPreference mSimColor;
	private String[] mColorStrings;
	private int[] mTintArr;
    private int mTintSelectorPos;
	
	private static Context mContext;
	private int mSlotId;
	
	private Bundle mBundle;
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
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
		mBundle = bundle;
        mContext = getContext();
        addPreferencesFromResource(R.xml.sim_management_tool_settings);
		mScreen = getPreferenceScreen();

		mSubscriptionManager = SubscriptionManager.from(mContext);
		mSubInfoRecord = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(mSlotId);
		mScreen.setTitle(String.format(mContext.getResources().getString(
		            R.string.sim_management_tool_title), mSubInfoRecord.getCarrierName()));
		mSimStatus = new SimPreference(getPrefContext(), mSubInfoRecord);
		mScreen.addPreference(mSimStatus);

		mColorStrings = mContext.getResources().getStringArray(R.array.color_picker);
		mColorStrings[0] = mContext.getResources().getString(R.string.default_sim_color);
		mTintArr = mContext.getResources().getIntArray(com.android.internal.R.array.sim_colors);
		mTintSelectorPos = 0;

		mSimColor = (SimColorPreference) findPreference(KEY_SIM_COLOR);
		SelectColorAdapter adapter = new SelectColorAdapter(mContext,
                R.layout.settings_color_picker_item, mColorStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSimColor.setAdapter(adapter);
		
		for (int i = 0; i < mTintArr.length; i++) {
            if (mTintArr[i] == mSubInfoRecord.getIconTint()) {
                mSimColor.setSelection(i);
                mTintSelectorPos = i;
                break;
            }
        }
		updateSimColor();

        mSimColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int pos, long id) {
				try {
					final int tintSelected = mSimColor.getSelectedItemPosition();
					int subscriptionId = mSubInfoRecord.getSubscriptionId();
					int tint = mTintArr[tintSelected];
					mSubInfoRecord.setIconTint(tint);
					mSubscriptionManager.setIconTint(tint, subscriptionId);
				} catch (NumberFormatException e) {
				}
                mSimColor.setSelection(pos);
                mTintSelectorPos = pos;
				if (mBundle != null) {
					mBundle.putInt(TINT_POS, mTintSelectorPos);
				}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
		
		updateSubscriptionState();
    }

	private void updateSimColor() {
		if (mBundle != null) {
			int pos = mBundle.getInt(TINT_POS);
			mSimColor.setSelection(pos);
			mTintSelectorPos = pos;
		}
	}

	private void updateSubscriptionState() {
		mSimStatus.setSlotId(mSlotId);
		mSimStatus.update();
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
		updateSimColor();
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
	
	public class SelectColorAdapter extends ArrayAdapter<CharSequence> {
        private Context mContext;
        private int mResId;

        public SelectColorAdapter(
                Context context, int resource, String[] arr) {
            super(context, resource, arr);
            mContext = context;
            mResId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView;
            final ViewHolder holder;
            Resources res = mContext.getResources();
            int iconSize = res.getDimensionPixelSize(R.dimen.color_swatch_size);
            int strokeWidth = res.getDimensionPixelSize(R.dimen.color_swatch_stroke_width);

            if (convertView == null) {
                // Cache views for faster scrolling
                rowView = inflater.inflate(mResId, null);
                holder = new ViewHolder();
                ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
                drawable.setIntrinsicHeight(iconSize);
                drawable.setIntrinsicWidth(iconSize);
                drawable.getPaint().setStrokeWidth(strokeWidth);
                holder.label = (TextView) rowView.findViewById(R.id.color_text);
                holder.icon = (ImageView) rowView.findViewById(R.id.color_icon);
                holder.swatch = drawable;
                rowView.setTag(holder);
            } else {
                rowView = convertView;
                holder = (ViewHolder) rowView.getTag();
            }

            holder.label.setText(getItem(position));
            holder.swatch.getPaint().setColor(mTintArr[position]);
            holder.swatch.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageDrawable(holder.swatch);
            return rowView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View rowView = getView(position, convertView, parent);
            final ViewHolder holder = (ViewHolder) rowView.getTag();

            if (mTintSelectorPos == position) {
                holder.swatch.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            } else {
                holder.swatch.getPaint().setStyle(Paint.Style.STROKE);
            }
            holder.icon.setVisibility(View.VISIBLE);
            return rowView;
        }

        private class ViewHolder {
            TextView label;
            ImageView icon;
            ShapeDrawable swatch;
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
