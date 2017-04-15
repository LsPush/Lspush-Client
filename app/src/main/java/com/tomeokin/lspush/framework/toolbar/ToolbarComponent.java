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
package com.tomeokin.lspush.framework.toolbar;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

public abstract class ToolbarComponent {
    protected LayoutInflater layoutInflater;
    protected View contentView;

    protected AppCompatActivity activity;

    public ToolbarComponent(AppCompatActivity activity) {
        layoutInflater = LayoutInflater.from(activity);
        contentView = layoutInflater.inflate(getLayoutRes(), null);
        activity.setContentView(contentView);

        populate(activity, contentView);
    }

    @LayoutRes
    public abstract int getLayoutRes();

    public abstract void populate(AppCompatActivity activity, @NonNull View view);

    public View build() {
        return contentView;
    }

    public View findViewById(@IdRes int id) {
        return activity.findViewById(id);
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        activity.setSupportActionBar(toolbar);
    }

    @Nullable
    public ActionBar getSupportActionBar() {
        return activity.getSupportActionBar();
    }

    public void onBackPressed() {
        activity.onBackPressed();
    }
}
