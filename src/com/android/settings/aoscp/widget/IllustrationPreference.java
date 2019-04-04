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
 * limitations under the License.
 */

package com.android.settings.aoscp.widget;

import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;

import aoscp.support.lottie.LottieAnimationView;

import com.android.settings.R;

public class IllustrationPreference extends Preference {

    private static final String TAG = "IllustrationPreference";

    private Context mContext;
    private LottieAnimationView mAnimationView;

    private int mIllustration;
    private boolean mIllustrationAvailable = false;

    public IllustrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.IllustrationPreference, 0, 0);
        try {
            mIllustration = a.getResourceId(R.styleable.IllustrationPreference_illustration, 0);
            setVisible(true);
            setLayoutResource(R.layout.preference_illustration);
            mIllustrationAvailable = true;
        } catch (Exception e) {
            Log.w(TAG, "IllustrationPreference requires a lottie support animation to be defined.");
        } finally {
            a.recycle();
        }
    }

    private void doAnimation() {
        mAnimationView.setRepeatCount(ValueAnimator.INFINITE);
        mAnimationView.playAnimation();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (!mIllustrationAvailable) {
            Log.w(TAG, "Illustration not found");
            return;
        }
        mAnimationView = (LottieAnimationView) holder.findViewById(R.id.illustration);
        mAnimationView.setAnimation(mIllustration);
        doAnimation();
    }
}