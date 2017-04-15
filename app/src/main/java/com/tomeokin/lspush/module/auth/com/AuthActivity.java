/*
 * Copyright 2017 LsPush
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
package com.tomeokin.lspush.module.auth.com;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.decay.di.ProvideComponent;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.framework.ToolbarActivity;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.auth.di.AuthComponent;
import com.tomeokin.lspush.module.main.com.MainActivity;
import com.tomeokin.lspush.widget.NotificationBar;

public class AuthActivity extends ToolbarActivity implements ProvideComponent<AuthComponent> {
    protected NotificationBar notificationBar;

    @Override
    public AuthComponent component() {
        return ((App) getApplication()).authComponent(this);
    }

    @Override
    protected void onDestroy() {
        ((App)getApplication()).releaseAuthComponent();
        super.onDestroy();
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, AuthActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_auth);
        navUtils.setContainerId(R.id.fragment_container);

        component().inject(this);

        notificationBar = (NotificationBar) findViewById(R.id.notification_bar);

        appBar.setBackgroundColor(Color.TRANSPARENT);
        setNavigationIcon(R.drawable.close);
        setDisplayHomeAsUpEnabled(true);
        showToolbarDivider(false);

        //gotoPhoneAuth();
        //gotoRegister("123456");
        gotoLogin();
    }

    public void gotoRegister(String ticket) {
        setToolbarTitle(R.string.register);
        navUtils.moveTo(RegisterFragment.class, RegisterFragment.create(ticket));
    }

    public void gotoLogin() {
        setToolbarTitle(R.string.login);
        navUtils.moveTo(LoginFragment.class);
    }

    public void gotoPhoneAuth() {
        setToolbarTitle(R.string.register);
        navUtils.moveTo(AuthPhoneFragment.class);
    }

    public void showErrorMessage(String message) {
        notificationBar.showTemporaryInverse(message);
    }

    public void gotoMain() {
        if (isTaskRoot()) {
            navUtils.startActivity(MainActivity.class);
        }
        finish();
    }
}
