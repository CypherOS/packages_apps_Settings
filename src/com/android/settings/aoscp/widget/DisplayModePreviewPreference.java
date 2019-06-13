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

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.android.settings.R;

import java.util.ArrayList;

public class DisplayModePreviewPreference extends Preference {

    private int index = 0;
    private Context mContext;
    private ImageView page0;
    private ImageView page1;
    private ImageView page2;
    private ViewPager mViewPager;

    public class PreviewChangeListener implements OnPageChangeListener {

        private Context mContext;
		private DisplayModePreviewPreference mPreview;

		public PreviewChangeListener(Context context, DisplayModePreviewPreference preview) {
			mContext = context;
			mPreview = preview;
		}

		@Override
        public void onPageSelected(int pos) {
            switch (pos) {
                case 0:
                    mPreview.page0.setImageDrawable(mContext.getResources().getDrawable(R.drawable.display_mode_preview_page_current));
                    mPreview.page1.setImageDrawable(mContext.getResources().getDrawable(R.drawable.display_mode_preview_page));
                    break;
                case 1:
                    mPreview.page1.setImageDrawable(mContext.getResources().getDrawable(R.drawable.display_mode_preview_page_current));
                    mPreview.page0.setImageDrawable(mContext.getResources().getDrawable(R.drawable.display_mode_preview_page));
                    mPreview.page2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.display_mode_preview_page));
                    break;
                case 2:
                    mPreview.page2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.display_mode_preview_page_current));
                    mPreview.page1.setImageDrawable(mContext.getResources().getDrawable(R.drawable.display_mode_preview_page));
                    break;
            }
            mPreview.index = pos;
        }

        @Override
        public void onPageScrolled(int pos, float arg1, int arg2) { }

        @Override
        public void onPageScrollStateChanged(int pos) { }
    }

    public DisplayModePreviewPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public DisplayModePreviewPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DisplayModePreviewPreference(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        setLayoutResource(R.layout.display_mode_preview);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        mViewPager = (ViewPager) view.findViewById(R.id.mode_preview);
        mViewPager.setOnPageChangeListener(new PreviewChangeListener(mContext, this));
        page0 = (ImageView) view.findViewById(R.id.page0);
        page1 = (ImageView) view.findViewById(R.id.page1);
        page2 = (ImageView) view.findViewById(R.id.page2);
        LayoutInflater inflator = LayoutInflater.from(mContext);
        View preview0 = inflator.inflate(R.layout.display_mode_preview_page_0, null);
        View preview1 = inflator.inflate(R.layout.display_mode_preview_page_1, null);
        View preview2 = inflator.inflate(R.layout.display_mode_preview_page_2, null);
        final ArrayList<View> views = new ArrayList();
        views.add(preview0);
        views.add(preview1);
        views.add(preview2);

        mViewPager.setAdapter(new PagerAdapter() {
			@Override
            public boolean isViewFromObject(View view, Object obj) {
                return view == obj;
            }

            @Override
            public int getCount() {
                return views.size();
            }

            @Override
            public void destroyItem(View container, int position, Object object) {
                ((ViewPager) container).removeView((View) views.get(position));
            }

            @Override
            public Object instantiateItem(View container, int position) {
                ((ViewPager) container).addView((View) views.get(position));
                return views.get(position);
            }
        });
        mViewPager.setCurrentItem(index);
    }
}
