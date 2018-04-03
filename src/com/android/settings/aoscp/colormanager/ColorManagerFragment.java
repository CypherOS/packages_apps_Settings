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
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Main class that acts as a container, holding theme fragments.
 */
public final class ColorManagerFragment extends InstrumentedFragment {

    private static final int THEME_FRAGMENT = 0;
    private static final int ACCENT_FRAGMENT = 1;

    private ViewPager mViewPager;
    private TabLayout mTab;
	private AppBarLayout mAppBar;
	private static CollapsingToolbarLayout mCollapsingToolbar;
	
	private static Context mContext;
	private static Activity mActivity;
	private static View mContent;
	
	private static View mThemePreview;
	private static int mSelectedTheme;
	
	private AppCompatDelegate mDelegate;
	
	private static boolean mIsDark;

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
		
		mActivity = getActivity();
		mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.color_manager_container, parent, false);
		
		final Toolbar toolbar = (Toolbar) mContent.findViewById(R.id.toolbar);

		getDelegate().setSupportActionBar(toolbar);
        if (getDelegate().getSupportActionBar() != null) getDelegate().getSupportActionBar().setTitle("Color Manager");
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mAppBar = (AppBarLayout) mContent.findViewById(R.id.appbar);
		mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
			int scrollRange = -1;

			@Override
			public void onOffsetChanged(final AppBarLayout appBarLayout, int verticalOffset) {
				if (scrollRange == -1) {
					scrollRange = appBarLayout.getTotalScrollRange();
				}
				// If we're collapsed, make sure we update the statusbar theme
				if (scrollRange + verticalOffset == 0) {
					setLightStatusBar(true);
				} else {
					if (!mIsDark) {
						setLightStatusBar(true);
					} else {
						setLightStatusBar(false);
					}
				}
			}
		});
		
		mViewPager = (ViewPager) mContent.findViewById(R.id.pager);
        setupViewPager(mViewPager);

        mTab = (TabLayout) mContent.findViewById(R.id.sliding_tabs);
		mTab.setupWithViewPager(mViewPager);
		mTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
		
		mCollapsingToolbar = (CollapsingToolbarLayout) mContent.findViewById(R.id.toolbar_collapse);

        return mContent;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
	
	private AppCompatDelegate getDelegate() {
		if (mDelegate == null) {
			mDelegate = AppCompatDelegate.create(getActivity(), null);
		}
		return mDelegate;
	}
	
	private boolean setLightStatusBar(boolean enabled) {
		if (enabled) {
			mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			return true;
		} else {
			mContent.setSystemUiVisibility(0);
			return false;
		}
	}

	public static void updateThemePreview(int selectedTheme, boolean isDark) {
		mSelectedTheme = selectedTheme;
		mIsDark = isDark;
		
		mThemePreview = (View) mContent.findViewById(R.id.header);
		mThemePreview.setBackgroundResource(selectedTheme);
		mCollapsingToolbar.setStatusBarScrimColor(mContext.getResources().getColor(selectedTheme));
		mCollapsingToolbar.setContentScrimColor(mContext.getResources().getColor(selectedTheme));
	}

	private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFrag(new ThemesFragment(), "Themes");
        adapter.addFrag(new AccentsFragment(), "Accents");
        viewPager.setAdapter(adapter);
    }
	
    private static class ViewPagerAdapter extends FragmentPagerAdapter {
		
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitle = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }

        public void addFrag(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitle.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitle.get(position);
        }
    }
}
