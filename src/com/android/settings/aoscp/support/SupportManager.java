/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.dashboard;
import android.annotation.DrawableRes;
import android.annotation.IdRes;
import android.annotation.StringRes;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.InstrumentedFragment;
import com.android.settings.R;

public final class SupportManager extends InstrumentedFragment {
    private View mContent;
    @Override
    protected int getMetricsCategory() {
        return SUPPORT_MANAGER;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.support_manager, container, false);
        // Update help item.
        updateSupportTile(R.id.help_locate_tile, R.drawable.ic_feedback_24dp,
                R.string.support_welcome_title);
        return mContent;
    }
    private void updateSupportTile(@IdRes int tileId, @DrawableRes int icon, @StringRes int title) {
        final View tile = mContent.findViewById(tileId);
        ((ImageView) tile.findViewById(android.R.id.icon)).setImageResource(icon);
        ((TextView) tile.findViewById(android.R.id.title)).setText(title);
    }
}