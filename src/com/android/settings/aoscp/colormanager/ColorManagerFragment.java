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

package com.android.settings.aoscp.colormanager;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.widget.RtlCompatibleViewPager;
import com.android.settings.widget.SlidingTabLayout;

/**
 * Main class that acts as a container, holding theme fragments.
 */
public final class ColorManagerFragment extends InstrumentedFragment {

    private static final int THEME_FRAGMENT = 0;
    private static final int ACCENT_FRAGMENT = 1;

    private RtlCompatibleViewPager mViewPager;
    private SlidingTabLayout mHeaderView;
    private ColorManagerPagerAdapter mPagerAdapter;
	private static View mContent;
	
	private static View mThemePreview;
	private static ImageView mAccentPreview;
	
	private static int mSelectedTheme;

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.color_manager_container, parent, false);
        mViewPager = (RtlCompatibleViewPager) mContent.findViewById(R.id.pager);
        mPagerAdapter = new ColorManagerPagerAdapter(getContext(),
                getChildFragmentManager(), mViewPager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabChangeListener(this));
        mViewPager.setCurrentItem(THEME_FRAGMENT);
		
		updateThemePreview(mSelectedTheme);

        mHeaderView = (SlidingTabLayout) mContent.findViewById(R.id.sliding_tabs);
        mHeaderView.setViewPager(mViewPager);

        return mContent;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
	
	private static void updateThemePreview(int selectedTheme) {
		mSelectedTheme = selectedTheme;
		
		mThemePreview = (View) mContent.findViewById(R.id.theme_preview);
		mAccentPreview = (ImageView) mContent.findViewById(R.id.accent_preview);
		
		mThemePreview.setBackgroundResource(selectedTheme);
	}

    private static final class ColorManagerPagerAdapter extends FragmentPagerAdapter {

        private final Context mContext;
        private final RtlCompatibleViewPager mViewPager;

        public ColorManagerPagerAdapter(Context context, FragmentManager fragmentManager,
                RtlCompatibleViewPager viewPager) {
            super(fragmentManager);
            mContext = context;
            mViewPager = viewPager;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case THEME_FRAGMENT:
                    return mContext.getString(R.string.color_manager_tab_themes);
                case ACCENT_FRAGMENT:
                    return mContext.getString(R.string.color_manager_tab_accents);
            }
            return super.getPageTitle(position);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case THEME_FRAGMENT:
                    return new ThemesFragment();
                case ACCENT_FRAGMENT:
                    return new AccentsFragment();
                default:
                    throw new IllegalArgumentException(
                            String.format(
                                    "Position %d does not map to a valid color manager fragment",
                                    position));
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container,
                    mViewPager.getRtlAwareIndex(position));
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private static final class TabChangeListener implements RtlCompatibleViewPager.OnPageChangeListener {

        private final ColorManagerFragment mColorManager;

        public TabChangeListener(ColorManagerFragment colorMgr) {
            mColorManager = colorMgr;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Do nothing
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Do nothing
        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case THEME_FRAGMENT:
                    //MetricsLogger.action(
                            //mActivity, MetricsProto.MetricsEvent.ACTION_SELECT_THEMES);
                    break;
                case ACCENT_FRAGMENT:
                    //MetricsLogger.action(
                            //mActivity, MetricsProto.MetricsEvent.ACTION_SELECT_ACCENTS);
                    break;
            }
        }
    }
}
