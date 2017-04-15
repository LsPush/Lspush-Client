/*
 * Copyright 2017 TomeOkin
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

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.URLUtil;

public class UrlUtils {
    public static String reformUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) return null;

        if (!url.startsWith("http")) {
            return "http://" + url;
        }

        return url;
    }

    public static boolean isNetworkUrl(@Nullable String url) {
        return !TextUtils.isEmpty(url) && URLUtil.isNetworkUrl(url);
    }
}
