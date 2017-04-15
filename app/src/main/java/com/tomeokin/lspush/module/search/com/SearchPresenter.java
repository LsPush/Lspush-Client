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
package com.tomeokin.lspush.module.search.com;

import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.Response;
import com.tomeokin.lspush.data.remote.LsPushService;
import com.tomeokin.lspush.data.remote.RxRequestCallback;
import com.tomeokin.lspush.module.app.RequestExecutor;
import com.tomeokin.lspush.module.search.di.SearchScope;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

@SearchScope
public class SearchPresenter {
    private final LsPushService lsPushService;
    private final RequestExecutor executor;

    @Inject
    public SearchPresenter(LsPushService lsPushService, RequestExecutor executor) {
        this.lsPushService = lsPushService;
        this.executor = executor;
    }

    public void searchCollect(final long targetUserId, final String option, final String group, final String keyword,
        final int page, final int size, RxRequestCallback<List<Collect>> callback) {
        executor.authExecute(callback, new RequestExecutor.ProvideRequest<List<Collect>>() {
            @Override
            public Observable<Response<List<Collect>>> provide() {
                return lsPushService.findCollect(targetUserId, option, group, keyword, page, size);
            }
        });
    }
}
