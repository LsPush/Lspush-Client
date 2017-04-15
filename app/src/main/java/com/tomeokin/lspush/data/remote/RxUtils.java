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

import android.support.annotation.NonNull;

import com.decay.logger.Log;
import com.google.gson.Gson;
import com.tomeokin.lspush.app.crypto.Crypto;
import com.tomeokin.lspush.app.crypto.CryptoException;
import com.tomeokin.lspush.app.crypto.CryptoToken;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.model.Response;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RxUtils {
    private static Log log = AppLogger.networkLog("RxUtils");

    private RxUtils() {}

    public static <T> Func1<Response<T>, Response<T>> adapterResponse() {
        return new Func1<Response<T>, Response<T>>() {
            @Override
            public Response<T> call(Response<T> response) {
                if (response.isSuccess()) {
                    return response;
                }
                if (response.isTokenTimeout()) {
                    throw Exceptions.propagate(new TokenTimeoutException());
                }
                throw Exceptions.propagate(new LsPushServiceException(response.toString()));
            }
        };
    }

    public static <T> Subscriber<Response<T>> callbackSubscriber(@NonNull final RxRequestCallback<T> callback) {
        return new Subscriber<Response<T>>() {
            @Override
            public void onCompleted() {
                callback.onRequestComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                log.logStub(3).log(throwable);

                callback.onRequestComplete();
                if (throwable instanceof TokenTimeoutException) {
                    log.log("TokenTimeoutException");
                    callback.onTokenTimeOut();
                } else if (throwable instanceof LsPushServiceException) {
                    log.log("LsPushServiceException");
                    callback.onRequestFailed(null, throwable.getMessage());
                } else if (throwable instanceof CryptoException) {
                    log.log("CryptoException");
                    callback.onRequestFailed(null, "客户端出现异常，请检查后重试");
                } else {
                    log.log("Other Request Exception");
                    callback.onRequestFailed(throwable, null);
                }
            }

            @Override
            public void onNext(Response<T> response) {
                callback.onRequestComplete();
                callback.onRequestSuccess(response.getData());
            }
        };
    }

    public static <T> Observable<CryptoToken> createToken(final Gson gson, final T obj) {
        return Observable.create(new Observable.OnSubscribe<CryptoToken>() {
            @Override
            public void call(Subscriber<? super CryptoToken> subscriber) {
                try {
                    String data = gson.toJson(obj);
                    log.log(data);
                    final CryptoToken cryptoToken = Crypto.get().encrypt(data);
                    subscriber.onNext(cryptoToken);
                } catch (Throwable cause) {
                    AppLogger.cryptoLog(null).logStub(4).log(cause, "create crypto-token failed");
                    subscriber.onError(new CryptoException(cause));
                }
            }
        }).subscribeOn(Schedulers.computation()).asObservable();
    }
}
