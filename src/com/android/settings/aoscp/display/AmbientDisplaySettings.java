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

package com.android.settings.aoscp.display;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Switch;

import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.core.PreferenceController;
import com.android.settings.core.lifecycle.Lifecycle;
import com.android.settings.display.DozePreferenceController;
import com.android.settings.gestures.PickupGesturePreferenceController;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.notification.SettingPref;
import com.android.settings.widget.SwitchBar;

import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.Secure.DOZE_ENABLED;

public class AmbientDisplaySettings extends SettingsPreferenceFragment
        implements SwitchBar.OnSwitchChangeListener {
    private static final String TAG = "AmbientDisplaySettings";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private static final long WAIT_FOR_SWITCH_ANIM = 500;
	private static final String KEY_DOZE_ENABLED = "doze_enabled";

    private final Handler mHandler = new Handler();
    private final SettingsObserver mSettingsObserver = new SettingsObserver(mHandler);

    private Context mContext;
    private boolean mCreated;
    private SettingPref mTriggerPref;
    private SwitchBar mSwitchBar;
    private Switch mSwitch;
    private boolean mValidListener;
	
	private DozePreferenceController mDozeController;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.AMBIENT_DISPLAY_SETTINGS;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCreated) {
            mSwitchBar.show();
            return;
        }
        mCreated = true;
        addPreferencesFromResource(R.xml.ambient_display_settings);
        mFooterPreferenceMixin.createFooterPreference()
                .setTitle(R.string.ambient_display_settings_description);
        mContext = getActivity();
        mSwitchBar = ((SettingsActivity) mContext).getSwitchBar();
        mSwitch = mSwitchBar.getSwitch();
        mSwitchBar.show();

        mTriggerPref = new SettingPref(SettingPref.TYPE_SECURE, KEY_DOZE_ENABLED,
         		Secure.DOZE_ENABLED, 1 /*default*/) {
            @Override
            protected String getCaption(Resources res, int value) {
                return res.getString(R.string.doze_summary);
            }
        };
        mTriggerPref.init(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSwitchBar.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSettingsObserver.setListening(true);
        if (!mValidListener) {
            mSwitchBar.addOnSwitchChangeListener(this);
            mValidListener = true;
        }
        updateSwitch();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSettingsObserver.setListening(false);
        if (mValidListener) {
            mSwitchBar.removeOnSwitchChangeListener(this);
            mValidListener = false;
        }
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        mHandler.removeCallbacks(mStartDoze);
        if (isChecked) {
            mHandler.postDelayed(mStartDoze, WAIT_FOR_SWITCH_ANIM);
        } else {
            if (DEBUG) Log.d(TAG, "Disabling doze mode");
            trySetDozeMode(false);
        }
    }

    private void trySetDozeMode(boolean mode) {
        if (!mode) {
            if (DEBUG) Log.d(TAG, "Setting mode failed, fallback to current value");
            mHandler.post(mUpdateSwitch);
        }
    }

    private void updateSwitch() {
        final boolean mode = mDozeController.isDozeActivated();
		Settings.Secure.putInt(mContext.getContentResolver(), DOZE_ENABLED, mode ? 1 : 0);
        if (DEBUG) Log.d(TAG, "updateSwitch: isChecked=" + mSwitch.isChecked() + " mode=" + mode);
        if (mode == mSwitch.isChecked()) return;

        // set listener to null so that that code below doesn't trigger onCheckedChanged()
        if (mValidListener) {
            mSwitchBar.removeOnSwitchChangeListener(this);
        }
        mSwitch.setChecked(mode);
        if (mValidListener) {
            mSwitchBar.addOnSwitchChangeListener(this);
        }
    }
	
    private final Runnable mUpdateSwitch = new Runnable() {
        @Override
        public void run() {
            updateSwitch();
        }
    };

    private final Runnable mStartDoze = new Runnable() {
        @Override
        public void run() {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "Applying new doze configuration");
                    Settings.Secure.putInt(mContext.getContentResolver(), DOZE_ENABLED, 1);
                }
            });
        }
    };

    private final class SettingsObserver extends ContentObserver {
        private final Uri DOZE_ENABLED
                = Secure.getUriFor(Secure.DOZE_ENABLED);

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (DOZE_ENABLED.equals(uri)) {
                mTriggerPref.update(mContext);
            }
        }

        public void setListening(boolean listening) {
            final ContentResolver resolver = getContentResolver();
            if (listening) {
                resolver.registerContentObserver(DOZE_ENABLED, false, this);
            } else {
                resolver.unregisterContentObserver(this);
            }
        }
    }
	
    protected List<PreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }
	
	private static List<PreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<PreferenceController> controllers = new ArrayList<>();
        AmbientDisplayConfiguration ambientDisplayConfig = new AmbientDisplayConfiguration(context);
        controllers.add(new PickupGesturePreferenceController(context,
                new AmbientDisplayConfiguration(context), UserHandle.myUserId()));
        return controllers;
    }
	
	public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();

                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.ambient_display_settings;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<PreferenceController> getPreferenceControllers(Context context) {
                    return buildPreferenceControllers(context, null);
                }
            };
}