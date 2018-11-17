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
 * limitations under the License
 */

package com.android.settings.aoscp;

import android.annotation.Nullable;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SetupWizardUtils;
import com.android.settings.core.InstrumentedActivity;
import com.android.setupwizardlib.GlifLayout;

public class FirstTimeIntroduction extends InstrumentedActivity
        implements View.OnClickListener {

	static final String FIRST_TIME_PROPERTY = "persist.aoscp.first_time";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_time_introduction);
        setHeaderText(R.string.first_time_introduction_header);
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        resid = SetupWizardUtils.getTheme(getIntent());
        super.onApplyThemeResource(theme, resid, first);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initViews();
    }
	
	@Override
    public int getMetricsCategory() {
        return -1;
    }

    protected void initViews() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Button confirm = getConfirmButton();
        if (confirm != null) {
            confirm.setOnClickListener(this);
        }
		
		TextView description = (TextView) findViewById(R.id.description_text);
    }

    protected GlifLayout getLayout() {
        return (GlifLayout) findViewById(R.id.setup_wizard_layout);
    }

    protected void setHeaderText(int resId, boolean force) {
        TextView layoutTitle = getLayout().getHeaderTextView();
        CharSequence previousTitle = layoutTitle.getText();
        CharSequence title = getText(resId);
        if (previousTitle != title || force) {
            if (!TextUtils.isEmpty(previousTitle)) {
                layoutTitle.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
            }
            getLayout().setHeaderText(title);
            setTitle(title);
        }
    }

    protected void setHeaderText(int resId) {
        setHeaderText(resId, false /* force */);
    }

    protected Button getConfirmButton() {
        return (Button) findViewById(R.id.confirm_button);
    }

    @Override
    public void onClick(View v) {
        if (v == getConfirmButton()) {
            onConfirmButtonClick();
        }
    }

    protected void onConfirmButtonClick() {
		SystemProperties.set(FIRST_TIME_PROPERTY, Boolean.toString(false));
		finish();
    }
}
