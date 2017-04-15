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

import com.decay.di.component.BaseAppComponent;
import com.tomeokin.lspush.data.remote.LsPushApiModule;
import com.tomeokin.lspush.module.auth.di.AuthComponent;
import com.tomeokin.lspush.module.auth.di.AuthModule;
import com.tomeokin.lspush.module.main.di.MainComponent;
import com.tomeokin.lspush.module.main.di.MainModule;
import com.tomeokin.lspush.module.search.di.SearchComponent;
import com.tomeokin.lspush.module.search.di.SearchModule;
import com.tomeokin.lspush.module.setting.di.SettingComponent;
import com.tomeokin.lspush.module.setting.di.SettingModule;
import com.tomeokin.lspush.module.splash.di.SplashComponent;
import com.tomeokin.lspush.module.splash.di.SplashModule;
import com.tomeokin.lspush.module.user.di.UserComponent;
import com.tomeokin.lspush.module.user.di.UserModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { AppModule.class, LsPushApiModule.class })
public interface AppComponent extends BaseAppComponent {
    AuthComponent loginComponent(AuthModule authModule);

    SplashComponent splashComponent(SplashModule splashModule);

    MainComponent mainComponent(MainModule mainModule);

    UserComponent userComponent(UserModule userModule);

    SettingComponent settingComponent(SettingModule settingModule);

    SearchComponent searchComponent(SearchModule searchModule);
}
