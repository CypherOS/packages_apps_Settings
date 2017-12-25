/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.aoscp.display;

import android.content.Context;
import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.aoscp.display.themes.ThemeDefaultPreferenceController;
import com.android.settings.aoscp.display.themes.ThemeTealPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;

public class ThemePreferenceFragment extends SettingsPreferenceFragment {

    private static final String TAG = "ThemePreference";
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		addPreferencesFromResource(R.xml.theme_settings);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }
	
	@Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getActivity(), this /* fragment */,
                getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Activity activity, Fragment fragment, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
		controllers.add(new ThemeDefaultPreferenceController(context));
		controllers.add(new ThemeTealPreferenceController(context));
        return controllers;
    }
}
