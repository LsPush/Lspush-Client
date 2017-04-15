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
import android.text.TextUtils;

import com.decay.logger.AndroidLog;
import com.decay.logger.Log;
import com.decay.logger.Logger;
import com.tomeokin.lspush.BuildConfig;

public final class AppLogger {
    public static final String BASE_LOG_TAG = "LsPush-App";
    public static final String TAG_NETWORK = "Network";
    public static final String TAG_DATABASE = "Database";
    public static final String TAG_CRYPTO = "DataCrypt";

    static {
        Logger.tag(BASE_LOG_TAG).debug(BuildConfig.DEBUG).bindLogger(new AndroidLog());
    }

    private AppLogger() {}

    public static void init(final Context context) {
        Logger.tag(BASE_LOG_TAG).bindLogger(new BuglyReportTree(context));
    }

    public static Log of(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return Logger.tag(BASE_LOG_TAG);
        } else {
            return Logger.tag(BASE_LOG_TAG).subTag(tag);
        }
    }

    public static Log databaseLog(String tag) {
        return of(TAG_DATABASE, tag);
    }

    public static Log networkLog(String tag) {
        return of(TAG_NETWORK, tag);
    }

    public static Log cryptoLog(String tag) {
        return of(TAG_CRYPTO, tag);
    }

    private static Log of(String basicTag, String tag) {
        String finaTag = TextUtils.isEmpty(tag) ? basicTag : basicTag + "-" + tag;
        return Logger.tag(BASE_LOG_TAG).subTag(finaTag);
    }
}
