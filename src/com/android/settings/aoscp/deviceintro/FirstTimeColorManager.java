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

public class FirstTimeColorManager extends FirstTimeBase {

    private static final String TAG = "FirstTimeColorManager";

    protected static final int FIRST_TIME_BUG_REPORT_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHeaderText(R.string.first_time_color_manager);
    }

    @Override
    protected void onNextButtonClick() {
        launchBugReportStep();
    }

    private void launchBugReportStep() {
        Intent intent = new Intent(this, FirstTimeBugReport.class);
        if (mUserId != UserHandle.USER_NULL) {
            intent.putExtra(Intent.EXTRA_USER_ID, mUserId);
        }
        startActivityForResult(intent, FIRST_TIME_BUG_REPORT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final boolean isResultFinished = resultCode == RESULT_FINISHED;
        if (requestCode == FIRST_TIME_BUG_REPORT_REQUEST) {
            if (isResultFinished || resultCode == RESULT_SKIP) {
                final int result = isResultFinished ? RESULT_OK : RESULT_SKIP;
                setResult(result, data);
                finish();
                return;
            }
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
		description.setText(R.string.first_time_color_manager_desc);
		getNextButton().setText(R.string.first_time_next_button);
    }
}
