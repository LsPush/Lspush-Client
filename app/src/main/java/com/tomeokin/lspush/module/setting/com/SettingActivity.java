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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.decay.di.ProvideComponent;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.framework.ToolbarActivity;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.setting.di.SettingComponent;

public class SettingActivity extends ToolbarActivity implements ProvideComponent<SettingComponent> {

    @Override
    public SettingComponent component() {
        return ((App) getApplication()).settingComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        component().inject(this);

        setToolbarTitle(R.string.app_setting);
        setDisplayHomeAsUpEnabled(true);
        setContentLayout(R.layout.activity_setting);
    }

    @Override
    protected void onDestroy() {
        ((App) getApplication()).releaseSettingComponent();
        super.onDestroy();
    }

    public void onAccountManageClick(View view) {
        navUtils.startActivity(UserEditorActivity.class);
    }

    public void onAppAboutClick(View view) {
        navUtils.startActivity(AboutActivity.class);
    }
}
