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
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.aoscp.buttons.NavigationBarPreferenceController;
import com.android.settings.core.PreferenceController;
import com.android.settings.core.lifecycle.Lifecycle;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.FooterPreferenceMixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ButtonSettings extends DashboardFragment implements Indexable {

    private static final String LOG_TAG = "ButtonSettings";
	
	private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    
    private final FooterPreferenceMixin mFooterPreferenceMixin =
            new FooterPreferenceMixin(this, getLifecycle());

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.button_settings_description);
    }

    @Override
    public int getMetricsCategory() {
        return -1;
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
        return R.xml.button_settings;
    }

    @Override
    protected List<PreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getActivity(), this /* fragment */,
                getLifecycle());
    }

    private static List<PreferenceController> buildPreferenceControllers(Context context,
            Activity activity, Fragment fragment, Lifecycle lifecycle) {
        final List<PreferenceController> controllers = new ArrayList<>();
		controllers.add(new NavigationBarPreferenceController(context, KEY_NAVIGATION_BAR));
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
                    sir.xmlResId = R.xml.button_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<PreferenceController> getPreferenceControllers(Context context) {
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
