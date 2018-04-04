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
 * limitations under the License
 */

package com.android.settings.aoscp;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.android.settings.R;

import com.android.settings.aoscp.colormanager.ColorManagerFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import java.util.ArrayList;
import java.util.List;


/**
 * The activity used to launch the configured Backup activity or the preference screen
 * if the manufacturer provided their backup settings.
 */
public class ColorManagerActivity extends Activity implements Indexable {
	
    private static final String TAG = "ColorManagerActivity";
    private FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mFragmentManager == null) {
            mFragmentManager = getFragmentManager();
        }
        mFragmentManager.beginTransaction()
                .replace(android.R.id.content, new ColorManagerFragment())
                .commit();
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                private static final String COLOR_MANAGER_INDEX_KEY = "colormanager";

                @Override
                public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                        boolean enabled) {

                    final List<SearchIndexableRaw> result = new ArrayList<>();

                    // Add the activity title
                    SearchIndexableRaw data = new SearchIndexableRaw(context);
                    data.title = context.getString(R.string.color_manager_title);
                    data.screenTitle = context.getString(R.string.settings_label);
                    data.intentTargetClass = ColorManagerActivity.class.getName();
                    data.intentAction = Intent.ACTION_MAIN;
                    data.key = COLOR_MANAGER_INDEX_KEY;
                    result.add(data);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);

                    if (UserHandle.myUserId() != UserHandle.USER_SYSTEM) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "Not a system user, not indexing the screen");
                        }
                        keys.add(COLOR_MANAGER_INDEX_KEY);
                    }

                    return keys;
                }
            };

    @VisibleForTesting
    void setFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }
}