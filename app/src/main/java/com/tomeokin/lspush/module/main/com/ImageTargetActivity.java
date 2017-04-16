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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.decay.logger.Log;
import com.decay.utillty.ClipboardUtils;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.framework.WebViewActivity;
import com.tomeokin.lspush.module.main.ImageDialog;

public class ImageTargetActivity extends WebViewActivity
    implements View.OnTouchListener, ImageDialog.OnDialogImageConfirmListener {
    private Log log = AppLogger.of("ImageTargetActivity");

    public static final String ARG_WEB_URL = "arg.web.url";
    public static final String REQUEST_DATA_IMAGE_URL
        = "com.tomeokin.lspush.module.main.com.ImageTargetActivity.imageUrl";
    private String webUrl;
    private static final int MSG_SELECT_IMAGE = 0;

    Handler handler;
    String image;
    int width;
    int height;
    float touchX, touchY;
    private boolean selected;

    public static Bundle create(@NonNull String webUrl) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_WEB_URL, webUrl);
        return bundle;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            webUrl = getIntent().getStringExtra(ARG_WEB_URL);
        }
        if (TextUtils.isEmpty(webUrl) || !URLUtil.isNetworkUrl(webUrl)) {
            Toast.makeText(this, "不是有效的网址：" + webUrl, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setNavigationIcon(R.drawable.close);
        setDisplayHomeAsUpEnabled(true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResultAndBack();
            }
        });

        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                log.log("handleMessage: image %s, width %d, height %d", image, width, height);
                showImageDialog();
            }
        };

        x5WebView.setPadding(x5WebView.getPaddingLeft(), x5WebView.getPaddingTop(), x5WebView.getPaddingRight(), 0);

        x5WebView.setWebViewClient(new WebViewClient() {
            /**
             * 防止加载网页时调起系统浏览器
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                webView.loadUrl(url);
                return true;
            }
        });
        x5WebView.getSettings().setJavaScriptEnabled(true);
        // https://labs.mwrinfosecurity.com/blog/webview-addjavascriptinterface-remote-code-execution/
        x5WebView.addJavascriptInterface(new ImageClickInterface(), "imageClick");
        x5WebView.setOnTouchListener(this);
        // http://blog.csdn.net/u013107656/article/details/51729398
        x5WebView.removeJavascriptInterface("searchBoxJavaBridge_");
        x5WebView.removeJavascriptInterface("accessibilityTraversal");
        x5WebView.removeJavascriptInterface("accessibility");

        x5WebView.loadUrl(webUrl);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Must be divided by the density of the screen
        final float density = getResources().getDisplayMetrics().density;
        float touchX = event.getX() / density;
        float touchY = event.getY() / density;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            this.touchX = touchX;
            this.touchY = touchY;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            float dx = Math.abs(touchX - this.touchX);
            float dy = Math.abs(touchY - this.touchY);
            if (dx < 10.0 / density && dy < 10.0 / density) {
                clickImage(touchX, touchY);
            }
        }
        return false;
    }

    public void clickImage(float touchX, float touchY) {
        final String js = getString(R.string.web_image_click, touchX, touchY);
        x5WebView.loadUrl(js);
    }

    public class ImageClickInterface {

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void onImageClick(String url, int w, int h) {
            final float density = getResources().getDisplayMetrics().density;
            width = (int) (w * density);
            height = (int) (h * density);
            image = url;
            log.log("onImageClick: image %s, width %d, height %d", url, w, h);
            Message.obtain(handler, MSG_SELECT_IMAGE).sendToTarget();
        }
    }

    public void showImageDialog() {
        ImageDialog dialog = new ImageDialog(self(), image, width, height, this);
        dialog.show();
    }

    @Override
    public void onImageConfirm(String imgUrl, int width, int height) {
        selected = true;
        saveResultAndBack();
    }

    @Override
    public void onBackPressed() {
        saveResultAndBack();
    }

    public void saveResultAndBack() {
        if (selected && !TextUtils.isEmpty(image)) {
            Intent data = new Intent();
            data.putExtra(REQUEST_DATA_IMAGE_URL, image);
            setResult(Activity.RESULT_OK, data);
        }

        super.onBackPressed();
    }

    public static String resolveData(Intent data) {
        return data.getStringExtra(REQUEST_DATA_IMAGE_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_target, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_copy_current_link:
                String currentUrl = x5WebView.getUrl();
                ClipboardUtils.setText(this, currentUrl);
                Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_share:
                String text = x5WebView.getTitle() + " " + x5WebView.getUrl();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "分享到"));
                return true;
            case R.id.action_open_in_browser:
                Intent viewIntent = new Intent();
                viewIntent.setAction(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(x5WebView.getUrl());
                viewIntent.setData(uri);
                startActivity(Intent.createChooser(viewIntent, "选择"));
                return true;
            case R.id.action_refresh:
                x5WebView.reload();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
