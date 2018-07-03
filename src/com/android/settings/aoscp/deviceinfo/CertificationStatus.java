/*
 * Copyright (C) 2018 CypherOS
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

package com.android.settings.aoscp.deviceinfo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.UserHandle;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settings.R;
import com.android.settings.aoscp.deviceinfo.devicestatus.BuildNumberAoscpPreferenceController;
import com.android.settings.aoscp.deviceinfo.devicestatus.MaintenancePatchPreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CertificationStatus extends DashboardFragment implements Indexable {

    private static final String LOG_TAG = "CertificationStatus";

    private static String mStatus;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.device_status;
    }

    @Override
    public void displayResourceTiles() {
        super.displayResourceTiles();
        mStatus = SystemProperties.get("ro.aoscp.releasetype");
        if ("unofficial".equals(mStatus)) {
            mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.device_certification_status_desc);
        }
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
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getActivity(), this /* fragment */,
                getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Activity activity, Fragment fragment, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new MaintenancePatchPreferenceController(context));
        controllers.add(new BuildNumberAoscpPreferenceController(context));
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
                    sir.xmlResId = R.xml.device_status;
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