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
package com.tomeokin.lspush.module.user.com;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.decay.di.ProvideComponent;
import com.decay.glide.CircleTransform;
import com.decay.logger.Log;
import com.decay.recyclerview.adapter.SectionAdapter;
import com.decay.recyclerview.decoration.DividerDecoration;
import com.decay.recyclerview.listener.EndlessRecyclerOnScrollListener;
import com.decay.recyclerview.section.LoadMoreFooter;
import com.decay.utillty.ListUtils;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.remote.LsPushService;
import com.tomeokin.lspush.data.remote.RxRequestAdapter;
import com.tomeokin.lspush.framework.ToolbarActivity;
import com.tomeokin.lspush.framework.nav.ActivityResultAdapter;
import com.tomeokin.lspush.framework.prefer.Prefer;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.main.com.CollectWebViewActivity;
import com.tomeokin.lspush.module.search.com.SearchActivity;
import com.tomeokin.lspush.module.user.UserCollectAdapter;
import com.tomeokin.lspush.module.user.di.UserComponent;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class UserCollectActivity extends ToolbarActivity
    implements ProvideComponent<UserComponent>, UserCollectAdapter.OnCollectClickListener {
    public static final String ARG_USER_ID = "arg.user.id";
    public static final String ARG_IS_USER_SHARE = "arg.is.user.share";
    private final Log log = AppLogger.of("UserCollectActivity");
    public static final int REQUEST_OPEN_COLLECT = 432;

    User user;
    boolean isUserFavor;
    @Inject Prefer prefer;
    @Inject UserPresenter userPresenter;

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView listRv;
    SectionAdapter sectionAdapter;
    LoadMoreFooter loadMoreFooter;
    UserCollectAdapter userCollectAdapter;
    EndlessRecyclerOnScrollListener endlessScrollListener;
    PublishSubject<List<Collect>> collectSubject = PublishSubject.create();
    private final int PAGE_COUNT = 20;

    public boolean parseIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
            isUserFavor = intent.getBooleanExtra(ARG_IS_USER_SHARE, false);
            long userId = intent.getLongExtra(ARG_USER_ID, -1);
            if (userId != -1) {
                user = prefer.get(String.valueOf(userId), User.class);
            }
            if (user != null) {
                return true;
            }
        }
        return false;
    }

    public static Bundle create(Prefer prefer, User user) {
        return create(prefer, user, false);
    }

    public static Bundle create(Prefer prefer, User user, boolean isUserShare) {
        prefer.put(String.valueOf(user.getId()), user);
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_USER_ID, user.getId());
        bundle.putBoolean(ARG_IS_USER_SHARE, isUserShare);
        return bundle;
    }

    @Override
    public UserComponent component() {
        return ((App) getApplication()).userComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        component().inject(this);

        if (!parseIntent()) {
            Toast.makeText(this, "缺少用户信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setDisplayHomeAsUpEnabled(true);
        setToolbarTitleGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        if (isUserFavor) {
            setToolbarTitle(user.getUsername() + "的点赞");
        } else {
            setToolbarTitle(user.getUsername() + "的分享");
        }
        toolbarLogo.setVisibility(View.VISIBLE);
        // @formatter:off
        Glide.with(this)
            .load(user.getAvatar())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
            .transform(new CircleTransform(this))
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .into(toolbarLogo);
        // @formatter:on

        setContentLayout(R.layout.activity_user_collect);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCollectList();
            }
        });
        listRv = (RecyclerView) findViewById(R.id.col_share_rv);
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
        userCollectAdapter = new UserCollectAdapter(isUserFavor, this);
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

        subscribeCollectSubject();
        refreshCollectList();
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
        endlessScrollListener.setEnabled(false);
        userPresenter.getUserCollects(isUserFavor, user.getId(), 0, PAGE_COUNT,
            new RxRequestAdapter<List<Collect>>(context(), false) {
                @Override
                public void onRequestSuccess(List<Collect> data) {
                    publishCollectList(true, data);
                }

                @Override
                public void onRequestComplete() {
                    super.onRequestComplete();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
    }

    public void loadNextCollectList() {
        int page = userCollectAdapter.getItemCount() / PAGE_COUNT;
        userPresenter.getUserCollects(isUserFavor, user.getId(), page, PAGE_COUNT,
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
    public void onCollectClick(final UserCollectAdapter adapter, final int pos, Collect collect) {
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

    @Override
    public void onUserClick(User user) {
        navUtils.startActivity(UserActivity.class, UserActivity.create(prefer, user));
    }
}
