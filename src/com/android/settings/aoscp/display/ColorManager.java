/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.aoscp.display;

import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;

import java.util.List;

public class ColorManager {

    private final IOverlayManager mService;
	private final PackageManager mPackageManager;
	
	public ColorManager() {
        mService = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
    }
	
	private boolean isAccentOverlay(String packageName) {
        try {
            PackageInfo pi = mPackageManager.getPackageInfo(packageName, 0);
            return pi != null && !pi.isAccentOverlay; // AOSCP: Only load themes that do not inclue "isAccent=true" in their manifest
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
	
	public void prepareAccent() {
        try {
            List<OverlayInfo> infos = getOverlayInfosForTarget("android",
                    UserHandle.myUserId());
            for (int i = 0, size = infos.size(); i < size; i++) {
                if (infos.get(i).isEnabled() &&
                        isAccentOverlay(infos.get(i).packageName)) {
                    setEnabled(infos.get(i).packageName, false, UserHandle.myUserId());
                }
            }
        } catch (RemoteException e) {
        }
    }
	
	public void setEnabledExclusive(String pkg, boolean enabled, int userId)
            throws RemoteException {
        mService.setEnabledExclusive(pkg, enabled, userId);
    }
	
	public void setEnabled(String pkg, boolean enabled, int userId)
            throws RemoteException {
        mService.setEnabled(pkg, enabled, userId);
    }
	
	public List<OverlayInfo> getOverlayInfosForTarget(String target, int userId)
            throws RemoteException {
        return mService.getOverlayInfosForTarget(target, userId);
    }
}
