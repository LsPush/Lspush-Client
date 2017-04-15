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

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.decay.di.ProvideComponent;
import com.tomeokin.lspush.framework.ToolbarActivity;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.main.di.MainComponent;

public class TagActivity extends ToolbarActivity implements ProvideComponent<MainComponent> {

    @Override
    public MainComponent component() {
        return ((App) getApplication()).mainComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        component().inject(this);


    }
}
