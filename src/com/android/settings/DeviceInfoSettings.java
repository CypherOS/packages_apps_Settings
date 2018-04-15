/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.provider.SearchIndexableResource;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.deviceinfo.AdditionalSystemUpdatePreferenceController;
import com.android.settings.deviceinfo.BasebandVersionPreferenceController;
import com.android.settings.deviceinfo.BuildNumberPreferenceController;
import com.android.settings.deviceinfo.DeviceModelPreferenceController;
import com.android.settings.deviceinfo.FccEquipmentIdPreferenceController;
//import com.android.settings.deviceinfo.FeedbackPreferenceController;
import com.android.settings.deviceinfo.FirmwareVersionPreferenceController;
import com.android.settings.deviceinfo.KernelVersionPreferenceController;
import com.android.settings.deviceinfo.ManualPreferenceController;
//import com.android.settings.deviceinfo.RegulatoryInfoPreferenceController;
import com.android.settings.deviceinfo.SELinuxStatusPreferenceController;
import com.android.settings.deviceinfo.SafetyInfoPreferenceController;
import com.android.settings.deviceinfo.SecurityPatchPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DeviceInfoSettings extends DashboardFragment implements Indexable {

    private static final String LOG_TAG = "DeviceInfoSettings";

    private static final String KEY_ABOUT_HEADER = "about_header";
    private static final String KEY_LEGAL_CONTAINER = "legal_container";

    private LayoutPreference mHeaderLayoutPref;
    private long[] mHits = new long[3];

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DEVICEINFO;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_about;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final BuildNumberPreferenceController buildNumberPreferenceController =
                getPreferenceController(BuildNumberPreferenceController.class);
        if (buildNumberPreferenceController.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.device_info_settings;
    }

    @Override
    public void displayResourceTiles() {
        final int resId = getPreferenceScreenResId();
        if (resId <= 0) {
            return;
        }
        addPreferencesFromResource(resId);
        final PreferenceScreen screen = getPreferenceScreen();
        Collection<AbstractPreferenceController> controllers = mPreferenceControllers.values();
        for (AbstractPreferenceController controller : controllers) {
            controller.displayPreference(screen);
        }

        mHeaderLayoutPref = (LayoutPreference) findPreference(KEY_ABOUT_HEADER);
        updateHeaderPreference();
    }

    private void updateHeaderPreference() {
        final Context context = getContext();
        if (context == null) {
            return;
        }
        final ImageView icon = (ImageView) mHeaderLayoutPref
                .findViewById(R.id.header_icon);
		icon.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable anim = (AnimationDrawable) icon.getDrawable();
				if (!anim.isRunning()) {
					anim.start();
				}
			}
		});
        icon.setClickable(true);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                    final Intent intent = new Intent(Intent.ACTION_MAIN)
                            .putExtra("aoscp", true)
                            .setClassName(
                                    "android", com.android.internal.app.PlatLogoActivity.class.getName());
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Unable to start activity " + intent.toString());
                    }
                }
            }
        });
        final TextView version = (TextView) mHeaderLayoutPref.findViewById(R.id.version);
        final TextView versionInfo = (TextView) mHeaderLayoutPref.findViewById(R.id.version_info);
        final TextView buildInfo = (TextView) mHeaderLayoutPref.findViewById(R.id.build_info);
        final TextView buildNumber = (TextView) mHeaderLayoutPref.findViewById(R.id.build_number);

        version.setText(context.getResources().getString(R.string.aoscp_version));
        versionInfo.setText(String.format(
                context.getResources().getString(R.string.aoscp_version_info), 
                Build.AOSCP.VERSION, Build.AOSCP.CODENAME));

        buildInfo.setText(context.getResources().getString(R.string.aoscp_build));
        buildNumber.setText(String.format(
                context.getResources().getString(R.string.aoscp_build_info), 
                Build.AOSCP.BUILD_NUMBER));
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getActivity(), this /* fragment */,
                getLifecycle());
    }

    private static class SummaryProvider implements SummaryLoader.SummaryProvider {

        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(SummaryLoader summaryLoader) {
            mSummaryLoader = summaryLoader;
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                mSummaryLoader.setSummary(this, DeviceModelPreferenceController.getDeviceModel());
            }
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                SummaryLoader summaryLoader) {
            return new SummaryProvider(summaryLoader);
        }
    };

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            Activity activity, Fragment fragment, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(
                new BuildNumberPreferenceController(context, activity, fragment, lifecycle));
        controllers.add(new AdditionalSystemUpdatePreferenceController(context));
        controllers.add(new ManualPreferenceController(context));
        //controllers.add(new FeedbackPreferenceController(fragment, context));
        controllers.add(new KernelVersionPreferenceController(context));
        controllers.add(new BasebandVersionPreferenceController(context));
        controllers.add(new FirmwareVersionPreferenceController(context, lifecycle));
        //controllers.add(new RegulatoryInfoPreferenceController(context));
        controllers.add(new DeviceModelPreferenceController(context, fragment));
        controllers.add(new SecurityPatchPreferenceController(context));
        controllers.add(new FccEquipmentIdPreferenceController(context));
        controllers.add(new SELinuxStatusPreferenceController(context));
        controllers.add(new SafetyInfoPreferenceController(context));
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
                    sir.xmlResId = R.xml.device_info_settings;
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
                    keys.add(KEY_LEGAL_CONTAINER);
                    return keys;
                }
            };
}
