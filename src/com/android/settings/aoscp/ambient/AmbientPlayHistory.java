/*
 * Copyright (C) 2018 PixelExperience
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package com.android.settings.aoscp.ambient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.internal.aoscp.AmbientHistoryData;
import com.android.internal.aoscp.AmbientHistoryManager;
import com.android.settings.aoscp.utils.TimeDateUtils;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Settings.AmbientPlayHistoryActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AmbientPlayHistory extends SettingsPreferenceFragment implements Preference.OnPreferenceClickListener {

	private static final String TAG = "AmbientPlayHistory";

    private ProgressDialog mProgress;
    private Map<String, List<AmbientPlayHistoryPreferenceEntry>> mAllSongs = new HashMap<>();
    private boolean mRunning = false;
    private AmbientPlayHistoryDialogEntry mDialog;

    private BroadcastReceiver mSongMatched = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals(AmbientHistoryManager.INTENT_SONG_MATCH.getAction())) {
                updateList();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ambient_play_history);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDivider(null);
    }

    private void updateList() {
        if (mRunning) {
            return;
        }
        mRunning = true;
        getPreferenceScreen().removeAll();
        mAllSongs.clear();
        try {
            if (mDialog != null){
                mDialog.dismiss();
            }
        }catch(Exception ignored){
        }
        mProgress = new ProgressDialog(getActivity());
        mProgress.setCancelable(false);
        mProgress.setMessage(getString(R.string.ambient_play_loading_data));
        mProgress.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(500);
                List<AmbientHistoryData> songs = AmbientHistoryManager.getSongs(getActivity());
                for (AmbientHistoryData entry : songs) {
                    AmbientPlayHistoryPreferenceEntry entryPref = new AmbientPlayHistoryPreferenceEntry(entry.getSongID(), entry.geMatchTimestamp(), entry.getSongTitle(), entry.getArtistTitle(), getActivity());
                    String key = TimeDateUtils.getDaysAgo(getActivity(), entryPref.geMatchTimestamp(),true);
                    if (mAllSongs.containsKey(key)) {
                        mAllSongs.get(key).add(entryPref);
                    } else {
                        mAllSongs.put(key, new ArrayList<AmbientPlayHistoryPreferenceEntry>());
                        mAllSongs.get(key).add(entryPref);
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, List<AmbientPlayHistoryPreferenceEntry>> treeMap = new TreeMap<>(mAllSongs);
                        for (String date : treeMap.keySet()) {
                            final List<AmbientPlayHistoryPreferenceEntry> songsByDate = mAllSongs.get(date);
                            final PreferenceCategory cat = new PreferenceCategory(getActivity());
                            String catTitle = TimeDateUtils.getDaysAgo(getActivity(), songsByDate.get(0).geMatchTimestamp(),false);
                            cat.setTitle(catTitle);
                            cat.setKey(date);
                            getPreferenceScreen().addPreference(cat);
                            for (final AmbientPlayHistoryPreferenceEntry songEntry : songsByDate) {
                                cat.addPreference(songEntry);
                                songEntry.setPreferenceCategory(cat);
                                songEntry.setOnPreferenceClickListener(AmbientPlayHistory.this);
                            }
                        }
                        mProgress.dismiss();
                        updateListState();
                        mRunning = false;
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mSongMatched, new IntentFilter(AmbientHistoryManager.INTENT_SONG_MATCH.getAction()));
        updateList();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mSongMatched);
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    private void updateListState() {
        if (mAllSongs.size() < 1) {
            Preference pref = new Preference(getActivity());
            pref.setLayoutResource(R.layout.ambient_play_preference_empty_list);
            pref.setTitle(R.string.ambient_play_history_empty);
            pref.setSelectable(false);
            pref.setEnabled(true);
            getPreferenceScreen().addPreference(pref);
        }
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ambient_play_history, menu);
        menu.getItem(0).setVisible(mAllSongs.size() > 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove_all:
                showRemoveAllDialog();
                return true;
            case R.id.action_add_to_home:
                createShortcut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showRemoveAllDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.ambient_play_history_menu_remove_all));
        builder.setMessage(getString(R.string.ambient_play_history_dialog_remove_all_confirmation));
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AmbientHistoryManager.deleteAll(getActivity());
                updateList();
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        builder.show();
    }

    private void createShortcut() {
        try {
            ShortcutManager shortcutManager = getActivity().getSystemService(ShortcutManager.class);
            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(getActivity(), "now_playing_history")
                    .setIntent(new Intent(Intent.ACTION_VIEW, null, getActivity(), AmbientPlayHistoryActivity.class))
                    .setShortLabel(getString(R.string.ambient_play_history))
                    .setIcon(Icon.createWithResource(getActivity(), R.drawable.ambient_play_launcher))
                    .build();
            shortcutManager.requestPinShortcut(shortcutInfo, null);
        } catch (Exception e) {
            Log.e(TAG, "Error when creating shortcut", e);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference instanceof AmbientPlayHistoryPreferenceEntry) {
            final AmbientPlayHistoryPreferenceEntry entry = (AmbientPlayHistoryPreferenceEntry) preference;
            mDialog = new AmbientPlayHistoryDialogEntry(getActivity(), entry);
            mDialog.getRemoveButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();
                    try {
                        AmbientHistoryManager.deleteSong(entry.getSongID(), getActivity());
                        mAllSongs.get(entry.getPreferenceCategory().getKey()).remove(entry);
                        entry.removePreference();
                        if (mAllSongs.get(entry.getPreferenceCategory().getKey()).size() < 1) {
                            mAllSongs.remove(entry.getPreferenceCategory().getKey());
                            getPreferenceScreen().removePreference(entry.getPreferenceCategory());
                        }
                    } catch (Exception ignored) {
                    }
                    updateListState();
                }
            });
            mDialog.show();
        }
        return false;
    }
}
