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

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.decay.logger.BasicLog;
import com.decay.logger.Logger;
import com.tomeokin.lspush.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    private static final BasicLog log = Logger.tag("FileUtils").debug(BuildConfig.DEBUG);

    private FileUtils() {}

    public static boolean isRealFile(@NonNull File file) {
        return file.exists() && file.isFile();
    }

    /**
     * Get the temp-copy one of the origin file.
     *
     * In general, you should only use it when the uri content {@link Uri#toString()} is not the real file path,
     * such as 'content://media/external/images/media/73'.
     *
     * Recommend to use {@link FileUtils#resolveImagePath(Context, Uri)} or the similar one
     * if you want to known the origin file name or avoid to create a temp-copy one.
     */
    public static void resolveFile(Context context, @NonNull Uri from, @NonNull File output) {
        try {
            InputStream in = context.getContentResolver().openInputStream(from);
            if (in == null) return;

            ensureFileExisted(output);
            OutputStream out = new FileOutputStream(output);
            Toolkit.transfer(in, out);
        } catch (Exception e) {
            /* no-op */
        }
    }

    /**
     * Get the real file path of Uri
     *
     * In Android, the uri may be 'content://media/external/images/media/73', which is not the real file path.
     * We get the real one, such as '/storage/emulated/0/Download/161__snow1.png' base on {@link
     * android.content.ContentResolver#query(Uri, String[], String, String[], String)}
     */
    @Nullable
    public static String resolveImagePath(Context context, @NonNull Uri from) {
        return resolveMediaPath(context, from, new String[] { MediaStore.Images.Media.DATA });
    }

    @Nullable
    public static String resolveVideoPath(Context context, @NonNull Uri from) {
        return resolveMediaPath(context, from, new String[] { MediaStore.Video.Media.DATA });
    }

    @Nullable
    public static String resolveAudioPath(Context context, @NonNull Uri from) {
        return resolveMediaPath(context, from, new String[] { MediaStore.Audio.Media.DATA });
    }

    @Nullable
    public static String resolveMediaPath(Context context, @NonNull Uri from, String[] projection) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(from, projection, null, null, null);
        try {
            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                path = cursor.getString(0);
            }
        } finally {
            Toolkit.tryToClose(cursor);
        }

        return path;
    }

    public static void ensureFileExisted(@NonNull File file) throws Exception {
        boolean done = true;
        if (file.exists()) {
            if (!file.isFile()) {
                done = file.delete() && file.createNewFile();
            }
        } else {
            done = file.createNewFile();
        }
        if (!done) {
            throw new Exception("create new file failed");
        }
    }

    public static void ensurePathExisted(@NonNull File dir) throws Exception {
        boolean done = true;
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                done = dir.delete() && dir.mkdirs();
            }
        } else {
            done = dir.mkdirs();
        }
        if (!done) {
            throw new Exception("create new dir failed");
        }
    }

    public static boolean isDocumentUri(final Context context, final Uri uri) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getFilePathFromDocumentsContract(final Context context, final Uri uri) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else {
                return null;
            }
        }
        // DownloadsProvider
        else if (isDownloadsDocument(uri)) {
            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                Long.valueOf(id));
            return getDataColumn(context, contentUri, null, null);
        }
        // MediaProvider
        // content://com.android.providers.media.documents/document/image%3A57
        // content://com.android.providers.media.documents/document/image:57
        else if (isMediaDocument(uri)) {
            // image:57
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[] {
                split[1]
            };

            return getDataColumn(context, contentUri, selection, selectionArgs);
        }

        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri The Uri to query.
     */
    public static String getFilePath(final Context context, final Uri uri) {
        log.log("Authority: %s, Fragment: %s, Port: %d, Query: %s, Scheme: %s, Host: %s, Segments: %s",
            uri.getAuthority(), uri.getFragment(), uri.getPort(), uri.getQuery(), uri.getScheme(), uri.getScheme(),
            uri.getPathSegments());

        // DocumentProvider
        if (isDocumentUri(context, uri)) {
            return getFilePathFromDocumentsContract(context, uri);
        }
        // MediaStore (and general)
        // content://media/external/images/media/13
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        // file:///d/gpio
        // file:///storage/emulated/0/Download/166__sun1.png
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath(); // /storage/emulated/0/Download/166__sun1.png
        }

        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        final String column = "_data";
        final String[] projection = { column };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                if (log.isDebug()) {
                    DatabaseUtils.dumpCursor(cursor);
                }

                final int columnIndex = cursor.getColumnIndex(column);
                return columnIndex < 0 ? null : cursor.getString(columnIndex);
            }
        } finally {
            Toolkit.tryToClose(cursor);
        }
        return null;
    }
}
