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

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.tomeokin.lspush.R;

public class DefaultToolbar extends ToolbarComponent {
    protected AppBarLayout appBar;
    protected Toolbar toolbar;
    protected ActionBar actionBar;
    private TextView toolbarTitle;
    protected ImageView toolbarLogo;
    protected View toolbarDivider;
    protected FrameLayout contentLayout;

    public DefaultToolbar(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_toolbar;
    }

    @Override
    public void populate(AppCompatActivity activity, @NonNull View view) {
        appBar = (AppBarLayout) findViewById(R.id.appBar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarLogo = (ImageView) findViewById(R.id.toolbar_logo);
        toolbarDivider = findViewById(R.id.toolbar_divider);
        contentLayout = (FrameLayout) findViewById(R.id.content_container);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            StatusBarUtil.setColor(activity, ContextCompat.getColor(activity, R.color.statusBarColor), 30);
        }

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            setToolbarTitle(activity.getTitle());
        }
    }

    public void setNavigationIcon(@DrawableRes int resId) {
        if (actionBar != null) actionBar.setHomeAsUpIndicator(resId);
        toolbar.setNavigationIcon(resId);
    }

    public void setNavigationIcon(@Nullable Drawable icon) {
        if (actionBar != null) actionBar.setHomeAsUpIndicator(icon);
        toolbar.setNavigationIcon(icon);
    }

    public void setDisplayHomeAsUpEnabled(boolean enabled) {
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enabled);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void setToolbarTitleGravity(int gravity) {
        ViewGroup.LayoutParams lp = toolbarTitle.getLayoutParams();

        if (lp != null && lp instanceof Toolbar.LayoutParams) {
            Toolbar.LayoutParams params = (Toolbar.LayoutParams) lp;
            params.gravity = gravity;
        }
    }

    public void setToolbarTitle(CharSequence title) {
        toolbarTitle.setText(title);
    }

    public void setToolbarTitle(@StringRes int title) {
        toolbarTitle.setText(title);
    }

    public void showToolbarDivider(boolean show) {
        toolbarDivider.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setContentLayout(@LayoutRes int layoutResID) {
        clearContentLayout();
        layoutInflater.inflate(layoutResID, contentLayout, true);
    }

    public void setContentLayout(@NonNull View view) {
        clearContentLayout();
        contentLayout.addView(view);
    }

    public void setContentLayout(@NonNull View view, ViewGroup.LayoutParams params) {
        clearContentLayout();
        contentLayout.addView(view, params);
    }

    protected void clearContentLayout() {
        if (contentLayout.getChildCount() != 0) {
            contentLayout.removeAllViews();
        }
    }
}
