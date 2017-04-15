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
package com.tomeokin.lspush.module.setting.di;

import android.content.Context;

import com.decay.di.qualifier.ActivityContext;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingModule {
    private Context context;

    public SettingModule(Context context) {
        this.context = context;
    }

    @Provides
    @SettingScope
    @ActivityContext
    public Context provideContext() {
        return context;
    }
}