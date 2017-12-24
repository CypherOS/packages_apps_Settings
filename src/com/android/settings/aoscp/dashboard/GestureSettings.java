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

package com.android.settings.aoscp.dashboard;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.Bundle;
import android.provider.SearchIndexableResource;

import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settings.aoscp.gestures.DoubleTapPowerPreferenceController;
import com.android.settings.aoscp.gestures.DoubleTwistPreferenceController;
import com.android.settings.aoscp.gestures.SwipeToNotificationPreferenceController;
import com.android.settings.aoscp.gestures.TapToSleepPreferenceController;
import com.android.settings.aoscp.gestures.TapToWakePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GestureSettings extends DashboardFragment implements Indexable {

    private static final String LOG_TAG = "GestureSettings";
			
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.gesture_settings_summary);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.GESTURE_SETTINGS;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_about;
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.gesture_settings;
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getActivity(), this /* fragment */,
                getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Activity activity, Fragment fragment, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        AmbientDisplayConfiguration ambientDisplayConfig = new AmbientDisplayConfiguration(context);
        controllers.add(new DoubleTapPowerPreferenceController(context));
        controllers.add(new DoubleTwistPreferenceController(context));
        controllers.add(new SwipeToNotificationPreferenceController(context));
        controllers.add(new TapToSleepPreferenceController(context));
        controllers.add(new TapToWakePreferenceController(context));
        return controllers;
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.gesture_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<AbstractPreferenceController> getPreferenceControllers(Context context) {
                    return buildPreferenceControllers(context, null /*activity */,
                            null /* fragment */, null /* lifecycle */);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}