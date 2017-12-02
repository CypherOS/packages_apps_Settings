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
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.settings.R;
import com.android.settings.Utils;

import aoscp.support.lottie.LottieAnimationView;

public class IllustrationPreference extends Preference {

    private final View.OnClickListener mClickListener = v -> performClick(v);
    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;
	
	private LottieAnimationView mAnimation;

    @VisibleForTesting
    View mRootView;

    public IllustrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference);
        mAllowDividerAbove = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerAbove,
                R.styleable.Preference_allowDividerAbove, false);
        mAllowDividerBelow = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerBelow,
                R.styleable.Preference_allowDividerBelow, false);
        a.recycle();

        a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.Preference, 0, 0);
		int animation = a.getResourceId(R.styleable.IllustrationPreference_animation, 0);
        if (animation == 0) {
            throw new IllegalArgumentException("IllustrationPreference requires a lottie support animation to be defined");
        }
        a.recycle();

        final View view = LayoutInflater.from(getContext())
                .inflate(R.layout.preference_illustration, null, false);
        setView(view);
		setAnimation(view, animation);
    }

    public IllustrationPreference(Context context, int resource) {
        this(context, LayoutInflater.from(context).inflate(resource, null, false));
    }

    public IllustrationPreference(Context context, View view) {
        super(context);
        setView(view);
    }

    private void setView(View view) {
        setLayoutResource(R.layout.layout_preference_frame);
        final ViewGroup allDetails = view.findViewById(R.id.all_details);
        if (allDetails != null) {
            Utils.forceCustomPadding(allDetails, true /* additive padding */);
        }
        mRootView = view;
        setShouldDisableView(false);
    }
	
	private void setAnimation(View view, int animation) {
		mAnimation = (LottieAnimationView) view.findViewById(R.id.illustration);
		mAnimation.setAnimation(animation);
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
        holder.itemView.setOnClickListener(mClickListener);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
        holder.setDividerAllowedAbove(mAllowDividerAbove);
        holder.setDividerAllowedBelow(mAllowDividerBelow);

        FrameLayout layout = (FrameLayout) holder.itemView;
        layout.removeAllViews();
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        layout.addView(mRootView);
		doAnimation();
    }

    public <T extends View> T findViewById(int id) {
        return mRootView.findViewById(id);
    }

}
