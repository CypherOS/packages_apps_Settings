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

package com.android.settings.aoscp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class CarrierLabelDialog extends DialogFragment implements OnClickListener {

    private static final String TAG_CARRIER_DIALOG = "carrierdialog";

    private View mInput;
    private EditText mCustomInput;
	private String mInputHint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mInput = LayoutInflater.from(getActivity()).inflate(R.layout.carrierlabel, null);
        initInput();
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.carrier_label_settings_title)
                .setView(mInput)
                .setPositiveButton(R.string.save, this)
                .setNegativeButton(R.string.cancel, this)
                .show();
    }

    private void initInput() {
        String hint = mInputHint.setSummary(R.string.carrierlabel_edit_text_hint);

        mCustomInput = (EditText) mInput.findViewById(R.id.carrierlabel_edit_text);
        if (!TextUtils.isEmpty(hint)) {
            mCustomInput.setText(hint);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            String hint = mCustomInput.getText().toString();
            mInputHint.setInputHintEnabled(!TextUtils.isEmpty(hint));
            mInputHint.setInputHint(hint);

            if (getTargetFragment() instanceof CarrierLabel) {
                ((CarrierLabel) getTargetFragment()).updateInputSummary();
            }
        }
    }

    public static void show(Fragment parent) {
        if (!parent.isAdded()) return;

        final CarrierLabelDialog dialog = new CarrierLabelDialog();
        dialog.setTargetFragment(parent, 0);
        dialog.show(parent.getFragmentManager(), TAG_CARRIER_DIALOG);
    }
}
