/*
 * Copyright 2017 LsPush
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
package com.decay.utillty;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;

public class AppInfoUtils {
    private AppInfoUtils() {}

    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        String processName = null;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                processName = appProcess.processName;
                break;
            }
        }
        return processName;
    }

    /**
     * 获取进程号对应的进程名
     *
     * @return 进程名
     */
    public static String getProcessName() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + android.os.Process.myPid() + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            Toolkit.tryToClose(reader);
        }
        return null;
    }

    @SuppressLint("PackageManagerGetSignatures")
    public static String getSignature(Context context) {
        String signature = null;
        try {
            PackageManager pkgManager = context.getPackageManager();
            PackageInfo pkgInfo = pkgManager.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signatures = pkgInfo.signatures;

            StringBuilder builder = new StringBuilder();
            for (Signature item : signatures) {
                builder.append(item.toCharsString());
            }
            signature = builder.toString();
        } catch (Exception e) {
            /* no-op */
        }

        return signature;
    }
}
