/*
 * Copyright 2016 TomeOkin
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
package com.tomeokin.lspush.widget;

import android.content.Context;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebView;

public class WebViewLoader {

    public static void init(Context context) {
        /**
         * 第一个参数传入 context，第二个参数传入 callback，不需要 callback 的可以传入 null，
         * initX5Environment 内部会创建一个线程向后台查询当前可用内核版本号，
         * 这个函数内是异步执行所以不会阻塞 AppWebViewLoader 主线程，这个函数内是轻量级执行所以对 AppWebViewLoader 启动性能没有影响，
         * 当 AppWebViewLoader 后续创建 webview 时就可以首次加载 x5 内核了
         *
         * http://x5.tencent.com/doc?id=1003
         */
        QbSdk.initX5Environment(context.getApplicationContext(), null);
    }

    public static String getWebViewInfo(Context context) {
        // @formatter:off
        // noinspection StringBufferReplaceableByString
        return new StringBuilder()
            .append("TbsCoreVersion = ").append(WebView.getTbsCoreVersion(context)) // 内核版本信息
            .append(", TbsSDKVersion = ").append(WebView.getTbsSDKVersion(context)) // 浏览器 SDK 版本信息
            .append(", CrashExtraMessage = ").append(WebView.getCrashExtraMessage(context)) // crash 线索信息
            .toString();
        // @formatter:on
    }
}
