package com.android.settings.aoscp.ambient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

import com.android.internal.aoscp.AmbientHistoryData;
import com.android.internal.aoscp.AmbientHistoryManager;

import com.android.settings.R;

import java.util.List;

public class AmbientPlayHistoryPreference extends Preference implements Preference.OnPreferenceClickListener {
    private Context mContext;

    public AmbientPlayHistoryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        setTitle(R.string.ambient_play_history);
        setIcon(R.drawable.now_playing_history);
        setOnPreferenceClickListener(this);
        updateSummary(context);
    }

    public AmbientPlayHistoryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setTitle(R.string.ambient_play_history);
        setIcon(R.drawable.now_playing_history);
        setOnPreferenceClickListener(this);
        updateSummary(context);
    }

    public AmbientPlayHistoryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setTitle(R.string.ambient_play_history);
        setIcon(R.drawable.now_playing_history);
        setOnPreferenceClickListener(this);
        updateSummary(context);
    }

    public AmbientPlayHistoryPreference(Context context) {
        super(context);
        mContext = context;
        setTitle(R.string.ambient_play_history);
        setIcon(R.drawable.now_playing_history);
        setOnPreferenceClickListener(this);
        updateSummary(context);
    }

    public void updateSummary(Context context) {
        List<AmbientHistoryData> songs = AmbientHistoryManager.getSongs(context);
        if (songs.size() < 1) {
            setSummary(R.string.ambient_play_history_empty);
        } else {
            AmbientHistoryData entry = songs.get(0);
            AmbientPlayHistoryPreferenceEntry preference = new AmbientPlayHistoryPreferenceEntry(entry.getSongID(), entry.geMatchTimestamp(), entry.getSongTitle(), entry.getArtistTitle(), context);
            setSummary(preference.getFormattedSummary());
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$AmbientPlayHistoryActivity"));
            mContext.startActivity(intent);
        } catch (Exception ignored) {
        }
        return false;
    }
}
