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
package com.tomeokin.lspush.module.search.com;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.remote.LsPushService;

public class SearchOptionDialog extends Dialog {
    EditText searchEditText;
    TextView optionTitleTv;
    TextView optionTagTv;
    TextView optionUrlTv;
    RadioGroup optionGroupRg;

    final View.OnClickListener onOptionSelectedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String option = (String) v.getTag(R.id.search_option_id);
            performSearch(option);
        }
    };

    OnSearchCollectListener onSearchCollectListener;

    public SearchOptionDialog(@NonNull Context context, @NonNull OnSearchCollectListener listener) {
        //super(context, R.style.ZoomPopTheme);
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window window = getWindow();
        if (window != null) {
            window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        setContentView(R.layout.dialog_search_collect);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Activity activity = getOwnerActivity();
            if (activity != null) {
                StatusBarUtil.setColor(getOwnerActivity(), ContextCompat.getColor(getContext(), R.color.statusBarColor), 30);
            }
        }

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.x = 0;
        attributes.y = 0;
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.windowAnimations = android.R.style.Animation;
        attributes.gravity = Gravity.TOP | Gravity.LEFT;

        onSearchCollectListener = listener;

        searchEditText = (EditText) findViewById(R.id.search_editText);
        TextView cancelBtn = (TextView) findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        optionTitleTv = (TextView) findViewById(R.id.option_title_tv);
        optionTagTv = (TextView) findViewById(R.id.option_tag_tv);
        optionUrlTv = (TextView) findViewById(R.id.option_url_tv);
        optionGroupRg = (RadioGroup) findViewById(R.id.option_group_rg);

        optionTitleTv.setTag(R.id.search_option_id, LsPushService.SEARCH_COLLECT_OPTION_TITLE);
        optionTagTv.setTag(R.id.search_option_id, LsPushService.SEARCH_COLLECT_OPTION_TAG);
        optionUrlTv.setTag(R.id.search_option_id, LsPushService.SEARCH_COLLECT_OPTION_URL);

        optionTitleTv.setOnClickListener(onOptionSelectedListener);
        optionTagTv.setOnClickListener(onOptionSelectedListener);
        optionUrlTv.setOnClickListener(onOptionSelectedListener);

        optionGroupRg.check(R.id.option_group_all);
    }

    public void performSearch(String option) {
        String keyword = searchEditText.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            return;
        }

        int optionGroupId = optionGroupRg.getCheckedRadioButtonId();
        String group;
        switch (optionGroupId) {
            case R.id.option_group_user:
                group = LsPushService.SEARCH_COLLECT_GROUP_USER;
                break;
            case R.id.option_group_userFavor:
                group = LsPushService.SEARCH_COLLECT_GROUP_FAVOR;
                break;
            case R.id.option_group_all:
            default:
                group = LsPushService.SEARCH_COLLECT_GROUP_ALL;
                break;
        }

        dismiss();
        onSearchCollectListener.searchCollect(option, group, keyword);
    }

    public void show(String keyword) {
        searchEditText.setText(keyword);
        if (!TextUtils.isEmpty(keyword)) {
            int offset = keyword.length();
            searchEditText.setSelection(offset);
        }
        super.show();
    }

    public interface OnSearchCollectListener {
        void searchCollect(String option, String group, @NonNull String keyword);
    }
}
