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
package com.tomeokin.lspush.module.auth.com;

import android.content.Context;

import com.decay.di.qualifier.AppContext;
import com.decay.logger.Log;
import com.google.gson.Gson;
import com.tomeokin.lspush.app.NetworkUtils;
import com.tomeokin.lspush.app.crypto.CryptoToken;
import com.tomeokin.lspush.app.crypto.PropertyStorage;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.internal.CurrentUser;
import com.tomeokin.lspush.data.model.AccessBundle;
import com.tomeokin.lspush.data.model.LoginData;
import com.tomeokin.lspush.data.model.PhoneData;
import com.tomeokin.lspush.data.model.RegisterData;
import com.tomeokin.lspush.data.model.Response;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.remote.LsPushService;
import com.tomeokin.lspush.data.remote.RxRequestCallback;
import com.tomeokin.lspush.data.remote.RxUtils;
import com.tomeokin.lspush.module.auth.SMSCaptchaUtils;
import com.tomeokin.lspush.module.auth.SMSRequestCallback;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import cn.smssdk.EventHandler;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@Singleton
public class AuthPresenter {
    private final LsPushService lsPushService;
    private final Gson gson;
    private final CurrentUser currentUser;
    private final Context context;
    private Log log = AppLogger.of("AuthPresenter");
    private SMSRequestCallback smsCallback;
    private SMSCaptchaUtils smsUtils;
    private SMSCaptchaUtils.SMSHandler mHandler;
    private EventHandler mEventHandler;

    @Inject
    public AuthPresenter(LsPushService lsPushService, Gson gson, CurrentUser currentUser, @AppContext Context context) {
        this.lsPushService = lsPushService;
        this.gson = gson;
        this.currentUser = currentUser;
        this.context = context;
        this.smsUtils = new SMSCaptchaUtils();
    }

    public void registerSMSEventListener(SMSRequestCallback callback) {
        smsCallback = callback;
        mHandler = new SMSCaptchaUtils.SMSHandler(callback);
        mEventHandler = new SMSCaptchaUtils.CustomEventHandler(mHandler);
        smsUtils.registerEventHandler(mEventHandler);
    }

    public void sendSMS(String countryCode, String phone) {
        if (mHandler == null || mEventHandler == null) {
            log.w("Please register SMSEventListener first");
            return;
        }

        if (NetworkUtils.isOffline()) {
            smsCallback.onNetworkOffline();
            return;
        }

        smsUtils.sendCaptcha(countryCode, phone);
    }

    public void removeSMSEventListener() {
        smsUtils.unregisterEventHandler(mEventHandler);
        mEventHandler = null;
        mHandler.removeAllMessage();
        mHandler = null;
    }

    public void checkSMS(PhoneData phoneData, RxRequestCallback<String> callback) {
        if (NetworkUtils.isOffline()) {
            callback.onNetworkOffline();
            return;
        }

        RxUtils.createToken(gson, phoneData)
            .concatMap(new Func1<CryptoToken, Observable<Response<String>>>() {
                @Override
                public Observable<Response<String>> call(CryptoToken cryptoToken) {
                    return lsPushService.checkCaptcha(cryptoToken);
                }
            })
            .subscribeOn(Schedulers.io())
            .map(RxUtils.<String>adapterResponse())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(RxUtils.callbackSubscriber(callback));
    }

    public void register(final RegisterData registerData, File avatar, final RxRequestCallback<AccessBundle> callback) {
        if (NetworkUtils.isOffline()) {
            callback.onNetworkOffline();
            return;
        }

        Observable<Response<String>> avatarObservable;
        if (avatar != null) {
            avatarObservable = upload(avatar);
        } else {
            avatarObservable = Observable.just(Response.create(registerData.getAvatar()));
        }

        // @formatter:off
        avatarObservable
            .subscribeOn(Schedulers.io())
            .map(new Func1<Response<String>, String>() {
                @Override
                public String call(Response<String> stringResponse) {
                    return stringResponse.getData();
                }
            })
            .concatMap(new Func1<String, Observable<CryptoToken>>() {
                @Override
                public Observable<CryptoToken> call(String s) {
                    registerData.setAvatar(s);
                    return RxUtils.createToken(gson, registerData);
                }
            })
            .concatMap(new Func1<CryptoToken, Observable<Response<AccessBundle>>>() {
                @Override
                public Observable<Response<AccessBundle>> call(CryptoToken cryptoToken) {
                    return lsPushService.register(cryptoToken);
                }
            })
            .map(RxUtils.<AccessBundle>adapterResponse())
            .doOnNext(saveUserToken(registerData.getPassword()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(RxUtils.callbackSubscriber(callback));
        // @formatter:on
    }

    public void login(LoginData loginData, RxRequestCallback<AccessBundle> callback) {
        if (NetworkUtils.isOffline()) {
            callback.onNetworkOffline();
            return;
        }

        // @formatter:off
        login(loginData)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(RxUtils.callbackSubscriber(callback));
        // @formatter:on
    }

    public Observable<Response<AccessBundle>> getLogin(LoginData loginData) {
        return login(loginData);
    }

    private Observable<Response<AccessBundle>> login(LoginData loginData) {
        return RxUtils.createToken(gson, loginData)
            .concatMap(new Func1<CryptoToken, Observable<Response<AccessBundle>>>() {
                @Override
                public Observable<Response<AccessBundle>> call(CryptoToken cryptoToken) {
                    return lsPushService.login(cryptoToken);
                }
            })
            .subscribeOn(Schedulers.io())
            .map(RxUtils.<AccessBundle>adapterResponse())
            .doOnNext(saveUserToken(loginData.getPassword()))
            .asObservable();
    }

    private Action1<Response<AccessBundle>> saveUserToken(final String password) {
        return new Action1<Response<AccessBundle>>() {
            @Override
            public void call(Response<AccessBundle> accessBundleResponse) {
                // save access bundle
                AccessBundle accessBundle = accessBundleResponse.getData();

                User user = accessBundle.getUser();
                user.setPassword(password);
                accessBundle.setUser(user);
                currentUser.setInstance(accessBundle);
                PropertyStorage propertyStorage = new PropertyStorage(context);
                String data = gson.toJson(accessBundle);
                propertyStorage.putString(CurrentUser.PROPERTY_NAME, data);
            }
        };
    }

    public Observable<Response<String>> upload(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        return lsPushService.upload(body);
    }
}
