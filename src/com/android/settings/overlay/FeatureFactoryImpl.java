/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.overlay;

import android.content.Context;
import android.support.annotation.Keep;
import com.android.settings.aoscp.support.SupportManagerCallback;
import com.android.settings.aoscp.support.SupportManagerCallbackImpl;
import com.android.settings.dashboard.SuggestionFeatureProvider;
import com.android.settings.dashboard.SuggestionFeatureProviderImpl;
import com.android.settings.fuelgauge.PowerUsageFeatureProvider;

/**
 * {@link FeatureFactory} implementation for AOSP Settings.
 */
@Keep
public class FeatureFactoryImpl extends FeatureFactory {

    private SuggestionFeatureProvider mSuggestionFeatureProvider;
	private SupportManagerCallback mSupportManagerCallback;

    @Override
    public SupportFeatureProvider getSupportFeatureProvider(Context context) {
        return null;
    }
	
	@Override
    public SupportManagerCallback getSupportManagerCallback(Context context) {
        if (mSupportManagerCallback == null) {
            mSupportManagerCallback = new SupportManagerCallbackImpl();
        }
        return mSupportManagerCallback;
    }

    @Override
    public PowerUsageFeatureProvider getPowerUsageFeatureProvider(Context context) {
        return null;
    }

    @Override
    public SurveyFeatureProvider getSurveyFeatureProvider(Context context) {
        return null;
    }

    @Override
    public SuggestionFeatureProvider getSuggestionFeatureProvider() {
        if (mSuggestionFeatureProvider == null) {
            mSuggestionFeatureProvider = new SuggestionFeatureProviderImpl();
        }
        return mSuggestionFeatureProvider;
    }

}
