/*
 * Copyright (C) 2019 CypherOS
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

package com.android.settings.aoscp.deviceintro;

import static android.provider.Settings.System.DEVICE_INTRODUCTION_COMPLETED;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.aoscp.FirstTimeBase;

public class FirstTimeFinished extends FirstTimeBase {

    private static final String TAG = "FirstTimeFinished";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHeaderText(R.string.first_time_finished);
    }

    @Override
    protected void onNextButtonClick() {
        confirmAndFinsh();
    }

    private void confirmAndFinsh() {
        Settings.System.putInt(getContentResolver(), DEVICE_INTRODUCTION_COMPLETED, 1);
        finish();
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected void initViews() {
        super.initViews();
        TextView description = (TextView) findViewById(R.id.description_text);
        description.setText(R.string.first_time_finished_desc);
        setIllustration(R.raw.first_time_finished);
        getNextButton().setText(R.string.first_time_confirm_button);
    }
}
