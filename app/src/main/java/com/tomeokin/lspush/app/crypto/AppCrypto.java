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
package com.tomeokin.lspush.app.crypto;

import android.content.Context;

import com.decay.logger.Log;
import com.decay.utillty.CharsetSupport;
import com.decay.utillty.HashUtils;
import com.decay.utillty.Toolkit;
import com.tomeokin.lspush.app.logger.AppLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import cn.smssdk.SMSSDK;

public class AppCrypto {
    private static final String TAG_APP_CRYPTO = AppLogger.TAG_CRYPTO;
    private static Log log = AppLogger.cryptoLog(null);

    static final String LSPUSH_OK = "lspush.ok";
    static final String PUBLIC_KEY = "LSPUSH_PUBLIC_KEY";
    static final String MOB_SMS_ID = "MOB_SMS_ID";
    static final String MOB_SMS_KEY = "MOB_SMS_KEY";

    public static void init(Context context) {
        InputStream in = null;
        BufferedReader reader = null;
        try {
            Properties properties = new Properties();
            in = context.getAssets().open(LSPUSH_OK, Context.MODE_PRIVATE);
            reader = new BufferedReader(new InputStreamReader(in, CharsetSupport.UTF_8));
            properties.load(reader);

            JaqCrypto jaqCrypto = JaqCrypto.init(context);
            String publicKey = jaqCrypto.decrypt(properties.getProperty(HashUtils.sha256(PUBLIC_KEY)));
            String mobSMSId = jaqCrypto.decrypt(properties.getProperty(HashUtils.sha256(MOB_SMS_ID)));
            String mobSMSKey = jaqCrypto.decrypt(properties.getProperty(HashUtils.sha256(MOB_SMS_KEY)));

            Crypto.init(publicKey);
            SMSSDK.initSDK(context, mobSMSId, mobSMSKey);
            //PropertyCrypto.init(context);

        } catch (Throwable throwable) {
            log.tag(TAG_APP_CRYPTO).log(throwable, "Init crypto component failed");
        } finally {
            Toolkit.tryToClose(reader, in);
        }
    }
}
