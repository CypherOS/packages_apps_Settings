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

package com.android.settings.aoscp.preference;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.R;

public class TwoChoicePreference extends Preference implements OnCheckedChangeListener {

    private Switch mSwitch;
    private TextView mTitle;
    private TextView mSummary;
    private Context mContext;
    private boolean mSwitchEnabled;
    private CharSequence prefTitle;
    private CharSequence prefSummary;

    public TwoChoicePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public TwoChoicePreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.switchPreferenceStyle);
        mContext = context;
    }

    public TwoChoicePreference(Context context) {
        this(context, null);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        mTitle = (TextView) view.findViewById(R.id.title);
        mTitle.setText(prefTitle);
        mSummary = (TextView) view.findViewById(R.id.summary);
        mSummary.setText(prefSummary);
        mSwitch = (Switch) view.findViewById(R.id.switchButton);
        mSwitch.setOnCheckedChangeListener(this);
        setChecked(mSwitchEnabled);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        callChangeListener(isChecked);
        mSwitchEnabled = isChecked;
    }

    public void setTitle(CharSequence title) {
        if (mTitle == null) {
            prefTitle = title;
            return;
        }
        mTitle.setText(title);
    }

    public void setSummary(CharSequence summary) {
        if (mSummary == null) {
            prefSummary = summary;
            return;
        }
        mSummary.setText(summary);
    }

    public void setChecked(boolean state) {
        if (mSwitch != null) {
            mSwitch.setOnCheckedChangeListener(null);
            mSwitch.setChecked(state);
            mSwitch.setOnCheckedChangeListener(this);
        }
        mSwitchEnabled=state;
    }
}