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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.aoscp.FirstTimeBase;

public class FirstTimePrivacy extends FirstTimeBase {

    private static final String TAG = "FirstTimePrivacy";

    protected static final int FIRST_TIME_AMBIENT_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHeaderText(R.string.first_time_privacy);
    }

    @Override
    protected void onNextButtonClick() {
        launchAmbientPlayStep();
    }

    private void launchAmbientPlayStep() {
        Intent intent = new Intent(this, FirstTimeAmbient.class);
        if (mUserId != UserHandle.USER_NULL) {
            intent.putExtra(Intent.EXTRA_USER_ID, mUserId);
        }
        startActivityForResult(intent, FIRST_TIME_AMBIENT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FIRST_TIME_AMBIENT_REQUEST) {
            setResult(RESULT_OK, data);
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected void initViews() {
        super.initViews();
        TextView description = (TextView) findViewById(R.id.description_text);
        description.setText(R.string.first_time_privacy_desc);
        setIllustration(R.raw.first_time_privacy);
        getNextButton().setText(R.string.first_time_next_button);
    }
}
