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
package com.tomeokin.lspush.module.splash.com;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.decay.di.ProvideComponent;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.model.AccessBundle;
import com.tomeokin.lspush.data.remote.RxRequestCallback;
import com.tomeokin.lspush.framework.NavActivity;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.auth.com.AuthActivity;
import com.tomeokin.lspush.module.main.com.MainActivity;
import com.tomeokin.lspush.module.splash.di.SplashComponent;

import javax.inject.Inject;

public class SplashActivity extends NavActivity
    implements ProvideComponent<SplashComponent>, RxRequestCallback<AccessBundle> {

    @Inject SplashPresenter splashPresenter;

    @Override
    public SplashComponent component() {
        return ((App) getApplication()).splashComponent(this);
    }

    @Override
    protected void onDestroy() {
        ((App) getApplication()).releaseSplashComponent();
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        component().inject(this);
        splashPresenter.loadCurrentUser(this);
    }

    @Override
    public void onRequestSuccess(AccessBundle data) {
        goToMain();
    }

    @Override
    public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
        navUtils.startActivity(AuthActivity.class);
        finish();
    }

    @Override
    public void onRequestComplete() {
        finish();
    }

    @Override
    public void onNetworkOffline() {
        Toast.makeText(this, R.string.network_offline, Toast.LENGTH_SHORT).show();
        goToMain();
    }

    @Override
    public void onTokenTimeOut() {
        navUtils.startActivity(AuthActivity.class);
        finish();
    }

    public void goToMain() {
        if (isTaskRoot()) {
            navUtils.startActivity(MainActivity.class);
        }
        finish();
    }
}
