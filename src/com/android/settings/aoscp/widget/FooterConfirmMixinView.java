/*
 * Copyright 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.aoscp.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.settings.R;

public class FooterConfirmMixinView extends RelativeLayout {

    private static final String TAG = "FooterConfirmMixinView";

    private static final int ANIMATION_DURATION = 250;
	
	private int mTimeoutDuration;

    private final Runnable mHideRunnable = new Runnable() {

        @Override
        public void run() {
            hide();
        }

    };

    private final Handler mMainHandler;
	private ImageView mIcon;
    private TextView mContent;

    public FooterConfirmMixinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setTranslationY(getHeight());
        setVisibility(View.GONE);

		mIcon = (ImageView) findViewById(R.id.icon);
        mContent = (TextView) findViewById(R.id.content);
    }

    public void show(String content, int duration) {
		mTimeoutDuration = duration;

        mContent.setText(content);
        animate().translationY(getHeight())
                .setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                        android.R.interpolator.fast_out_slow_in))
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(final Animator animation) {
                        setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        animate().translationY(0f)
                                .setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                                        android.R.interpolator.fast_out_slow_in))
                                .setDuration(ANIMATION_DURATION)
                                .start();
                    }
                }).start();

        mMainHandler.removeCallbacks(mHideRunnable);
        mMainHandler.postDelayed(mHideRunnable, mTimeoutDuration);
    }

    public void hide() {

        animate().translationY(getHeight())
                .setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                        android.R.interpolator.fast_out_slow_in))
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        setVisibility(View.GONE);
                    }
                }).start();
    }
}