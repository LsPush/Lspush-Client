/**
 * Copyright 2016 Gredus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomeokin.lspush.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class TouchWebView extends WebView implements Handler.Callback, View.OnTouchListener {
    private static final int CLICK_ON_WEBVIEW = 1;
    private static final int CLICK_ON_URL = 2;
    private OnWebViewClickListener onWebViewClickListener;

    private Handler handler;

    public TouchWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchWebView(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (handler != null) {
            handler.removeMessages(CLICK_ON_URL);
            handler.removeMessages(CLICK_ON_WEBVIEW);
            handler = null;
        }
        super.onDetachedFromWindow();
    }

    private void initWebViewSettings() {
        WebSettings webSetting = this.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        //webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        //webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        //webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
        // settings 的设计
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        initWebViewSettings();
        getView().setClickable(true);
        getView().setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        //WebSettings settings = getSettings();
        ////settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        ////settings.setUseWideViewPort(true);
        //settings.setDisplayZoomControls(false); // 设置显示缩放按钮
        //settings.setBuiltInZoomControls(true);
        //settings.setSupportZoom(true); // 支持缩放
        //settings.setUseWideViewPort(true);
        //settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        //settings.setLoadWithOverviewMode(true);
        //settings.setAppCacheEnabled(true);
        ////webSetting.setDatabaseEnabled(true);
        //settings.setDomStorageEnabled(true);
        //settings.setGeolocationEnabled(true);
        //settings.setAppCacheMaxSize(Long.MAX_VALUE);
        //// webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        ////webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isInEditMode()) {
            return super.onTouchEvent(event);
        }

        if (onWebViewClickListener != null) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                handler.sendEmptyMessageDelayed(CLICK_ON_WEBVIEW, 500);
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                handler.removeMessages(CLICK_ON_WEBVIEW);
            }

            requestDisallowInterceptTouchEvent(true);
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (onWebViewClickListener != null) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                handler.sendEmptyMessageDelayed(CLICK_ON_WEBVIEW, 500);
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                handler.removeMessages(CLICK_ON_WEBVIEW);
            }
        }

        return false;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == CLICK_ON_URL) {
            handler.removeMessages(CLICK_ON_WEBVIEW);
            return true;
        }
        if (msg.what == CLICK_ON_WEBVIEW) {
            if (onWebViewClickListener != null) {
                onWebViewClickListener.onClick(this);
            }
            return true;
        }
        return false;
    }

    public OnWebViewClickListener getOnWebViewClickListener() {
        return onWebViewClickListener;
    }

    public void setOnWebViewClickListener(OnWebViewClickListener onWebViewClickListener) {
        this.onWebViewClickListener = onWebViewClickListener;
        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                handler.sendEmptyMessage(CLICK_ON_URL);
                return super.shouldOverrideUrlLoading(webView, s);
            }
        });
        setOnTouchListener(this);
        if (handler == null) {
            handler = new Handler(this);
        }
    }

    public interface OnWebViewClickListener extends OnClickListener { }
}
