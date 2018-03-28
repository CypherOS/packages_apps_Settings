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

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.android.settings.R;

/**
 * RadioButtonPreferenceGroup is a preference group that can be expanded or collapsed and
 * also has a checkbox.
 */
public class RadioButtonPreferenceGroup extends PreferenceGroup {
	
    private boolean mCollapsed;

    public RadioButtonPreferenceGroup(Context context) {
        this(context, null);
		initialize();
    }

    public RadioButtonPreferenceGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }
	
	private void initialize() {
        setLayoutResource(R.layout.expand_preference);
        setIcon(R.drawable.ic_arrow_down_24dp);
    }
	
	@Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedAbove(false);
    }

    @Override
    public boolean addPreference(Preference pref) {
        super.addPreference(pref);
        pref.setVisible(!isCollapsed());
        return true;
    }

    // The preference click handler.
    @Override
    protected void onClick() {
        super.onClick();
        setCollapse(!isCollapsed());
    }

    /**
     * Return if the view is collapsed.
     */
    public boolean isCollapsed() {
        return mCollapsed;
    }

    private void setCollapse(boolean isCollapsed) {
        if (mCollapsed == isCollapsed) {
            return;
        }

        mCollapsed = isCollapsed;
        setAllPreferencesVisibility(!isCollapsed);
        notifyChanged();
    }

    private void setAllPreferencesVisibility(boolean visible) {
        for (int i = 0; i < getPreferenceCount(); i++) {
            Preference pref = getPreference(i);
            pref.setVisible(visible);
        }
    }
}