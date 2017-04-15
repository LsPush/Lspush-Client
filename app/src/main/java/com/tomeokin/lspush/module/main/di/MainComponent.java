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
package com.tomeokin.lspush.module.main.di;

import com.tomeokin.lspush.module.main.com.CollectEditorActivity;
import com.tomeokin.lspush.module.main.com.CollectWebViewActivity;
import com.tomeokin.lspush.module.main.com.FindFragment;
import com.tomeokin.lspush.module.main.com.HomeFragment;
import com.tomeokin.lspush.module.main.com.MainActivity;
import com.tomeokin.lspush.module.main.com.MeFragment;
import com.tomeokin.lspush.module.main.com.TagActivity;

import dagger.Subcomponent;

@MainScope
@Subcomponent(modules = { MainModule.class })
public interface MainComponent {
    void inject(MainActivity activity);

    void inject(HomeFragment fragment);

    void inject(FindFragment fragment);

    void inject(MeFragment fragment);

    void inject(CollectWebViewActivity activity);

    void inject(CollectEditorActivity activity);

    void inject(TagActivity activity);
}
