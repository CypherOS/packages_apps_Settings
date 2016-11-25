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

public class SoftwareInfo extends SettingsPreferenceFragment implements
        Indexable {
    private static final String TAG = "SoftwareInfo";
	
	Preference mWebsiteUrl;
    Preference mGoogleUrl;
	Preference mGithubUrl;
	Preference mGerritUrl;
	Preference mGoogleUrl1;
	Preference mGoogleUrl2;
	Preference mGoogleUrl3;
	Preference mGoogleUrl4;
	Preference mGoogleUrl5;
	Preference mGoogleUrl6;
	Preference mGoogleUrl7;
        Preference mGoogleUrl8;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.software_info);
		
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();
		
		mWebsiteUrl = findPreference("on_the_web");
        mGoogleUrl = findPreference("cypher_plus");
		mGithubUrl = findPreference("github_source");
		mGerritUrl = findPreference("gerrit_review");
		mGoogleUrl1 = findPreference("mani_kumar_plus");
		mGoogleUrl2 = findPreference("chris_crump_plus");
		mGoogleUrl3 = findPreference("ken_adams_plus");
		mGoogleUrl4 = findPreference("isaiah_pez_plus");
		mGoogleUrl5 = findPreference("rahul_s_nair_plus");
		mGoogleUrl6 = findPreference("jonathan_bruno_plus");
		mGoogleUrl7 = findPreference("filipe_carreto_plus");
      		mGoogleUrl8 = findPreference("abhishek_kaushik_plus");
	}
	
	@Override
    protected int getMetricsCategory() {
        return MetricsEvent.ADDITIONS;
    }
	
	@Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mWebsiteUrl) {
            launchUrl("http://cypheros.co");
        } else if (preference == mGoogleUrl) {
            launchUrl("https://plus.google.com/communities/111402352496339801246");
		} else if (preference == mGithubUrl) {
            launchUrl("https://github.com/CypherOS");
		} else if (preference == mGerritUrl) {
            launchUrl("http://gerrit.cypheros.co/");
		} else if (preference == mGoogleUrl1) {
            launchUrl("https://plus.google.com/u/0/103704565326077746082");
		} else if (preference == mGoogleUrl2) {
            launchUrl("https://plus.google.com/u/0/109812248604746035480");
		} else if (preference == mGoogleUrl3) {
            launchUrl("https://plus.google.com/u/0/107972764100526423734");
		} else if (preference == mGoogleUrl4) {
            launchUrl("https://plus.google.com/u/0/+IsaiahPEz");
		} else if (preference == mGoogleUrl5) {
            launchUrl("https://plus.google.com/u/0/+RahulSNair30");
		} else if (preference == mGoogleUrl6) {
            launchUrl("https://plus.google.com/u/0/+jonathanbruno21");
		} else if (preference == mGoogleUrl7) {
            launchUrl("https://plus.google.com/u/0/+FilipeCarreto");
		} else if (preference == mGoogleUrl8) {
            launchUrl("https://plus.google.com/101368184428720999404");
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(intent);
    }
	
	public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.software_info;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}