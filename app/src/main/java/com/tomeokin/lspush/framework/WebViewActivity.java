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
package com.tomeokin.lspush.framework;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.widget.TouchWebView;

public class WebViewActivity extends NavActivity implements AppBarLayout.OnOffsetChangedListener {
    protected CoordinatorLayout rootLayout;
    protected AppBarLayout appBar;
    protected Toolbar toolbar;
    protected ActionBar actionBar;
    private TextView toolbarTitle;
    protected View toolbarDivider;
    protected FrameLayout bottomLayout;
    protected BottomSheetBehavior bottomSheetBehavior;

    protected TouchWebView x5WebView;
    protected ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);
        appBar = (AppBarLayout) findViewById(R.id.appBar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarDivider = findViewById(R.id.toolbar_divider);
        bottomLayout = (FrameLayout) findViewById(R.id.bottom_container);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.statusBarColor), 30);
        }

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            setToolbarTitle(getTitle());
        }

        //bottomSheetBehavior = BottomSheetBehavior.from(bottomLayout);
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //nestedScrollView = (NestedScrollView) findViewById(R.id.webView_container);
        x5WebView = (TouchWebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar.setMax(100);
        x5WebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                    onWebViewLoadComplete();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                setToolbarTitle(title);
            }
        });
    }

    public void onWebViewLoadComplete() {}

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        ViewCompat.setTranslationY(progressBar, verticalOffset);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        appBar.addOnOffsetChangedListener(this);
    }

    @Override
    public void onDetachedFromWindow() {
        appBar.removeOnOffsetChangedListener(this);
        super.onDetachedFromWindow();
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

    public void setDisplayHomeAsUpEnabled(boolean enabled, View.OnClickListener listener) {
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enabled);
        }
        toolbar.setNavigationOnClickListener(listener);
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

    public void setBottomBar(@LayoutRes int layoutResID) {
        clearBottomLayout();
        LayoutInflater inflater = getLayoutInflater();
        inflater.inflate(layoutResID, bottomLayout, true);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomLayout);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void setBottomBar(@NonNull View view) {
        clearBottomLayout();
        bottomLayout.addView(view);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomLayout);
    }

    public void setBottomBar(@NonNull View view, ViewGroup.LayoutParams params) {
        clearBottomLayout();
        bottomLayout.addView(view, params);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomLayout);
    }

    protected void clearBottomLayout() {
        if (bottomLayout.getChildCount() != 0) {
            bottomLayout.removeAllViews();
        }
    }

    public boolean isBottomBarShowing() {
        return bottomSheetBehavior != null && (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
            || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_SETTLING);
    }
}
