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
package com.tomeokin.lspush.app.crypto;

import android.content.Context;

import com.alibaba.wireless.security.jaq.JAQException;
import com.alibaba.wireless.security.jaq.SecurityStorage;
import com.decay.logger.Log;
import com.tomeokin.lspush.app.logger.AppLogger;

public class PropertyStorage {
    private Log log = AppLogger.cryptoLog(null);
    private SecurityStorage storage;

    public PropertyStorage(Context context) {
        storage = new SecurityStorage(context.getApplicationContext());
    }

    public void putString(String key, String content) {
        try {
            storage.putString(key, content);
        } catch (JAQException e) {
            log.log(e, "ErrorCode = %s", e.getErrorCode());
        }
    }

    public String getString(String key) {
        try {
            return storage.getString(key);
        } catch (JAQException e) {
            log.log(e, "ErrorCode = %s", e.getErrorCode());
        }
        return null;
    }

    public void remove(String key) {
        try {
            storage.removeString(key);
        } catch (JAQException e) {
            log.log(e, "ErrorCode = %s", e.getErrorCode());
        }
    }
}
