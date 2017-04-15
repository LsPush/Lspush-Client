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

import android.app.Activity;
import android.app.Application;
import android.os.Environment;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.leakcanary.LeakCanary;
import com.tomeokin.lspush.app.NetworkUtils;
import com.tomeokin.lspush.app.crypto.AppCrypto;
import com.tomeokin.lspush.app.logger.AppLogger;
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
import com.tomeokin.lspush.widget.WebViewLoader;

public class App extends Application {
    public static final String APP_NAME = "LsPush";
    public static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String EXTERNAL_STORAGE_DIRECTION = SDCARD + "/" + APP_NAME.toLowerCase();

    private AppComponent appComponent;
    private AuthComponent authComponent;
    private SplashComponent splashComponent;
    private MainComponent mainComponent;
    private UserComponent userComponent;
    private SettingComponent settingComponent;
    private SearchComponent searchComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        //LeakCanary.install(this);

        AppLogger.init(this);
        AppCrypto.init(this);
        NetworkUtils.init(this);
        AndroidThreeTen.init(this);
        WebViewLoader.init(this);
    }

    public AppComponent appComponent() {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        }
        return appComponent;
    }

    public static AppComponent appComponent(Activity activity) {
        return ((App) activity.getApplication()).appComponent();
    }

    public AuthComponent authComponent(Activity activity) {
        if (authComponent == null) {
            authComponent = appComponent().loginComponent(new AuthModule(activity));
        }
        return authComponent;
    }

    public void releaseAuthComponent() {
        authComponent = null;
    }

    public SplashComponent splashComponent(Activity activity) {
        if (splashComponent == null) {
            splashComponent = appComponent().splashComponent(new SplashModule(activity));
        }
        return splashComponent;
    }

    public void releaseSplashComponent() {
        splashComponent = null;
    }

    public MainComponent mainComponent(Activity activity) {
        if (mainComponent == null) {
            mainComponent = appComponent().mainComponent(new MainModule(activity));
        }
        return mainComponent;
    }

    public void releaseMainComponent() {
        mainComponent = null;
    }

    public UserComponent userComponent(Activity activity) {
        if (userComponent == null) {
            userComponent = appComponent.userComponent(new UserModule(activity));
        }
        return userComponent;
    }

    public void releaseUserComponent() {
        userComponent = null;
    }

    public SettingComponent settingComponent(Activity activity) {
        if (settingComponent == null) {
            settingComponent = appComponent.settingComponent(new SettingModule(activity));
        }
        return settingComponent;
    }

    public void releaseSettingComponent() {
        settingComponent = null;
    }

    public SearchComponent searchComponent(Activity activity) {
        if (searchComponent == null) {
            searchComponent = appComponent.searchComponent(new SearchModule(activity));
        }
        return searchComponent;
    }

    public void releaseSearchComponent() {
        searchComponent = null;
    }
}
