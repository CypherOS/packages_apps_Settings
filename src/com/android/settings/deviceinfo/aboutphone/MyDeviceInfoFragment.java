/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.deviceinfo.aboutphone;

import static com.android.settings.bluetooth.Utils.getLocalBtManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.deviceinfo.BluetoothAddressPreferenceController;
import com.android.settings.deviceinfo.BrandedAccountPreferenceController;
import com.android.settings.deviceinfo.BuildNumberPreferenceController;
import com.android.settings.deviceinfo.DeviceModelPreferenceController;
import com.android.settings.deviceinfo.DeviceNamePreferenceController;
import com.android.settings.deviceinfo.FccEquipmentIdPreferenceController;
import com.android.settings.deviceinfo.FeedbackPreferenceController;
import com.android.settings.deviceinfo.IpAddressPreferenceController;
import com.android.settings.deviceinfo.WifiMacAddressPreferenceController;
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionPreferenceController;
import com.android.settings.deviceinfo.simstatus.SimStatusPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.users.UserSettings;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import aoscp.support.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyDeviceInfoFragment extends DashboardFragment
        implements DeviceNamePreferenceController.DeviceNamePreferenceHost {
    private static final String LOG_TAG = "MyDeviceInfoFragment";

    private static final String KEY_MY_DEVICE_INFO_HEADER = "my_device_info_header";
    private static final String KEY_LEGAL_CONTAINER = "legal_container";

    private static final int MENU_MULTI_USER = Menu.FIRST;
    private int mMultiUserVersion;
    private long[] mHits = new long[3];

    private LayoutPreference mHeaderPreference;
    private LottieAnimationView mAnimationView;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DEVICEINFO;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_about;
    }

    @Override
    public void onResume() {
        super.onResume();
        initHeader();
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.my_device_info;
    }

    @Override
    public void displayResourceTiles() {
        super.displayResourceTiles();
        mMultiUserVersion = UserManager.getMultiUserVersion();
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getActivity(), this /* fragment */,
                getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context,
            Activity activity,
            MyDeviceInfoFragment fragment,
            Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new BrandedAccountPreferenceController(context));
        DeviceNamePreferenceController deviceNamePreferenceController =
                new DeviceNamePreferenceController(context);
        deviceNamePreferenceController.setLocalBluetoothManager(getLocalBtManager(context));
        deviceNamePreferenceController.setHost(fragment);
        if (lifecycle != null) {
            lifecycle.addObserver(deviceNamePreferenceController);
        }
        controllers.add(deviceNamePreferenceController);
        controllers.add(new SimStatusPreferenceController(context, fragment));
        controllers.add(new DeviceModelPreferenceController(context, fragment));
        controllers.add(new FirmwareVersionPreferenceController(context, fragment));
        controllers.add(new IpAddressPreferenceController(context, lifecycle));
        controllers.add(new WifiMacAddressPreferenceController(context, lifecycle));
        controllers.add(new BluetoothAddressPreferenceController(context, lifecycle));
        controllers.add(new FeedbackPreferenceController(fragment, context));
        controllers.add(new FccEquipmentIdPreferenceController(context));
        controllers.add(
                new BuildNumberPreferenceController(context, activity, fragment, lifecycle));
        return controllers;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final BuildNumberPreferenceController buildNumberPreferenceController =
            use(BuildNumberPreferenceController.class);
        if (buildNumberPreferenceController.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initHeader() {
        mHeaderPreference =
                (LayoutPreference) getPreferenceScreen().findPreference(KEY_MY_DEVICE_INFO_HEADER);
        final Activity context = getActivity();
        /* Todo: Convert into a fallback for MultiUserV1
        if (mMultiUserVersion == UserManager.MULTI_USER_V1) {
            // TODO: Migrate into its own controller.
            final Bundle bundle = getArguments();
            final View appSnippet = mHeaderPreference.findViewById(R.id.entity_header);
            EntityHeaderController controller = EntityHeaderController
                    .newInstance(context, this, appSnippet)
                    .setRecyclerView(getListView(), getLifecycle())
                    .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                            EntityHeaderController.ActionType.ACTION_NONE);

            // TODO: There may be an avatar setting action we can use here.
            final int iconId = bundle.getInt("icon_id", 0);
            if (iconId == 0) {
                UserManager userManager = (UserManager) getActivity().getSystemService(
                        Context.USER_SERVICE);
                UserInfo info = Utils.getExistingUser(userManager, android.os.Process.myUserHandle());
                controller.setLabel(info.name);
                controller.setIcon(
                        com.android.settingslib.Utils.getUserIcon(getActivity(), userManager, info));
            }
            controller.done(context, true);
        }*/
        final View appSnippet = mHeaderPreference.findViewById(R.id.entity_header);
        EntityHeaderController controller = EntityHeaderController
                .newInstance(context, this, appSnippet)
                .setRecyclerView(getListView(), getLifecycle())
                .styleActionBar(context)
                .setButtonActions(EntityHeaderController.ActionType.ACTION_NONE,
                        EntityHeaderController.ActionType.ACTION_NONE);
        mAnimationView = (LottieAnimationView) mHeaderPreference.findViewById(R.id.header_icon);
        doLunaReveal();
        mAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                    final Intent intent = new Intent(Intent.ACTION_MAIN)
                            .putExtra("aoscp", true)
                            .setClassName(
                                    "android", com.android.internal.app.PlatLogoActivity.class.getName());
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Unable to start activity " + intent.toString());
                    }
                }
            }
        });
        final TextView version = (TextView) mHeaderPreference.findViewById(R.id.version);
        final TextView versionCode = (TextView) mHeaderPreference.findViewById(R.id.version_code);
        version.setText(Build.LUNA.VERSION);
        versionCode.setText(Build.LUNA.VERSION_CODE);
    }

    private void doLunaReveal() {
        mAnimationView.playAnimation();
    }

    @Override
    public void showDeviceNameWarningDialog(String deviceName) {
        DeviceNameWarningDialog.show(this);
    }

    public void onSetDeviceNameConfirm() {
        final DeviceNamePreferenceController controller = use(DeviceNamePreferenceController.class);
        controller.confirmDeviceName();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mMultiUserVersion == UserManager.MULTI_USER_V2) {
            final Bundle bundle = getArguments();
            final int iconId = bundle.getInt("icon_id", 0);
            UserManager userManager = (UserManager) getActivity().getSystemService(
                    Context.USER_SERVICE);
            UserInfo info = Utils.getExistingUser(userManager, android.os.Process.myUserHandle());

            if (iconId == 0) {
                SubMenu multiUser = menu.addSubMenu(1, MENU_MULTI_USER, 1, info.name);
                MenuItem multiUserIcon = multiUser.getItem();
                multiUserIcon.setIcon(com.android.settingslib.Utils.getSmallUserIcon(
                        getActivity(), userManager, info)).setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_MULTI_USER:
                if (mMultiUserVersion == UserManager.MULTI_USER_V2) {
                    new SubSettingLauncher(getContext())
                            .setDestination(UserSettings.class.getName())
                            .setSourceMetricsCategory(getMetricsCategory())
                            .setTitle(R.string.user_settings_title)
                            .launch();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class SummaryProvider implements SummaryLoader.SummaryProvider {

        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(SummaryLoader summaryLoader) {
            mSummaryLoader = summaryLoader;
        }

        @Override
        public void setListening(boolean listening) {
            final DeviceNamePreferenceController controller = use(DeviceNamePreferenceController.class);
            if (listening) {
                mSummaryLoader.setSummary(this, controller.getSummary());
            }
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = (activity, summaryLoader) -> new SummaryProvider(summaryLoader);

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.my_device_info;
                    return Arrays.asList(sir);
                }

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null /*activity */,
                            null /* fragment */, null /* lifecycle */);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    // The legal container is duplicated, so we ignore it here.
                    keys.add(KEY_LEGAL_CONTAINER);
                    return keys;
                }
            };
}
