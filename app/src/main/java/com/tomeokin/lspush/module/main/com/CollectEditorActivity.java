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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.decay.callback.TextWatchAdapter;
import com.decay.di.ProvideComponent;
import com.decay.utillty.ClipboardUtils;
import com.decay.utillty.UrlUtils;
import com.decay.utillty.ViewHelper;
import com.tomeokin.lspush.BuildConfig;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.remote.RxRequestAdapter;
import com.tomeokin.lspush.framework.ToolbarActivity;
import com.tomeokin.lspush.framework.nav.ActivityResultAdapter;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.main.EditableTagAdapter;
import com.tomeokin.lspush.module.main.di.MainComponent;
import com.tomeokin.lspush.widget.TagFilter;

import java.util.Date;

import javax.inject.Inject;

public class CollectEditorActivity extends ToolbarActivity implements ProvideComponent<MainComponent> {
    private static final String ARG_COLLECT_URL = "arg.collect.url";
    private static final int REQUEST_SELECT_WEB_IMAGE = 135;
    private static final int REQUEST_ZXING_URL = 166;
    private Collect collect;
    private String httpUrl;
    private String imageUrl;

    EditText urlEt;
    TextView checkUrlTv;
    EditText titleEt;
    EditText descEt;
    ImageView addImageIv;
    ImageView imageIv;
    ImageView deleteImageIv;
    EditText collectTagEt;
    TextView addTagTv;
    RecyclerView tagRv;
    EditableTagAdapter tagAdapter;
    final TextWatcher onTagEditorChangeAdapter = new TextWatchAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            ViewHelper.setViewEnabled(addTagTv, s.length() > 0);
        }
    };

    @Inject HomePresenter homePresenter;

    @Override
    public MainComponent component() {
        return ((App) getApplication()).mainComponent(this);
    }

    public static Bundle create(String url) {
        Bundle bundle = new Bundle();
        if (url != null) bundle.putString(ARG_COLLECT_URL, url);
        return bundle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_collect_editor);

        component().inject(this);

        setDisplayHomeAsUpEnabled(true);
        setToolbarTitle(R.string.edit_collect_title);

        urlEt = (EditText) findViewById(R.id.collect_url_et);
        if (BuildConfig.DEBUG) {
            //urlEt.setText("https://www.baidu.com");
            urlEt.setText("https://github.com/LsPush");
        }

        checkUrlTv = (TextView) findViewById(R.id.check_url_tv);
        titleEt = (EditText) findViewById(R.id.collect_title_et);
        descEt = (EditText) findViewById(R.id.collect_desc_et);
        addImageIv = (ImageView) findViewById(R.id.collect_add_image_iv);
        imageIv = (ImageView) findViewById(R.id.collect_image_iv);
        deleteImageIv = (ImageView) findViewById(R.id.delete_image);
        collectTagEt = (EditText) findViewById(R.id.collect_tag_et);
        addTagTv = (TextView) findViewById(R.id.add_tag_tv);
        tagRv = (RecyclerView) findViewById(R.id.tag_rv);

        checkUrlTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCollectFromEditText();
            }
        });
        addImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectWebImage();
            }
        });
        deleteImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelectImage();
            }
        });

        collectTagEt.setFilters(new InputFilter[] { new TagFilter() });
        tagAdapter = new EditableTagAdapter(new EditableTagAdapter.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {}

            @Override
            public void onTagDelete(String tag) {}
        });
        tagRv.setLayoutManager(new LinearLayoutManager(context(), LinearLayoutManager.HORIZONTAL, false));
        tagRv.setAdapter(tagAdapter);
        addTagTv.setEnabled(false);
        collectTagEt.addTextChangedListener(onTagEditorChangeAdapter);
        addTagTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag();
            }
        });

        updateImage(null);
        if (getIntent() != null) {
            String url = getIntent().getStringExtra(ARG_COLLECT_URL);
            if (UrlUtils.isNetworkUrl(url)) {
                updateCollect(url);
            } else {
                // 如果无传入的网址，则从
                url = ClipboardUtils.getText(context());
                if (UrlUtils.isNetworkUrl(url)) {
                    updateCollect(url);
                }
            }
        }
    }

    public void selectWebImage() {
        final String url = urlEt.getText().toString().trim();
        if (!UrlUtils.isNetworkUrl(url)) {
            showErrorUrl(url);
            return;
        }

        navUtils.startActivityForResult(ImageTargetActivity.class, ImageTargetActivity.create(url),
            new ActivityResultAdapter(REQUEST_SELECT_WEB_IMAGE) {
                @Override
                public void onRequestSuccess(Intent data) {
                    String imgUrl = ImageTargetActivity.resolveData(data);
                    if (!TextUtils.isEmpty(imgUrl)) {
                        updateImage(imgUrl);
                    }
                }
            });
    }

    public void clearSelectImage() {
        updateImage(null);
    }

    public void addTag() {
        tagAdapter.addTag(collectTagEt.getText().toString());
        collectTagEt.setText(null);
    }

    public void updateCollectFromEditText() {
        String url = urlEt.getText().toString().trim();
        String netUrl = UrlUtils.reformUrl(url);
        updateCollectWithError(netUrl);
    }

    public void updateCollectWithError(String url) {
        if (checkUrl(url)) {
            updateCollect(httpUrl);
        } else {
            showErrorUrl(url);
        }
    }

    public boolean checkUrl(String url) {
        if (url == null) return false;

        if (url.equals(httpUrl)) return true;

        if (UrlUtils.isNetworkUrl(url)) {
            httpUrl = url;
            return true;
        }
        return false;
    }

    public void updateCollect(String url) {
        urlEt.setText(url);
        homePresenter.getUrlInfo(url, new RxRequestAdapter<Collect>(context()) {
            @Override
            public void onRequestSuccess(Collect data) {
                updateCollectView(data);
            }
        });
    }

    public void updateCollectView(Collect col) {
        collect = col;
        httpUrl = col.getUrl();
        urlEt.setText(col.getUrl());
        titleEt.setText(col.getTitle());
        descEt.setText(col.getDescription());
        //updateImage(col.getImage());
    }

    public void updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
        if (imageUrl == null || imageUrl.trim().length() == 0) {
            addImageIv.setVisibility(View.VISIBLE);
            addImageIv.setImageResource(R.drawable.add_image);
            imageIv.setVisibility(View.GONE);
            deleteImageIv.setVisibility(View.GONE);
        } else {
            addImageIv.setVisibility(View.GONE);
            imageIv.setVisibility(View.VISIBLE);
            Glide.with(context())
                .load(imageUrl)
                .error(R.drawable.loading_failed)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageIv);
            deleteImageIv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_collect_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_submit) {
            submitCollect();
            return true;
        } else if (id == R.id.action_qr) {
            navUtils.startActivityForResult(ZxingActivity.class, new ActivityResultAdapter(REQUEST_ZXING_URL) {
                @Override
                public void onRequestSuccess(Intent data) {
                    String url = ZxingActivity.parseResult(data);
                    updateCollectWithError(url);
                }
            });
            return true;
        }
        return false;
    }

    public void submitCollect() {
        final String url = urlEt.getText().toString();
        if (!checkUrl(url)) {
            showErrorUrl(url);
            return;
        }
        final String title = titleEt.getText().toString();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "标题不为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (collect == null) {
            collect = new Collect();
        }
        collect.setUrl(httpUrl);
        collect.setTitle(title);
        collect.setDescription(descEt.getText().toString());
        collect.setImage(imageUrl);
        collect.setTags(tagAdapter.getTagList());

        collect.setCreateDate(new Date());
        homePresenter.postCollect(collect, new RxRequestAdapter<Void>(context()) {
            @Override
            public void onRequestSuccess(Void data) {
                Toast.makeText(CollectEditorActivity.this, "新建收藏成功", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void showErrorUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "请先输入网址", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(CollectEditorActivity.this, url + "不是一个有效的网址", Toast.LENGTH_SHORT).show();
    }
}
