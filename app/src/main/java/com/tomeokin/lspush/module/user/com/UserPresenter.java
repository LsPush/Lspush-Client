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
package com.tomeokin.lspush.module.user.com;

import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.Response;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.model.UserProfile;
import com.tomeokin.lspush.data.remote.LsPushService;
import com.tomeokin.lspush.data.remote.RxRequestCallback;
import com.tomeokin.lspush.module.app.RequestExecutor;
import com.tomeokin.lspush.module.user.di.UserScope;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

@UserScope
public class UserPresenter {
    private final LsPushService lsPushService;
    private final RequestExecutor executor;

    @Inject
    public UserPresenter(LsPushService lsPushService, RequestExecutor executor) {
        this.lsPushService = lsPushService;
        this.executor = executor;
    }

    public void getUserProfile(final long userId, RxRequestCallback<UserProfile> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<UserProfile>() {
            @Override
            public Observable<Response<UserProfile>> provide() {
                return lsPushService.getUserProfile(userId);
            }
        });
    }

    public void followUser(final long userId, RxRequestCallback<Void> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<Void>() {
            @Override
            public Observable<Response<Void>> provide() {
                return lsPushService.follow(userId);
            }
        });
    }

    public void unfollowUser(final long userId, RxRequestCallback<Void> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<Void>() {
            @Override
            public Observable<Response<Void>> provide() {
                return lsPushService.unfollow(userId);
            }
        });
    }

    public void getUserCollects(final boolean isUserFavor, final long userId, final int page, final int size,
        RxRequestCallback<List<Collect>> callback) {
        if (isUserFavor) {
            getUserFavorCollects(userId, page, size, callback);
        } else {
            getUserCollects(userId, page, size, callback);
        }
    }

    public void getUserCollects(final long userId, final int page, final int size,
        RxRequestCallback<List<Collect>> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<List<Collect>>() {
            @Override
            public Observable<Response<List<Collect>>> provide() {
                return lsPushService.getUserCollects(userId, page, size);
            }
        });
    }

    public void getUserFavorCollects(final long userId, final int page, final int size,
        RxRequestCallback<List<Collect>> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<List<Collect>>() {
            @Override
            public Observable<Response<List<Collect>>> provide() {
                return lsPushService.getUserFavorCollects(userId, page, size);
            }
        });
    }

    public void getUserFollowers(final long targetUserId, final boolean isFollower, final int page, final int size,
        RxRequestCallback<List<User>> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<List<User>>() {
            @Override
            public Observable<Response<List<User>>> provide() {
                Observable<Response<List<User>>> targetObservable;
                if (isFollower) {
                    targetObservable = lsPushService.getUserFollowers(targetUserId, page, size);
                } else {
                    targetObservable = lsPushService.getUserFollowing(targetUserId, page, size);
                }
                return targetObservable;
            }
        });
    }
}
