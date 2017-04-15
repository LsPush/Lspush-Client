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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.decay.zxing.BarcodeCallback;
import com.decay.zxing.BarcodeResult;
import com.decay.zxing.CaptureManager;
import com.decay.zxing.DecoratedBarcodeView;
import com.google.zxing.ResultPoint;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.framework.ToolbarActivity;

import java.util.List;

public class ZxingActivity extends ToolbarActivity {
    public static final String REQUEST_DATA_CONTENT = "com.tomeokin.lspush.module.main.com.ZxingActivity.content";
    private String OPEN_FLASH;
    private String CLOSE_FLASH;

    DecoratedBarcodeView barcodeScannerView;
    CaptureManager capture;

    private Handler handler;
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(final BarcodeResult result) {
            barcodeScannerView.pause();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ZxingActivity.this, "扫描结果：" + result.getText(), Toast.LENGTH_SHORT).show();
                    saveResult(result);
                }
            });
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {}
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_zxing);

        setNavigationIcon(R.drawable.close);
        setDisplayHomeAsUpEnabled(true);
        setToolbarTitle(R.string.scan_qrcode);

        handler = new Handler(Looper.getMainLooper());
        barcodeScannerView = (DecoratedBarcodeView) findViewById(R.id.zxing_scanner);
        //barcodeScannerView.setTorchListener(this);

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decodeSingle(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
        @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_zxing, menu);
        MenuItem flashItem = menu.findItem(R.id.action_flash);
        if (hasFlash()) {
            OPEN_FLASH = getString(R.string.open_flash);
            CLOSE_FLASH = getString(R.string.close_flash);
            flashItem.setIcon(R.drawable.flash_off);
            flashItem.setTitle(OPEN_FLASH);
            flashItem.setVisible(true);
            barcodeScannerView.setTorchOff();
        } else {
            flashItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_flash) {
            if (item.getTitle().equals(OPEN_FLASH)) {
                item.setTitle(CLOSE_FLASH);
                item.setIcon(R.drawable.flash_on);
                barcodeScannerView.setTorchOn();
            } else {
                item.setTitle(OPEN_FLASH);
                item.setIcon(R.drawable.flash_off);
                barcodeScannerView.setTorchOff();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveResult(BarcodeResult result) {
        Intent data = new Intent();
        data.putExtra(REQUEST_DATA_CONTENT, result.getText());
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    public static String parseResult(Intent data) {
        return data.getStringExtra(REQUEST_DATA_CONTENT);
    }

    /**
     * Check if the device's camera has a Flashlight.
     *
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}
