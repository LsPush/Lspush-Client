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
import android.widget.TextView;

import com.tomeokin.lspush.R;
import com.tomeokin.lspush.framework.ToolbarActivity;

public class AboutActivity extends ToolbarActivity {
    TextView versionTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setToolbarTitle("关于");
        setDisplayHomeAsUpEnabled(true);
        setContentLayout(R.layout.activity_about);

        versionTv = (TextView) findViewById(R.id.version_tv);
        versionTv.setText(String.format("版本号：%s", getString(R.string.app_version)));
    }
}
