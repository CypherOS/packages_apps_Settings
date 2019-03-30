/*
 * Copyright (C) 2019 CypherOS
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

package com.android.settings.aoscp.display;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.view.View;

import aoscp.support.lottie.LottieAnimationView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.ArrayList;
import java.util.List;

public class AmbientPlaySettings extends DashboardFragment implements Indexable {

    private static final String TAG = "AmbientPlaySettings";
	private static final String KEY_AMBIENT_PLAY_ILLUSTRATION = "ambient_play_illustration";

	private LayoutPreference mHeaderPreference;
	private LottieAnimationView mAnimationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
    public void onResume() {
        super.onResume();
        initHeader();
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.ambient_play_settings;
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    private static List <AbstractPreferenceController> buildPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>(1);
		controllers.add(new AmbientPlayKeyguardPreferenceController(context));
        controllers.add(new AmbientPlayFooterPreferenceController(context));
        return controllers;
    }

	private void initHeader() {
		mHeaderPreference =
                (LayoutPreference) getPreferenceScreen().findPreference(KEY_AMBIENT_PLAY_ILLUSTRATION);
		mAnimationView = (LottieAnimationView) mHeaderPreference.findViewById(R.id.header_ambient_play);
		mAnimationView.setRepeatCount(ValueAnimator.INFINITE);
		mAnimationView.playAnimation();
	}

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.ambient_play_settings;
                    result.add(sir);
                    return result;
                }

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return true;
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                    Context context) {
                    return buildPreferenceControllers(context);
                }
            };
}
