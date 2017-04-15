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
package com.tomeokin.lspush.module.app;

import android.app.Application;
import android.content.Context;

import com.decay.di.qualifier.AppContext;
import com.google.gson.Gson;
import com.tomeokin.lspush.data.internal.CurrentUser;
import com.tomeokin.lspush.framework.prefer.Prefer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final Application application;

    public AppModule(final Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application application() {
        return application;
    }

    @Provides
    @Singleton
    @AppContext
    Context provideAppContext() {
        return application;
    }

    @Provides
    @Singleton
    CurrentUser provideCurrentUser(@AppContext Context context, Gson gson) {
        return new CurrentUser(gson, context);
    }

    @Provides
    @Singleton
    Prefer providePrefer(@AppContext Context context, Gson gson) {
        return new Prefer(context, gson);
    }
}
