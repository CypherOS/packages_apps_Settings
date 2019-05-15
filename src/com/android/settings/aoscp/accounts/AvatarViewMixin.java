/*
 * Copyright (C) 2019 CypherOS
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

package com.android.settings.aoscp.accounts;

import android.accounts.Account;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.utils.ThreadUtils;

import java.net.URISyntaxException;
import java.util.List;

public class AvatarViewMixin implements LifecycleObserver, OnClickListener, Observer {
	
	private static final String TAG = "AvatarViewMixin";

    static final Intent INTENT_GET_ACCOUNT_DATA = new Intent("android.content.action.SETTINGS_ACCOUNT_DATA");
    private String mAccountName;
    private final ActivityManager mActivityManager;
    private MutableLiveData<Bitmap> mAvatarImage;
    private ImageView mAvatarView;
    private Context mContext;

    public AvatarViewMixin(SettingsActivity activity, ImageView imageView) {
        mContext = activity.getApplicationContext();
		mActivityManager = ((ActivityManager) mContext.getSystemService(ActivityManager.class));
        mAvatarView = imageView;
        mAvatarView.setOnClickListener(this);
        mAvatarImage = new MutableLiveData();
        mAvatarImage.observe(this);
    }

	@Override
	public void onClick(View view) {
		try {
            Intent parseUri = Intent.parseUri(mContext.getResources().getString(R.string.config_account_intent_uri), 1);
            if (!TextUtils.isEmpty(mAccountName)) {
                parseUri.putExtra("extra.accountName", mAccountName);
            }
            if (mContext.getPackageManager().queryIntentActivities(parseUri, PackageManager.MATCH_SYSTEM_ONLY).isEmpty()) {
                Log.w(TAG, "Cannot find any matching action VIEW_ACCOUNT intent.");
                return;
            }
            mContext.startActivity(parseUri);
        } catch (URISyntaxException e) {
            Log.w(TAG, "Error parsing avatar mixin intent, skipping", e);
        }
    }

	@Override
	public void onChanged(Object obj) {
        mAvatarView.setImageBitmap((Bitmap) obj);
    }

    @OnLifecycleEvent(Event.ON_START)
    public void onStart() {
        if (!mContext.getResources().getBoolean(R.bool.config_showAccountAvatar)) {
            Log.d(TAG, "Feature disabled by config. Skipping");
        } else if (mActivityManager.isLowRamDevice()) {
            Log.d(TAG, "Feature disabled on low ram device. Skipping");
        } else {
            if (hasAccount()) {
                loadAccount();
            } else {
                mAvatarView.setImageResource(R.drawable.ic_account_circle_24dp);
            }
        }
    }

    boolean hasAccount() {
        Account[] accounts = FeatureFactory.getFactory(mContext).getAccountFeatureProvider().getAccounts(mContext);
        return accounts != null && accounts.length > 0;
    }

    private void loadAccount() {
        String provider = queryProviderAuthority();
        if (!TextUtils.isEmpty(provider)) {
            ThreadUtils.postOnBackgroundThread(new Runnable() {
				@Override
				public final void run() {
					Bundle caller = mContext.getContentResolver().caller(new Builder().scheme("content").authority(provider).build(), "getAccountAvatar", null, null);
					Bitmap bitmap = (Bitmap) caller.getParcelable("account_avatar");
					mAccountName = caller.getString("account_name", "");
					mAvatarImage.postValue(bitmap);
				}
			});
        }
    }

    String queryProviderAuthority() {
        List providers = mContext.getPackageManager().queryIntentContentProviders(INTENT_GET_ACCOUNT_DATA, PackageManager.MATCH_SYSTEM_ONLY);
        if (providers.size() == 1) {
            return ((ResolveInfo) providers.get(0)).providerInfo.authority;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("The size of the provider is ");
        sb.append(providers.size());
        Log.w(TAG, sb.toString());
        return null;
    }
}