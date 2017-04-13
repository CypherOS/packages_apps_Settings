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

import android.content.Context;
import android.content.Intent;

public class SupportManagerCallbackImpl implements SupportManagerCallback {
	
	static final String LAUNCH_HELP_TIPS = "http://cypheros.co/help";
	
	@Override
	public Intent getHelpIntent() {
        Intent intent = new Intent(LAUNCH_HELP_TIPS);
    }

    @Override
    public boolean isSupportTypeEnabled(Context context, @SupportType int type) {
        return false;
    }

    @Override
    public boolean isOperatingNow(@SupportType int type) {
        return false;
    }

}
