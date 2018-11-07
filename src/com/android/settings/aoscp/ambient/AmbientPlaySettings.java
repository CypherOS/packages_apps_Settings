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

package com.android.settings.aoscp.ambient;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.ambient.play.AmbientPlayHistoryManager;
import com.android.settings.R;
import com.android.settings.aoscp.ambient.play.AmbientPlayHistoryPreference;
import com.android.settings.aoscp.ambient.play.AmbientPlayKeyguardPreferenceController;
import com.android.settings.aoscp.ambient.play.AmbientPlayNotificationPreferenceController;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmbientPlaySettings extends DashboardFragment implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "AmbientPlaySettings";

    private static final String KEY_AMBIENT_HEADER       = "ambient_header";
    private static final String KEY_AMBIENT_KEYGUARD     = "ambient_recognition_keyguard";
    private static final String KEY_AMBIENT_NOTIFICATION = "ambient_recognition_notification";
	private static final String KEY_AMBIENT_HISTORY      = "ambient_recognition_history";

	private AmbientPlayHistoryPreference mAmbientHistoryPreference;
    private LayoutPreference mHeaderPreference;
    private TextView mTextView;
	
	private BroadcastReceiver onSongMatch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals(AmbientPlayHistoryManager.INTENT_SONG_MATCH.getAction())) {
                if (mAmbientHistoryPreference != null){
                    mAmbientHistoryPreference.updateSummary(getActivity());
                }
            }
        }
    };

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.ambient_play_settings;
    }

    @Override
    public void displayResourceTiles() {
        super.displayResourceTiles();
		mAmbientHistoryPreference = (AmbientPlayHistoryPreference) findPreference(KEY_AMBIENT_HISTORY);
        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.ambient_play_help_text);
    }
	
	@Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(onSongMatch, new IntentFilter(AmbientPlayHistoryManager.INTENT_SONG_MATCH.getAction()));
        if (mAmbientHistoryPreference != null){
            mAmbientHistoryPreference.updateSummary(getActivity());
        }
    }
	
	@Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(onSongMatch);
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final Lifecycle lifecycle = getLifecycle();
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AmbientPlayKeyguardPreferenceController(context, KEY_AMBIENT_KEYGUARD));
        controllers.add(new AmbientPlayNotificationPreferenceController(context, KEY_AMBIENT_NOTIFICATION));
        return controllers;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.ambient_play, container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final boolean isEnabled = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.AMBIENT_RECOGNITION, 0, UserHandle.USER_CURRENT) != 0;
        mTextView = view.findViewById(R.id.switch_text);
        mTextView.setText(getString(isEnabled ? R.string.ambient_play_switch_bar_on : R.string.ambient_play_switch_bar_off));
        View switchBar = view.findViewById(R.id.switch_bar);
        Switch switchWidget = switchBar.findViewById(android.R.id.switch_widget);
        switchWidget.setChecked(isEnabled);
        switchWidget.setOnCheckedChangeListener(this);
        switchBar.setOnClickListener(v -> switchWidget.setChecked(!switchWidget.isChecked()));
    }

    private void initHeader() {
        mHeaderPreference =
                (LayoutPreference) getPreferenceScreen().findPreference(KEY_AMBIENT_HEADER);
        final Activity context = getActivity();
        final Bundle bundle = getArguments();
        final View appSnippet = mHeaderPreference.findViewById(R.id.entity_header);
        EntityHeaderController controller = EntityHeaderController
                .newInstance(context, this, appSnippet)
                .setRecyclerView(getListView(), getLifecycle())
                .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                        EntityHeaderController.ActionType.ACTION_NONE);
        controller.done(context, true);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Settings.System.putIntForUser(getActivity().getContentResolver(),
                Settings.System.AMBIENT_RECOGNITION, isChecked ? 1 : 0, UserHandle.USER_CURRENT);
        mTextView.setText(getString(isChecked ? R.string.ambient_play_switch_bar_on : R.string.ambient_play_switch_bar_off));
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.ambient_play_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    keys.add(KEY_AMBIENT_KEYGUARD);
                    keys.add(KEY_AMBIENT_NOTIFICATION);

                    return keys;
                }
            };
}
