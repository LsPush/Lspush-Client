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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.decay.di.ProvideComponent;
import com.decay.logger.Log;
import com.decay.recyclerview.adapter.SectionAdapter;
import com.decay.recyclerview.decoration.DividerDecoration;
import com.decay.recyclerview.listener.EndlessRecyclerOnScrollListener;
import com.decay.recyclerview.section.LoadMoreFooter;
import com.decay.utillty.ListUtils;
import com.jaeger.library.StatusBarUtil;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.remote.LsPushService;
import com.tomeokin.lspush.data.remote.RxRequestAdapter;
import com.tomeokin.lspush.framework.NavActivity;
import com.tomeokin.lspush.framework.nav.ActivityResultAdapter;
import com.tomeokin.lspush.framework.prefer.Prefer;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.main.com.CollectWebViewActivity;
import com.tomeokin.lspush.module.search.SearchCollectAdapter;
import com.tomeokin.lspush.module.search.di.SearchComponent;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class SearchActivity extends NavActivity
    implements ProvideComponent<SearchComponent>, SearchOptionDialog.OnSearchCollectListener, SearchCollectAdapter.OnCollectClickListener {

    private Log log = AppLogger.of("SearchActivity");
    public static final String ARG_SEARCH_OPTION = "arg.search.option";
    public static final String ARG_SEARCH_GROUP = "arg.search.group";
    public static final String ARG_SEARCH_KEYWORD = "arg.search.keyword";
    public static final String ARG_SEARCH_TARGET_USER = "arg.search.targetUser";
    private static final int REQUEST_OPEN_COLLECT = 339;

    private String option;
    private String group;
    private String keyword;
    private long targetUserId;

    Toolbar toolbar;
    ActionBar actionBar;
    TextView searchTv;
    SearchOptionDialog searchOptionDialog;
    FrameLayout contentLayout;

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView listRv;
    SectionAdapter sectionAdapter;
    LoadMoreFooter loadMoreFooter;
    SearchCollectAdapter userCollectAdapter;
    EndlessRecyclerOnScrollListener endlessScrollListener;
    PublishSubject<List<Collect>> collectSubject = PublishSubject.create();
    private final int PAGE_COUNT = 20;

    public static Bundle create(String option, String group, String keyword, long targetUserId) {
        Bundle bundle = new Bundle();
        addArgString(bundle, ARG_SEARCH_OPTION, option);
        addArgString(bundle, ARG_SEARCH_GROUP, group);
        addArgString(bundle, ARG_SEARCH_KEYWORD, keyword);
        bundle.putLong(ARG_SEARCH_TARGET_USER, targetUserId);
        return bundle;
    }

    private static void addArgString(Bundle bundle, String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            bundle.putString(key, value);
        }
    }

    @Inject SearchPresenter searchPresenter;
    @Inject Prefer prefer;

    @Override
    public SearchComponent component() {
        return ((App) getApplication()).searchComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        component().inject(this);

        initToolbar();
        setDisplayHomeAsUpEnabled(true);
        contentLayout = (FrameLayout) findViewById(R.id.content_container);
        setContentLayout(R.layout.activity_search_content);

        if (getIntent() != null) {
            final Intent intent = getIntent();
            option = intent.getStringExtra(ARG_SEARCH_OPTION);
            group = intent.getStringExtra(ARG_SEARCH_GROUP);
            keyword = intent.getStringExtra(ARG_SEARCH_KEYWORD);
            targetUserId = intent.getLongExtra(ARG_SEARCH_TARGET_USER, -1);
        }
        if (TextUtils.isEmpty(option)) option = LsPushService.SEARCH_COLLECT_OPTION_TITLE;
        if (TextUtils.isEmpty(group)) group = LsPushService.SEARCH_COLLECT_GROUP_ALL;
        if (targetUserId <= 0) targetUserId = -1;

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCollectList();
            }
        });
        listRv = (RecyclerView) findViewById(R.id.col_result_rv);
        RecyclerView.ItemAnimator animator = listRv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        listRv.setLayoutManager(new LinearLayoutManager(context()));
        DividerDecoration decoration = new DividerDecoration(this, DividerDecoration.VERTICAL);
        decoration.setEndOffset(1);
        listRv.addItemDecoration(decoration);
        endlessScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                loadMoreFooter.setState(LoadMoreFooter.STATE_LOADING);
                notifyRvDataSetChanged();
                loadNextCollectList();
            }
        };
        listRv.addOnScrollListener(endlessScrollListener);
        userCollectAdapter = new SearchCollectAdapter(this);
        sectionAdapter = new SectionAdapter(userCollectAdapter);
        loadMoreFooter = new LoadMoreFooter(context(), new LoadMoreFooter.OnLoadMoreClickListener() {
            @Override
            public void onLoadMoreClick(LoadMoreFooter footer, int state) {
                if (state == LoadMoreFooter.STATE_ERROR) {
                    footer.setState(LoadMoreFooter.STATE_LOADING);
                    sectionAdapter.notifyDataSetChanged();
                }
            }
        });
        sectionAdapter.addFooter(loadMoreFooter);
        listRv.setAdapter(sectionAdapter);
        swipeRefreshLayout.setVisibility(View.GONE);

        subscribeCollectSubject();

        searchTv = (TextView) findViewById(R.id.search_editText);
        searchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchDialog();
            }
        });
        if (!TextUtils.isEmpty(keyword)) {
            searchTv.setText(keyword);
            refreshCollectList();
        }
    }

    public void notifyRvDataSetChanged() {
        listRv.post(new Runnable() {
            @Override
            public void run() {
                sectionAdapter.notifyDataSetChanged();
            }
        });
    }

    public void refreshCollectList() {
        if (TextUtils.isEmpty(keyword)) {
            return;
        }

        endlessScrollListener.setEnabled(false);
        searchPresenter.searchCollect(targetUserId, option, group, keyword, 0, PAGE_COUNT,
            new RxRequestAdapter<List<Collect>>(context(), false) {
                @Override
                public void onRequestSuccess(List<Collect> data) {
                    publishCollectList(true, data);
                }

                @Override
                public void onRequestComplete() {
                    super.onRequestComplete();
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
    }

    public void loadNextCollectList() {
        if (TextUtils.isEmpty(keyword)) {
            return;
        }

        int page = userCollectAdapter.getItemCount() / PAGE_COUNT;
        searchPresenter.searchCollect(targetUserId, option, group, keyword, page, PAGE_COUNT,
            new RxRequestAdapter<List<Collect>>(context(), false) {
                @Override
                public void onRequestSuccess(List<Collect> data) {
                    publishCollectList(false, data);
                }

                @Override
                public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                    super.onRequestFailed(t, message);
                    loadMoreFooter.setState(LoadMoreFooter.STATE_ERROR);
                    sectionAdapter.notifyDataSetChanged();
                }
            });
    }

    public void publishCollectList(boolean update, List<Collect> collectList) {
        if (collectList.size() < PAGE_COUNT) {
            endlessScrollListener.setEnabled(false);
            loadMoreFooter.setState(LoadMoreFooter.STATE_NO_MORE);
            sectionAdapter.notifyDataSetChanged();
        } else {
            endlessScrollListener.setEnabled(true);
            loadMoreFooter.setState(LoadMoreFooter.STATE_HIDE);
            sectionAdapter.notifyDataSetChanged();
        }

        if (update) {
            userCollectAdapter.setCollectList(collectList);
        } else {
            collectSubject.onNext(collectList);
        }
    }

    private Func1<List<Collect>, List<Collect>> combineSortList() {
        return new Func1<List<Collect>, List<Collect>>() {
            @Override
            public List<Collect> call(List<Collect> collects) {
                final List<Collect> collectList = userCollectAdapter.getCollectList();
                return ListUtils.combineSortList(collectList, collects, Collect.UPDATE_COMPARATOR);
            }
        };
    }

    public void subscribeCollectSubject() {
        // @formatter:off
        collectSubject
            .subscribeOn(Schedulers.computation())
            .map(combineSortList())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<Collect>>() {
                @Override
                public void call(List<Collect> collects) {
                    userCollectAdapter.setCollectList(collects);
            }});
        // @formatter:on
    }

    @Override
    public void onCollectClick(final SearchCollectAdapter adapter, final int pos, Collect collect) {
        navUtils.startActivityForResult(CollectWebViewActivity.class,
            CollectWebViewActivity.create(prefer, collect, false), new ActivityResultAdapter(REQUEST_OPEN_COLLECT) {
                @Override
                public void onRequestSuccess(Intent data) {
                    Collect result = CollectWebViewActivity.resolveCollect(prefer, data);
                    if (result == null) {
                        log.w("the collect is not be null");
                        return;
                    }

                    adapter.updateCollectItem(pos, result);
                    sectionAdapter.notifyDataSetChanged();
                }
            });
    }

    @Override
    public void onTagClick(String tag) {
        navUtils.startActivity(SearchActivity.class,
            SearchActivity.create(LsPushService.SEARCH_COLLECT_OPTION_TAG, LsPushService.SEARCH_COLLECT_GROUP_ALL, tag,
                -1));
    }

    public void showSearchDialog() {
        if (searchOptionDialog == null) {
            searchOptionDialog = new SearchOptionDialog(this, this);
        }
        if (searchOptionDialog.isShowing()) {
            return;
        }
        searchOptionDialog.show(keyword);
    }

    @Override
    public void searchCollect(String option, String group, @NonNull String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return;
        }

        if (TextUtils.isEmpty(this.keyword)) {
            if (!keyword.equals(this.keyword)) {
                this.keyword = keyword;
                searchTv.setText(keyword);
                refreshCollectList();
            }
        } else if (!keyword.equals(this.keyword)) {
            Bundle args = create(option, group, keyword, -1);
            navUtils.startActivity(SearchActivity.class, args);
            overridePendingTransition(R.anim.hold, android.R.anim.fade_in);
        }
    }

    public void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.statusBarColor), 30);
        }

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
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

    public void setContentLayout(@LayoutRes int layoutResID) {
        clearContentLayout();
        LayoutInflater inflater = getLayoutInflater();
        inflater.inflate(layoutResID, contentLayout, true);
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

    @Override
    protected void onDestroy() {
        ((App) getApplication()).releaseSearchComponent();
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_right_out);
    }
}
