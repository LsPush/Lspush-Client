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
package com.decay.utillty;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.decay.logger.Log;
import com.tomeokin.lspush.app.logger.AppLogger;

import java.io.File;
import java.util.Arrays;

public class ImageIntentUtils {
    private static Log log = AppLogger.of("ImageIntentUtils");
    private ImageIntentUtils() {}

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String[] IMAGE_PREFIX = new String[] {
        ".jpg", ".jpeg", ".png", ".gif", ".bmp"
    };

    static {
        Arrays.sort(IMAGE_PREFIX);
    }

    public static final String[] PERMISSION_PICK_IMAGE;
    public static final String[] PERMISSION_TAKE_PHOTO;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            PERMISSION_PICK_IMAGE = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
            PERMISSION_TAKE_PHOTO = new String[] {
                Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
            };
        } else {
            PERMISSION_PICK_IMAGE = new String[] {};
            PERMISSION_TAKE_PHOTO = new String[] { Manifest.permission.CAMERA };
        }
    }

    /**
     * With Build.VERSION_CODES.KITKAT and above, we can see a document and we can select another app in drawer.
     * For other, they won't see the document activity.
     */
    public static Intent createSelectImageIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    public static File resolveSelectImageData(Context context, Intent data) {
        Uri uri = data.getData();
        if (!isUrlEmpty(uri)) {
            final String path = FileUtils.getFilePath(context, uri);
            log.log("image path = %s", path);

            File file = null;
            if (path != null) {
                file = new File(path);
            }
            if (file != null && FileUtils.isRealFile(file)) {
                final String filename = file.getName();
                log.log("file name = %s", filename);
                log.log("file path = %s", file.getPath());
                return file;
            }
        }
        return null;
    }

    private static boolean isUrlEmpty(@Nullable Uri uri) {
        return uri == null || uri.equals(Uri.EMPTY);
    }

    /**
     * Using with Intent.ACTION_OPEN_DOCUMENT mean you will not see other suitable app. But it limit what user selected
     * is compat with editor_image.
     */
    public static Intent createSelectImageIntentWithDocumentCompat() {
        Intent intent = new Intent().setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        return intent;
    }

    public static Intent createTakePhotoIntent(File file) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        return intent;
    }

    public static boolean isImage(@NonNull File file) {
        String extension = getExtension(file.getName());
        if (!TextUtils.isEmpty(extension)) {
            return Arrays.binarySearch(IMAGE_PREFIX, extension) >= 0;
        }

        // not support file type
        return false;
    }

    /**
     * Get the extension of a file name, like ".png" or ".jpg".
     *
     * @return Extension including the dot("."); "" if there is no extension; null if filename was null.
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }

        int dot = filename.lastIndexOf(".");
        if (dot >= 0) {
            return filename.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }
}
