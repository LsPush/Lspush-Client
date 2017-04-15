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
package com.tomeokin.lspush.module.setting.com;

import android.content.Context;

import com.decay.di.qualifier.AppContext;
import com.google.gson.Gson;
import com.tomeokin.lspush.app.crypto.PropertyStorage;
import com.tomeokin.lspush.data.internal.CurrentUser;
import com.tomeokin.lspush.data.model.AccessBundle;
import com.tomeokin.lspush.data.model.Response;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.remote.LsPushService;
import com.tomeokin.lspush.data.remote.RxRequestCallback;
import com.tomeokin.lspush.module.app.RequestExecutor;
import com.tomeokin.lspush.module.setting.di.SettingScope;

import java.io.File;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Action1;

@SettingScope
public class SettingPresenter {
    private final LsPushService lsPushService;
    private final RequestExecutor executor;
    private final Gson gson;
    private final CurrentUser currentUser;
    private final Context context;

    @Inject
    public SettingPresenter(RequestExecutor executor, LsPushService lsPushService, Gson gson, CurrentUser currentUser,
        @AppContext Context context) {
        this.executor = executor;
        this.lsPushService = lsPushService;
        this.gson = gson;
        this.currentUser = currentUser;
        this.context = context;
    }

    public void updateUserInfo(User user, File file, RxRequestCallback<User> callback) {
        String json = gson.toJson(user.profile());
        final RequestBody userBody = RequestBody.create(MediaType.parse("multipart/form-data"), json);

        MultipartBody.Part body = null;
        if (file != null) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        }

        final MultipartBody.Part finalBody = body;
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<User>() {
            @Override
            public Observable<Response<User>> provide() {
                return lsPushService.updateUserInfo(userBody, finalBody).doOnNext(saveUserInfo()).asObservable();
            }
        });
    }

    private Action1<Response<User>> saveUserInfo() {
        return new Action1<Response<User>>() {
            @Override
            public void call(Response<User> userResponse) {
                // save access bundle
                User newUser = userResponse.getData();

                User user = currentUser.getCurrentUser();
                user.setAvatar(newUser.getAvatar());
                user.setDescription(newUser.getDescription());
                user.setUsername(newUser.getUsername());

                AccessBundle accessBundle = currentUser.getAccessBundle();
                accessBundle.setUser(user);
                currentUser.setInstance(accessBundle);

                PropertyStorage propertyStorage = new PropertyStorage(context);
                String data = gson.toJson(accessBundle);
                propertyStorage.putString(CurrentUser.PROPERTY_NAME, data);
            }
        };
    }
}
