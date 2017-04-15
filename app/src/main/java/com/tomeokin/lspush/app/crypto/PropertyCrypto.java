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
import android.util.Base64;

import com.decay.logger.Log;
import com.decay.utillty.CharsetSupport;
import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.crypto.Entity;
import com.tomeokin.lspush.app.logger.AppLogger;

public class PropertyCrypto {
    private Log log = AppLogger.cryptoLog("PropertyCrypto");

    private static PropertyCrypto propertyCrypto;
    private com.facebook.crypto.Crypto mFbCrypto;

    public static PropertyCrypto init(Context context) {
        if (propertyCrypto == null) {
            propertyCrypto = new PropertyCrypto(context.getApplicationContext());
        }
        return propertyCrypto;
    }

    public static PropertyCrypto get() {
        return propertyCrypto;
    }

    private PropertyCrypto(Context context) {
        try {
            ConcealKeyChain keyChain = new ConcealKeyChain(context);
            mFbCrypto = AndroidConceal.get().createCrypto256Bits(keyChain);
        } catch (Throwable throwable) {
            log.log(throwable, "Init property crypto failed");
        }
    }

    public String decrypt(String key, String value) throws CryptoException {
        try {
            Entity entity = Entity.create(key);
            byte[] data = mFbCrypto.decrypt(Base64.decode(value, Base64.NO_WRAP), entity);
            return new String(data, CharsetSupport.UTF_8);
        } catch (Throwable throwable) {
            log.log(throwable, "Decrypt key = %s, value = %s failed", key, value);
            throw new CryptoException(throwable);
        }
    }

    public String encrypt(String key, String value) throws CryptoException {
        try {
            Entity entity = Entity.create(key); // original key
            byte[] data = mFbCrypto.encrypt(value.getBytes(CharsetSupport.UTF_8), entity);
            return Base64.encodeToString(data, Base64.NO_WRAP);
        } catch (Throwable throwable) {
            log.log(throwable, "Encrypt key = %s, value = %s failed", key, value);
            throw new CryptoException(throwable);
        }
    }
}
