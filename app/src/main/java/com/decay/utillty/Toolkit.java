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
package com.decay.utillty;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

public class Toolkit {
    private Toolkit() {}

    public static void tryToClose(@Nullable Closeable... objects) {
        if (objects == null || objects.length == 0) return;

        for (Closeable it : objects) {
            try {
                it.close();
            } catch (Exception e) {
                /* no-op */
            }
        }
    }

    public static void tryToRecycle(@Nullable Recyclable... objects) {
        if (objects == null || objects.length == 0) return;

        for (Recyclable it : objects) {
            try {
                it.recycle();
            } catch (Exception e) {
                /* no-op */
            }
        }
    }

    public static int transfer(@NonNull InputStream in, @NonNull OutputStream out) {
        final int bufferSize = 4096;

        int byteCount = 0;
        try {
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
        } catch (Exception ex) {
            /* no-op */
        } finally {
            Toolkit.tryToClose(in, out);
        }
        return byteCount;
    }
}
