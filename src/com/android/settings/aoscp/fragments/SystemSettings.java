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

package com.android.settings.aoscp.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceCategory;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.Settings.Secure;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static android.provider.Settings.System.PIXEL_NAV_ANIMATION;
import static android.provider.Settings.Secure.ONE_HANDED_MODE_UI;

public class SystemSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "SystemSettings";
	
    private static final String SCREENSHOT_TYPE = "screenshot_type";
    private static final String PIXEL_NAV_ANIMATION = "pixel_nav_animation";
    private static final String ONE_HANDED_MODE_UI = "one_handed_mode_ui";
    private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
    private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";

    private static final String SCROLLINGCACHE_DEFAULT = "2";

    private ListPreference mScreenshotType;
    private ListPreference mScrollingCachePref;
    private SwitchPreference mNavbarAnimation;
	private SwitchPreference mOneHandedMode;
  
    private int mEnableNavigationBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.system_settings);
		
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();
		
	mEnableNavigationBar = Settings.System.getInt(resolver,
                   Settings.System.DEV_FORCE_SHOW_NAVBAR, 1);
				
        mNavbarAnimation = (SwitchPreference) findPreference(PIXEL_NAV_ANIMATION);
	int nav = Settings.System.getInt(resolver, PIXEL_NAV_ANIMATION, 1);
	mNavbarAnimation.setChecked(nav != 0);
        mNavbarAnimation.setOnPreferenceChangeListener(this);
		
	mOneHandedMode = (SwitchPreference) findPreference(ONE_HANDED_MODE_UI);
	int ohm = Settings.Secure.getInt(resolver, ONE_HANDED_MODE_UI, 0);
	mOneHandedMode.setChecked(ohm != 0);
        mOneHandedMode.setOnPreferenceChangeListener(this);
		
	if (mEnableNavigationBar != 0) {
	    mNavbarAnimation.setEnabled(false);
            mOneHandedMode.setEnabled(false);
        } else {
	    mNavbarAnimation.setEnabled(true);
            mOneHandedMode.setEnabled(true);
        }
		
	mScreenshotType = (ListPreference) findPreference(SCREENSHOT_TYPE);
        int mScreenshotTypeValue = Settings.System.getInt(resolver,
                Settings.System.SCREENSHOT_TYPE, 0);
        mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
        mScreenshotType.setSummary(mScreenshotType.getEntry());
        mScreenshotType.setOnPreferenceChangeListener(this);
		
	mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
        mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
        mScrollingCachePref.setOnPreferenceChangeListener(this);
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if  (preference == mScreenshotType) {
            int mScreenshotTypeValue = Integer.parseInt(((String) newValue).toString());
            mScreenshotType.setSummary(
                    mScreenshotType.getEntries()[mScreenshotTypeValue]);
            Settings.System.putInt(resolver,
                    Settings.System.SCREENSHOT_TYPE, mScreenshotTypeValue);
            mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
            return true;
		} else if (preference == mNavbarAnimation) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.PIXEL_NAV_ANIMATION, value ? 1 : 0);
            return true;
		} else if (preference == mOneHandedMode) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.Secure.ONE_HANDED_MODE_UI, value ? 1 : 0);
            return true;
		} else if (preference == mScrollingCachePref) {
            if (newValue != null) {
                SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String) newValue);
            }
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ADDITIONS;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList<SearchIndexableResource>();

            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.system_settings;
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