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
package com.android.settings.aoscp;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;

import com.android.settings.aoscp.footerlib.FooterConfirm;

import java.lang.ref.WeakReference;

public class FooterConfirmMixin {

    private static final String TAG = "FooterConfirmMixin";
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private static WeakReference<FooterConfirm> sReference;

    private FooterConfirmMixin() {
    }

    public static void show(@NonNull FooterConfirm footerConfirm) {
        try {
            show(footerConfirm, (Activity) footerConfirm.getContext());
        } catch (ClassCastException e) {
            Log.e(TAG, "Couldn't get Activity from the this context", e);
        }
    }

    /**
     * Displays a FooterConfirmation in the current {@link Activity}, dismissing
     * the current one that is being displayed, if one is in view.
     */
    public static void show(@NonNull final FooterConfirm footerConfirm, @NonNull final Activity activity) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                FooterConfirm current = getCurrent();
                if (current != null) {
                    if (current.isShowing() && !current.isDimissing()) {
                        current.setDismissAnimation(false);
                        current.dismissByReplace();
                        sReference = new WeakReference<>(footerConfirm);
                        footerConfirm.setShowAnimation(false);
                        footerConfirm.showByReplace(activity);
                        return;
                    }
                    current.dismiss();
                }
                sReference = new WeakReference<>(footerConfirm);
                footerConfirm.show(activity);
            }
        });
    }

    /**
     * Dismisses the FooterConfirmation
     */
    public static void dismiss() {
        final FooterConfirm current = getCurrent();
        if (current != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    current.dismiss();
                }
            });
        }
    }

    /**
     * Return the current FooterConfirmation
     */
    public static FooterConfirm getCurrent() {
        if (sReference != null) {
            return sReference.get();
        }
        return null;
    }
}
