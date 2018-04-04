/*
 * Copyright 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.aoscp.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.android.settings.R;

public final class FooterConfirmMixin {

    public static final String TAG = "FooterConfirmMixin";

    private FooterConfirmMixin(Context context) {
		mContext = context;
        mWindowManager = (WindowManager) mContext.getSystemService(
                Context.WINDOW_SERVICE);
    }
	
	private void createConfirmView() {
        mConfirmView = (FooterConfirmMixinView) View.inflate(mContext,
                R.layout.footer_confirm_mixin, null);
        if (mConfirmView != null) attachConfirmView();
    }

    private void attachConfirmView() {
        if (mConfirmView != null) {
            final WindowManager.LayoutParams footerConfirm = new WindowManager.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            footerConfirm.gravity = Gravity.BOTTOM;
            mWindowManager.addView(mConfirmView, footerConfirm);
        }
    }

    public FooterConfirmMixinView getView() {
        createConfirmView();
        return mConfirmView;
    }
	
	public FooterConfirmMixinView getCurrentView() {
		return mConfirmView;
    }

    public void prompt(String content, int duration) {
		getView().show(content, duration);
    }

	public void dismiss() {
		getCurrentView().hide();
    }
}