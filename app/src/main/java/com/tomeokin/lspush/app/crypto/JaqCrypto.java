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
import android.text.TextUtils;

import com.alibaba.wireless.security.jaq.JAQException;
import com.alibaba.wireless.security.jaq.SecurityCipher;
import com.alibaba.wireless.security.jaq.SecurityInit;
import com.decay.logger.Log;
import com.tomeokin.lspush.BuildConfig;
import com.tomeokin.lspush.app.logger.AppLogger;

// https://jaq-doc.alibaba.com/docs/doc.htm?spm=0.0.0.0.X0pN2D&treeId=243&articleId=105581&docType=1#s5
public class JaqCrypto {
    private static Log log = AppLogger.cryptoLog("JaqCrypto");

    private static final String JAQ_KEY = BuildConfig.JAQ_KEY;
    private static JaqCrypto jaqCrypto;
    private final SecurityCipher cipher;

    public static JaqCrypto init(Context context) {
        if (jaqCrypto == null) {
            jaqCrypto = new JaqCrypto(context.getApplicationContext());
        }
        return jaqCrypto;
    }

    public static JaqCrypto get() {
        return jaqCrypto;
    }

    private JaqCrypto(Context context) {
        try {
            SecurityInit.Initialize(context);
        } catch (JAQException e) {
            log.log("Init jaq failed, errorCode = %d", e.getErrorCode());
        }
        cipher = new SecurityCipher(context);
    }

    public byte[] encrypt(byte[] data) throws JAQException {
        return data == null || data.length == 0 ? data : cipher.encryptBinary(data, JAQ_KEY);
    }

    public String encrypt(String data) throws JAQException {
        return TextUtils.isEmpty(data) ? data : cipher.encryptString(data, JAQ_KEY);
    }

    public byte[] decrypt(byte[] data) throws JAQException {
        return data == null || data.length == 0 ? data : cipher.decryptBinary(data, JAQ_KEY);
    }

    public String decrypt(String data) throws JAQException {
        return TextUtils.isEmpty(data) ? data : cipher.decryptString(data, JAQ_KEY);
    }
}
