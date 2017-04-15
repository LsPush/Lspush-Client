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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.alibaba.wireless.security.jaq.JAQException;
import com.decay.logger.Log;
import com.decay.utillty.CharsetSupport;
import com.decay.utillty.HashUtils;
import com.facebook.android.crypto.keychain.FixedSecureRandom;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;
import com.tomeokin.lspush.app.logger.AppLogger;

import java.util.Arrays;

public class ConcealKeyChain implements KeyChain {
    private static final String LSPUSH_BEE = "lspush.bee";
    private static final String CIPHER_KEY = "CIPHER_KEY";
    private static final String MAC_KEY = "MAC_KEY";

    private Log log = AppLogger.cryptoLog(null);
    private final CryptoConfig mCryptoConfig;
    private final SharedPreferences mSharedPreferences;
    private final FixedSecureRandom mSecureRandom;
    private byte[] mCipherKey = null;
    private byte[] mMacKey;

    public ConcealKeyChain(Context context) {
        mSharedPreferences = context.getSharedPreferences(LSPUSH_BEE, Context.MODE_PRIVATE);
        mSecureRandom = new FixedSecureRandom();
        mCryptoConfig = CryptoConfig.KEY_256;
    }

    private byte[] maybeGenerateKey(String key, int length) throws KeyChainException, JAQException {
        byte[] data;
        String salt = mSharedPreferences.getString(HashUtils.sha256(key), "");
        if (!TextUtils.isEmpty(salt)) {
            try {
                data = JaqCrypto.get().decrypt(salt.getBytes(CharsetSupport.UTF_8));
                data = Base64.decode(data, Base64.NO_WRAP);
            } catch (Exception e) {
                log.log(e, "get key from preferences failure");
                data = generateKeyAndSave(key, length);
            }
        } else {
            data = generateKeyAndSave(key, length);
        }

        return data;
    }

    @SuppressLint("ApplySharedPref")
    private byte[] generateKeyAndSave(String key, int length) throws JAQException {
        byte[] random = new byte[length];
        mSecureRandom.nextBytes(random);
        String data = JaqCrypto.get().encrypt(Base64.encodeToString(random, Base64.NO_WRAP));

        mSharedPreferences.edit().putString(HashUtils.sha256(key), data).commit();
        return random;
    }

    @Override
    public byte[] getCipherKey() throws KeyChainException {
        if (mCipherKey == null) {
            try {
                mCipherKey = maybeGenerateKey(CIPHER_KEY, mCryptoConfig.keyLength);
            } catch (JAQException e) {
                log.log(e, "generate cipher key failure");
                throw new KeyChainException(e.getMessage(), e);
            }
        }
        return mCipherKey;
    }

    @Override
    public byte[] getMacKey() throws KeyChainException {
        if (mMacKey == null) {
            try {
                mMacKey = maybeGenerateKey(MAC_KEY, 64);
            } catch (JAQException e) {
                throw new KeyChainException(e.getMessage(), e);
            }
        }
        return mMacKey;
    }

    @Override
    public byte[] getNewIV() throws KeyChainException {
        byte[] iv = new byte[mCryptoConfig.ivLength];
        mSecureRandom.nextBytes(iv);
        return iv;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void destroyKeys() {
        if (mCipherKey != null) {
            Arrays.fill(mCipherKey, (byte) 0);
        }
        if (mMacKey != null) {
            Arrays.fill(mMacKey, (byte) 0);
        }
        mCipherKey = null;
        mMacKey = null;
        mSharedPreferences.edit().remove(HashUtils.sha256(CIPHER_KEY)).remove(MAC_KEY).commit();
    }
}
