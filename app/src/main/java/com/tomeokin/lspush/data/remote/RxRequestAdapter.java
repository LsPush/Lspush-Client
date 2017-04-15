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
package com.tomeokin.lspush.data.remote;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.decay.logger.Log;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.app.logger.AppLogger;

public abstract class RxRequestAdapter<T> implements RxRequestCallback<T> {
    private final Log log = AppLogger.of("RxRequestAdapter");
    private final Context context;
    private ProgressDialog dialog;

    public RxRequestAdapter(Context context) {
        this(context, true);
    }

    public RxRequestAdapter(Context context, boolean progress) {
        this.context = context;
        if (progress) {
            dialog = new ProgressDialog(context);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
    }

    @Override
    public void onNetworkOffline() {
        Toast.makeText(context, R.string.network_offline, Toast.LENGTH_SHORT).show();
        closeDialog();
    }

    @Override
    public void onTokenTimeOut() {
        Toast.makeText(context, "Token time out, need to login first", Toast.LENGTH_SHORT).show();
        //LoginActivity.startAndBackLater(context);
        closeDialog();
    }

    @Override
    public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
        if (t != null) {
            Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
        closeDialog();
    }

    @Override
    public void onRequestSuccess(T data) {
        closeDialog();
    }

    @Override
    public void onRequestComplete() {
        log.log("onRequestComplete");
        closeDialog();
    }

    public void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
