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
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;

import com.android.settings.R;

import aoscp.support.lottie.LottieAnimationView;

public class IllustrationPreference extends Preference {
	
	private static final String TAG = "IllustrationPreference";

	private Context mContext;
	private LottieAnimationView mAnimation;
	
	private int mIllustration;
	private int mIllustrationDark;
	private boolean mIllustrationAvailable;

    public IllustrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
		mContext = context;
		TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.IllustrationPreference, 0, 0);
		try {
		    mIllustration = a.getResourceId(R.styleable.IllustrationPreference_illustration, 0);
			mIllustrationDark = a.getResourceId(R.styleable.IllustrationPreference_illustrationDark, 0);
			if (mIllustration != 0) {
				setVisible(true);
				setLayoutResource(R.layout.preference_illustration);
				mIllustrationAvailable = true;
			} else {
                setVisible(false);
            }
		} catch (Exception e) {
            Log.w(TAG, "IllustrationPreference requires a lottie support animation to be defined.");
        } finally {
            a.recycle();
        }
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
			Log.w(TAG, "Illustration not found");
            return;
        }

		boolean darkTheme = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                    Settings.Secure.DEVICE_THEME, 2, UserHandle.USER_CURRENT) != 0;
		boolean blackTheme = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                    Settings.Secure.DEVICE_THEME, 3, UserHandle.USER_CURRENT) != 0;
		mAnimation = (LottieAnimationView) holder.findViewById(R.id.illustration);
		if (darkTheme || blackTheme) {
			if (mIllustrationDark != 0) {
				mAnimation.setAnimation(mIllustrationDark);
			}
		} else {
			mAnimation.setAnimation(mIllustration);
		}
		doAnimation();
    }
}
