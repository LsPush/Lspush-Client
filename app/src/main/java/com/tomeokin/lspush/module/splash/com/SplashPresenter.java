/*
 * Copyright 2017 TomeOkin
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
package com.tomeokin.lspush.module.splash.com;

import android.content.Context;

import com.decay.di.qualifier.ActivityContext;
import com.decay.logger.Log;
import com.decay.utillty.DateUtils;
import com.google.gson.Gson;
import com.tomeokin.lspush.app.NetworkUtils;
import com.tomeokin.lspush.app.crypto.CryptoToken;
import com.tomeokin.lspush.app.crypto.PropertyStorage;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.internal.CurrentUser;
import com.tomeokin.lspush.data.model.AccessBundle;
import com.tomeokin.lspush.data.model.LoginData;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.remote.RxRequestCallback;
import com.tomeokin.lspush.module.auth.com.AuthPresenter;
import com.tomeokin.lspush.module.splash.di.SplashScope;

import java.util.Date;

import javax.inject.Inject;

import dagger.Lazy;

@SplashScope
public class SplashPresenter {
    private Log log = AppLogger.of("SplashPresenter");
    private final CurrentUser currentUser;
    private final Context context;
    private final Gson gson;
    private final Lazy<AuthPresenter> authPresenter;

    @Inject
    public SplashPresenter(CurrentUser currentUser, @ActivityContext Context context, Gson gson,
        Lazy<AuthPresenter> authPresenter) {
        this.currentUser = currentUser;
        this.context = context;
        this.gson = gson;
        this.authPresenter = authPresenter;
    }

    public void loadCurrentUser(RxRequestCallback<AccessBundle> callback) {
        AccessBundle accessBundle = null;
        try {
            PropertyStorage storage = new PropertyStorage(context);
            String accessInfo = storage.getString(CurrentUser.PROPERTY_NAME);
            if (accessInfo != null) {
                accessBundle = gson.fromJson(accessInfo, AccessBundle.class);
                currentUser.setInstance(accessBundle);

                final User user = accessBundle.getUser();
                log.log("user: %s", user.toString());
                final CryptoToken cryptoToken = accessBundle.getExpireToken();
                log.log("expire token: %s", cryptoToken.toString());
                final Date expireTime = accessBundle.getExpireTime();
                log.log("expire time: %s", DateUtils.toFormatDateTime(expireTime));
            }
        } catch (Throwable cause) {
            log.log(cause, "loadCurrentUser failed");
        }

        int tokenState = -1;
        User user = null;
        if (accessBundle != null) {
            tokenState = accessBundle.checkTokenTimeOut(true);
            user = accessBundle.getUser();
        }

        // 未登录
        if (tokenState < 0 || user == null || user.getPhone() == null || user.getPassword() == null) {
            callback.onTokenTimeOut();
            return;
        }

        // token 有效期还比较充足
        if (tokenState > 0) {
            callback.onRequestSuccess(null);
            return;
        }

        if (NetworkUtils.isOffline()) {
            callback.onNetworkOffline();
            return;
        }

        LoginData loginData = new LoginData(user.getPhone(), user.getPassword());
        authPresenter.get().login(loginData, callback);
    }
}
