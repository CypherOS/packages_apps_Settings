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

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.aoscp.display.themes.ThemeDefaultPreferenceController;
import com.android.settings.aoscp.display.themes.ThemeTealPreferenceController;
import com.android.settings.core.instrumentation.MetricsFeatureProvider;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;

public class ThemePreferenceFragment extends DashboardFragment {

    private static final String TAG = "ThemePreference";
	
	private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Lifecycle lifecycle, MetricsFeatureProvider metricsFeatureProvider) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AmbientDisplayNotificationsPreferenceController(context, config,
                metricsFeatureProvider));
        controllers.add(new ThemeDefaultPreferenceController(context));
        controllers.add(new ThemeTealPreferenceController(context));
        return controllers;
    }
	
	@Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.theme_settings;
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle(), mMetricsFeatureProvider);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }
}
