/*
 * Copyright 2016 LsPush
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
package com.tomeokin.lspush.app.logger;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.decay.logger.AbstractLog;
import com.decay.utillty.AppInfoUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.tomeokin.lspush.BuildConfig;
import com.tomeokin.lspush.app.MissingConfigurationException;

public final class BuglyReportTree extends AbstractLog {

    public BuglyReportTree(final Context context) {
        if (TextUtils.isEmpty(BuildConfig.BUGLY_APPID)) {
            throw new MissingConfigurationException("Missing Bugly Config");
        }

        String packageName = AppInfoUtils.getProcessName();
        String processName = AppInfoUtils.getProcessName(context);
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context.getApplicationContext());
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        strategy.setAppVersion(BuildConfig.BUGLY_APP_VERSION);
        CrashReport.setIsDevelopmentDevice(context, BuildConfig.DEBUG);
        CrashReport.initCrashReport(context, BuildConfig.BUGLY_APPID, BuildConfig.BUGLY_ENABLE_DEBUG, strategy);

        // https://bugly.qq.com/docs/user-guide/advance-features-android/?v=20161229104838
        // CrashReport.setUserId("9527");
    }

    @Override
    protected boolean isLoggable(boolean debug, int priority, String tag) {
        return !debug && priority >= Log.WARN;
    }

    @Override
    protected void log(int priority, String finalTag, String tag, @Nullable String message, @Nullable Throwable cause) {
        // need to create tag first in bugly web console
        // CrashReport.setUserSceneTag(mContext, yourUserSceneTagId);

        // bugly 会将这个 throwable 上报
        CrashReport.postCatchedException(new CrashReportException(priority, finalTag, message, cause));
    }
}
