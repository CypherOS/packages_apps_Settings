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

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public final class FooterConfirmMixin {

    public static final String TAG = "FooterConfirmMixin";

    private FooterConfirmMixin() {
		// No op
    }

    public static void prompt(FooterConfirmMixinView footerConfirm, String content) {
        if (content == null) {
            throw new IllegalArgumentException("content == null");
        }
		footerConfirm.show(content);
    }
	
	public static void dismiss(FooterConfirmMixinView footerConfirm) {
		footerConfirm.hide();
    }
}