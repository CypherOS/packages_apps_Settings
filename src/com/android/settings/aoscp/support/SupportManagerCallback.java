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

package com.android.settings.aoscp.support;

import android.app.Activity;
import android.annotation.IntDef;
import android.content.Context;
import android.content.Intent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Feature provider for support tab.
 */
public interface SupportManagerCallback {
	
	@IntDef({SupportType.REPORT})
    @Retention(RetentionPolicy.SOURCE)
    @interface SupportType {
        int REPORT = 1;
    }
	
	/**
     * Returns a intent that will open help page.
     */
    Intent getHelpIntent(Context context);
	
	/**
     * Whether or not a support type is enabled.
     */
    boolean isSupportTypeEnabled(Context context, @SupportType int type);
	
	/**
     * Whether or not a support type is operating now.
     */
    boolean isOperatingNow(@SupportType int type);
}
