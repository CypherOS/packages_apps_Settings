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

package com.android.settings.aoscp.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;

import com.android.settings.R;

import aoscp.support.lottie.LottieAnimationView;

public class IllustrationPreference extends Preference {
	
	private static final String TAG = "IllustrationPreference";

	private int mIllustration;
	private LottieAnimationView mAnimation;
	
	private boolean mIllustrationAvailable;

    public IllustrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.Preference, 0, 0);
		mIllustration = a.getResourceId(R.styleable.IllustrationPreference_illustration, 0);
		setAnimation(mIllustration);

		setLayoutResource(R.layout.preference_illustration);
        if (mIllustration != 0) {
			mIllustrationAvailable = true;
        }
        a.recycle();
    }

	private void setAnimation(int illustration) {
		mAnimation.setAnimation(illustration);
	}
	
	private void doAnimation() {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f).setDuration(4000);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnim) {
                mAnimation.setProgress((Float) valueAnim.getAnimatedValue());
            }
        });

        if (mAnimation.getProgress() == 0f) {
            anim.start();
			anim.setRepeatCount(ValueAnimator.INFINITE);
        } else {
            mAnimation.setProgress(0f);
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		if (!mIllustrationAvailable) {
			Log.w(TAG, "IllustrationPreference requires a lottie support animation to be defined");
            return;
        }
		
		mAnimation = (LottieAnimationView) holder.findViewById(R.id.illustration);
		doAnimation();
    }
}
