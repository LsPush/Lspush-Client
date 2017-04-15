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
package com.tomeokin.lspush.module.main.com;

import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.Comment;
import com.tomeokin.lspush.data.model.Response;
import com.tomeokin.lspush.data.remote.LsPushService;
import com.tomeokin.lspush.data.remote.RxRequestCallback;
import com.tomeokin.lspush.module.app.RequestExecutor;
import com.tomeokin.lspush.module.main.di.MainScope;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

@MainScope
public class HomePresenter {
    private final LsPushService lsPushService;
    private final RequestExecutor executor;

    @Inject
    public HomePresenter(RequestExecutor executor, LsPushService lsPushService) {
        this.executor = executor;
        this.lsPushService = lsPushService;
    }

    public void getNewestCollect(final int page, final int size, RxRequestCallback<List<Collect>> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<List<Collect>>() {
            @Override
            public Observable<Response<List<Collect>>> provide() {
                return lsPushService.getNewestCollects(page, size);
            }
        });
    }

    public void addFavor(final long colId, RxRequestCallback<Void> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<Void>() {
            @Override
            public Observable<Response<Void>> provide() {
                return lsPushService.addFavor(colId);
            }
        });
    }

    public void removeFavor(final long colId, RxRequestCallback<Void> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<Void>() {
            @Override
            public Observable<Response<Void>> provide() {
                return lsPushService.removeFavor(colId);
            }
        });
    }

    public void getComments(final long colId, final int page, final int size,
        RxRequestCallback<List<Comment>> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<List<Comment>>() {
            @Override
            public Observable<Response<List<Comment>>> provide() {
                return lsPushService.findComment(colId, page, size);
            }
        });
    }

    public void addComment(final long colId, final String comment, RxRequestCallback<Comment> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<Comment>() {
            @Override
            public Observable<Response<Comment>> provide() {
                return lsPushService.addComment(colId, comment);
            }
        });
    }

    public void getUrlInfo(final String url, RxRequestCallback<Collect> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<Collect>() {
            @Override
            public Observable<Response<Collect>> provide() {
                return lsPushService.getUrlInfo(url);
            }
        });
    }

    public void postCollect(final Collect collect, RxRequestCallback<Void> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<Void>() {
            @Override
            public Observable<Response<Void>> provide() {
                return lsPushService.postCollect(collect);
            }
        });
    }

    public void findHotCollects(final int page, final int size, RxRequestCallback<List<Collect>> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<List<Collect>>() {
            @Override
            public Observable<Response<List<Collect>>> provide() {
                return lsPushService.findHotCollects(page, size);
            }
        });
    }

    public void findRecentTopHotCollect(final int days, RxRequestCallback<List<Collect>> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<List<Collect>>() {
            @Override
            public Observable<Response<List<Collect>>> provide() {
                return lsPushService.findRecentTopHotCollects(days);
            }
        });
    }
}
