/*
 * Copyright (C) 2016 CypherOS
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

package com.android.settings.cypher.deviceinfo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import java.util.ArrayList;
import java.util.List;

public class Affiliation extends SettingsPreferenceFragment {
    private static final String TAG = "Affiliation";
	
    Preference mGoogleUrlChris;
    Preference mGoogleUrlAaron;
    Preference mGoogleUrlMani;
    Preference mGoogleUrlKen;
    Preference mGoogleUrlIsaiah;
    Preference mGoogleUrlRahul;
    Preference mGoogleUrlJonathan;
    Preference mGoogleUrlFilipe;
    Preference mGoogleUrlAbhishek;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.affiliation_settings);
		
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();
		
	mGoogleUrlChris = findPreference("developers_chris");
	mGoogleUrlAaron = findPreference("developers_aaron");
	mGoogleUrlMani = findPreference("community_mani");
	mGoogleUrlKen = findPreference("maintainer_ken");
	mGoogleUrlIsaiah = findPreference("maintainer_isaiah");
	mGoogleUrlRahul = findPreference("maintainer_rahul");
	mGoogleUrlJonathan = findPreference("maintainer_jonathan");
	mGoogleUrlFilipe = findPreference("maintainer_filipe");
	mGoogleUrlAbhishek = findPreference("maintainer_abhishek");
		
    }
	
    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ADDITIONS;
    }
	
	@Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mGoogleUrlChris) {
            launchUrl("https://plus.google.com/u/0/109812248604746035480");
	} else if (preference == mGoogleUrlAaron) {
            launchUrl("https://plus.google.com/+AaronNixon");
	} else if (preference == mGoogleUrlMani) {
            launchUrl("https://plus.google.com/u/0/103704565326077746082");
	} else if (preference == mGoogleUrlKen) {
            launchUrl("https://plus.google.com/u/0/107972764100526423734");
	} else if (preference == mGoogleUrlIsaiah) {
            launchUrl("https://plus.google.com/u/0/+IsaiahPEz");
	} else if (preference == mGoogleUrlRahul) {
            launchUrl("https://plus.google.com/u/0/+RahulSNair30");
	} else if (preference == mGoogleUrlJonathan) {
            launchUrl("https://plus.google.com/u/0/+jonathanbruno21");
	} else if (preference == mGoogleUrlFilipe) {
            launchUrl("https://plus.google.com/u/0/+FilipeCarreto");
	} else if (preference == mGoogleUrlAbhishek) {
            launchUrl("https://plus.google.com/101368184428720999404");
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(intent);
    }
}