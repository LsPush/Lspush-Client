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
package com.tomeokin.lspush.module.app;

import android.content.Context;
import android.text.TextUtils;

import com.decay.di.qualifier.AppContext;
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
import com.tomeokin.lspush.data.model.Response;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.remote.RxRequestCallback;
import com.tomeokin.lspush.data.remote.RxUtils;
import com.tomeokin.lspush.module.auth.com.AuthPresenter;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@Singleton
public class RequestExecutor {
    private static final Log log = AppLogger.of("RequestExecutor");

    private final CurrentUser currentUser;
    private final Gson gson;
    private final Context context;
    private final Lazy<AuthPresenter> authPresenter;

    @Inject
    public RequestExecutor(CurrentUser currentUser, Gson gson, @AppContext Context context,
        Lazy<AuthPresenter> authPresenter) {
        this.currentUser = currentUser;
        this.gson = gson;
        this.context = context;
        this.authPresenter = authPresenter;
    }

    public <Data> boolean authExecute(RxRequestCallback<Data> callback, ProvideRequest<Data> provideRequest) {
        Observable<Response<Data>> request = authWrapper(callback, provideRequest);
        if (request == null) {
            return false;
        }

        request.subscribeOn(Schedulers.io())
            .map(RxUtils.<Data>adapterResponse())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(RxUtils.callbackSubscriber(callback));

        return true;
    }

    public <Data> Observable<Response<Data>> authWrapper(RxRequestCallback<Data> callback,
        final ProvideRequest<Data> provideRequest) {

        if (NetworkUtils.isOffline()) {
            callback.onNetworkOffline();
            return null;
        }

        AccessBundle accessBundle = currentUser.getAccessBundle();
        if (TextUtils.isEmpty(currentUser.getAuthToken())) {
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
        }

        int tokenState = -1;
        User user = null;
        if (accessBundle != null) {
            tokenState = accessBundle.checkTokenTimeOut(false);
            user = accessBundle.getUser();
        }

        // 未登录
        if (tokenState < 0 || user == null || user.getPhone() == null || user.getPassword() == null) {
            callback.onTokenTimeOut();
            return null;
        }

        // token 有效期还比较充足
        if (tokenState > 0) {
            return provideRequest.provide();
        }

        // token 即将过期
        LoginData loginData = new LoginData(user.getPhone(), user.getPassword());
        return authPresenter.get()
            .getLogin(loginData)
            .concatMap(new Func1<Response<AccessBundle>, Observable<Response<Data>>>() {
                @Override
                public Observable<Response<Data>> call(Response<AccessBundle> accessBundleResponse) {
                    return provideRequest.provide();
                }
            })
            .asObservable();
    }

    public interface ProvideRequest<Data> {
        Observable<Response<Data>> provide();
    }
}
