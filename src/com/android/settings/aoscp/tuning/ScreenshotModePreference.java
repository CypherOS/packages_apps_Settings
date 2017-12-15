/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.aoscp.tuning;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.android.settingslib.CustomDialogPreference;

public class ScreenshotModePreference extends CustomDialogPreference {

    private static final String TAG = "ScreenshotModePreference";

    private CheckedTextView mScreenshotModeFullTitle;
    private TextView mScreenshotModeFullSummary;
    private CheckedTextView mScreenshotModePartialTitle;
    private TextView mScreenshotModePartialSummary;

    public ScreenshotModePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder, DialogInterface.OnClickListener onClickListener) {
        super.onPrepareDialogBuilder(builder, onClickListener);

        final View view = View.inflate(getContext(), R.layout.screenshot_mode_preference, null);
        mScreenshotModeFullTitle = (CheckedTextView) view.findViewById(R.id.screenshot_mode_fullscreen_title);
        mScreenshotModeFullSummary = (TextView) view.findViewById(R.id.screenshot_mode_fullscreen_summary);
        mScreenshotModePartialTitle = (CheckedTextView) view.findViewById(R.id.screenshot_mode_partial_title);
        mScreenshotModePartialSummary = (TextView) view.findViewById(R.id.screenshot_mode_partial_summary);
        final View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == mScreenshotModePartialTitle || v == mScreenshotModePartialSummary) {
                    mScreenshotModeFullTitle.setChecked(false);
                    mScreenshotModePartialTitle.setChecked(true);
                }
                if (v == mScreenshotModeFullTitle || v == mScreenshotModeFullSummary) {
                    mScreenshotModeFullTitle.setChecked(true);
                    mScreenshotModePartialTitle.setChecked(false);
                }
            }
        };
        mScreenshotModeFullTitle.setOnClickListener(listener);
        mScreenshotModePartialTitle.setOnClickListener(listener);
        mScreenshotModeFullSummary.setOnClickListener(listener);
        mScreenshotModePartialSummary.setOnClickListener(listener);

        builder.setPositiveButton(R.string.select, onClickListener);
        builder.setView(view);
    }

    @Override
    protected void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {

            final Context context = getContext();
            if (mScreenshotModePartialTitle.isChecked()) {
                Log.v(TAG, "Set to partial screenshot mode");
            } else {
                Log.v(TAG, "Set to fullscreen screenshot mode");
            }
        }
    }
}
