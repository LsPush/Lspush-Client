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
package com.tomeokin.lspush.data.remote;

import java.text.MessageFormat;

public class CloudSpeedLoader {
    public static final String CLOUD_SPEED_FORMAT = LsPushService.CLOUD_SPEED_URL + "?fileUrl={0}&frame={1}";

    public static String wrap(String url, boolean frame) {
        return MessageFormat.format(CLOUD_SPEED_FORMAT, url, frame);
        //return String.format(Locale.getDefault(), url, frame);
    }
}
