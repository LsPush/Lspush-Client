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
package com.tomeokin.lspush.module.auth.di;

import com.tomeokin.lspush.module.auth.com.AuthActivity;
import com.tomeokin.lspush.module.auth.com.AuthPhoneFragment;
import com.tomeokin.lspush.module.auth.com.LoginFragment;
import com.tomeokin.lspush.module.auth.com.RegisterFragment;

import dagger.Subcomponent;

@AuthScope
@Subcomponent(modules = { AuthModule.class })
public interface AuthComponent {
    void inject(AuthActivity authActivity);

    void inject(AuthPhoneFragment fragment);

    void inject(RegisterFragment fragment);

    void inject(LoginFragment fragment);
}