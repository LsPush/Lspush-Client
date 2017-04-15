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
import android.content.ContextWrapper;

import com.decay.logger.Log;
import com.decay.utillty.CharsetSupport;
import com.decay.utillty.HashUtils;
import com.decay.utillty.Toolkit;
import com.tomeokin.lspush.BuildConfig;
import com.tomeokin.lspush.app.logger.AppLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

public class LsPushOkBuildTool {
    private Log log = AppLogger.of("LsPushOkBuildTool");
    private JaqCrypto mJaqCrypto;

    public LsPushOkBuildTool(Context context) {
        mJaqCrypto = JaqCrypto.init(context);
    }

    public void write(Context context) throws CryptoException {
        OutputStream out = null;
        BufferedWriter writer = null;
        try {
            Properties keyFile = new Properties();
            keyFile.setProperty(HashUtils.sha256(AppCrypto.PUBLIC_KEY),
                mJaqCrypto.encrypt(BuildConfig.LSPUSH_PUBLIC_KEY));
            keyFile.setProperty(HashUtils.sha256(AppCrypto.MOB_SMS_ID), mJaqCrypto.encrypt(BuildConfig.MOB_SMS_ID));
            keyFile.setProperty(HashUtils.sha256(AppCrypto.MOB_SMS_KEY), mJaqCrypto.encrypt(BuildConfig.MOB_SMS_KEY));

            File file = getFile(context, "secure", AppCrypto.LSPUSH_OK);
            out = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(out, CharsetSupport.UTF_8));
            keyFile.store(writer, "lspush.ok version 3");
            out.close();
            writer.close();
        } catch (Throwable cause) {
            throw new CryptoException(cause);
        } finally {
            Toolkit.tryToClose(writer, out);
        }
    }

    public void read(Context context) throws CryptoException {
        InputStream in = null;
        BufferedReader reader = null;
        try {
            Properties keyFile = new Properties();
            File file = getFile(context, "secure", AppCrypto.LSPUSH_OK);
            in = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in, CharsetSupport.UTF_8));
            keyFile.load(reader);

            log.log("key: %s, value: %s", AppCrypto.PUBLIC_KEY,
                JaqCrypto.get().decrypt(keyFile.getProperty(HashUtils.sha256(AppCrypto.PUBLIC_KEY))));
            log.log("key: %s, value: %s", AppCrypto.MOB_SMS_ID,
                JaqCrypto.get().decrypt(keyFile.getProperty(HashUtils.sha256(AppCrypto.MOB_SMS_ID))));
            log.log("key: %s, value: %s", AppCrypto.MOB_SMS_KEY,
                JaqCrypto.get().decrypt(keyFile.getProperty(HashUtils.sha256(AppCrypto.MOB_SMS_KEY))));
        } catch (Throwable cause) {
            throw new CryptoException(cause);
        } finally {
            Toolkit.tryToClose(reader, in);
        }
    }

    private File getFile(Context context, String path, String filename) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File dir = contextWrapper.getDir(path, Context.MODE_PRIVATE);
        return new File(dir, filename);
    }
}
