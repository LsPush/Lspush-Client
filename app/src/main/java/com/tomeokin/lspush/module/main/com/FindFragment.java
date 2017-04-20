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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.tomeokin.lspush.framework.NavFragment;
import com.tomeokin.lspush.framework.nav.ActivityResultAdapter;
import com.tomeokin.lspush.framework.prefer.Prefer;
import com.tomeokin.lspush.module.main.HotAdapter;
import com.tomeokin.lspush.module.main.HotSection;
import com.tomeokin.lspush.module.main.RecentTopFooter;
import com.tomeokin.lspush.module.main.RecentTopSection;
import com.tomeokin.lspush.module.main.di.MainComponent;
import com.tomeokin.lspush.module.search.com.SearchActivity;
import com.tomeokin.lspush.module.user.UserCollectAdapter;
import com.tomeokin.lspush.module.user.com.UserActivity;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class FindFragment extends NavFragment implements UserCollectAdapter.OnCollectClickListener {
    private final Log log = AppLogger.of("FindFragment");

    public static final int REQUEST_OPEN_COLLECT = 189;

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView hotRv;
    HotAdapter hotAdapter;

    RecentTopSection recentTopSection;
    UserCollectAdapter recentTopColAdapter;
    RecentTopFooter recentTopFooter;
    UserCollectAdapter hotColAdapter;
    LoadMoreFooter loadMoreFooter;
    EndlessRecyclerOnScrollListener endlessScrollListener;
    PublishSubject<List<Collect>> collectSubject = PublishSubject.create();
    final int PAGE_COUNT = 20;

    @Inject HomePresenter homePresenter;
    @Inject Prefer prefer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        component(MainComponent.class).inject(this);

        final View view = inflater.inflate(R.layout.fragment_find, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshHotCollects(false);
            }
        });
        hotRv = (RecyclerView) view.findViewById(R.id.col_hot_rv);

        RecyclerView.ItemAnimator animator = hotRv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        hotRv.setLayoutManager(new LinearLayoutManager(context()));
        DividerDecoration decoration = new DividerDecoration(context(), DividerDecoration.VERTICAL);
        decoration.setEndOffset(1);
        hotRv.addItemDecoration(decoration);
        endlessScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                loadMoreFooter.setState(LoadMoreFooter.STATE_LOADING);
                notifyRvDataSetChanged();
                loadNextCollectList();
            }
        };
        hotRv.addOnScrollListener(endlessScrollListener);

        // recent top collects
        recentTopColAdapter = new UserCollectAdapter(true, this);
        SectionAdapter recentTopHeaderAdapter = new SectionAdapter(recentTopColAdapter);
        recentTopSection = new RecentTopSection(context(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDaysSelectorDialog();
            }
        });
        recentTopHeaderAdapter.addHeader(recentTopSection);
        recentTopFooter = new RecentTopFooter();
        recentTopFooter.setShowingNoMore(true);
        recentTopHeaderAdapter.addFooter(recentTopFooter);

        // hot collects
        hotColAdapter = new UserCollectAdapter(true, this);
        SectionAdapter hotColHeaderAdapter = new SectionAdapter(hotColAdapter);
        hotColHeaderAdapter.addHeader(new HotSection());
        loadMoreFooter = new LoadMoreFooter(context(), new LoadMoreFooter.OnLoadMoreClickListener() {
            @Override
            public void onLoadMoreClick(LoadMoreFooter footer, int state) {
                if (state == LoadMoreFooter.STATE_ERROR) {
                    footer.setState(LoadMoreFooter.STATE_LOADING);
                    notifyRvDataSetChanged();
                }
            }
        });
        hotColHeaderAdapter.addFooter(loadMoreFooter);

        // hot adapter
        hotAdapter = new HotAdapter(recentTopHeaderAdapter, hotColHeaderAdapter);

        //sectionAdapter = new SectionAdapter(hotAdapter);

        //sectionAdapter.addFooter(loadMoreFooter);

        hotRv.setAdapter(hotAdapter);

        subscribeCollectSubject();
        showCollects();

        return view;
    }

    public void showCollects() {
        List<Collect> collectList = hotColAdapter.getCollectList();
        if (collectList == null || collectList.size() == 0) {
            refreshHotCollects(true);
        }
    }

    public void showDaysSelectorDialog() {
        recentTopSection.showDialog(new RecentTopSection.OnDaySelectorDialogListener() {
            @Override
            public void onDaySelected(RecentTopSection section, DialogInterface dialog, int days) {
                log.i("onDaySelected %d", days);
                recentTopSection.setDays(days);
                hotAdapter.notifyDataSetChanged();
                refreshRecentTopList(true);
            }
        });
    }

    public void notifyRvDataSetChanged() {
        hotRv.post(new Runnable() {
            @Override
            public void run() {
                hotAdapter.notifyDataSetChanged();
            }
        });
    }

    public void refreshHotCollects(boolean showProgress) {
        refreshRecentTopList(false);
        refreshCollectList(showProgress);
    }

    public void refreshRecentTopList(boolean showProgress) {
        recentTopFooter.setShowingNoMore(false);
        homePresenter.findRecentTopHotCollect(recentTopSection.getDays(),
            new RxRequestAdapter<List<Collect>>(context(), showProgress) {
                @Override
                public void onRequestSuccess(List<Collect> data) {
                    super.onRequestSuccess(data);
                    recentTopColAdapter.setCollectList(data);
                    if (recentTopColAdapter.getCollectList().size() == 0) {
                        recentTopFooter.setShowingNoMore(true);
                    }
                    notifyRvDataSetChanged();
                }

                @Override
                public void onRequestComplete() {
                    super.onRequestComplete();
                }
            });
    }

    public void refreshCollectList(boolean showProgress) {
        endlessScrollListener.setEnabled(false);
        homePresenter.findHotCollects(0, PAGE_COUNT, new RxRequestAdapter<List<Collect>>(context(), showProgress) {
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
        int page = hotColAdapter.getItemCount() / PAGE_COUNT;
        homePresenter.findHotCollects(page, PAGE_COUNT, new RxRequestAdapter<List<Collect>>(context(), false) {
            @Override
            public void onRequestSuccess(List<Collect> data) {
                publishCollectList(false, data);
            }

            @Override
            public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                super.onRequestFailed(t, message);
                loadMoreFooter.setState(LoadMoreFooter.STATE_ERROR);
                notifyRvDataSetChanged();
            }
        });
    }

    public void publishCollectList(boolean update, List<Collect> collectList) {
        if (collectList.size() < PAGE_COUNT) {
            endlessScrollListener.setEnabled(false);
            loadMoreFooter.setState(LoadMoreFooter.STATE_NO_MORE);
            //notifyRvDataSetChanged();
            hotAdapter.notifyDataSetChanged();
        } else {
            endlessScrollListener.setEnabled(true);
            loadMoreFooter.setState(LoadMoreFooter.STATE_HIDE);
            hotAdapter.notifyDataSetChanged();
            //notifyRvDataSetChanged();
        }

        if (update) {
            log.i("CollectList-count %d", collectList.size());
        }

        if (update) {
            hotColAdapter.setCollectList(collectList);
        } else {
            collectSubject.onNext(collectList);
        }
    }

    private Func1<List<Collect>, List<Collect>> combineSortList() {
        return new Func1<List<Collect>, List<Collect>>() {
            @Override
            public List<Collect> call(List<Collect> collects) {
                final List<Collect> collectList = hotColAdapter.getCollectList();
                List<Collect> result = ListUtils.combineSortList(collectList, collects, Collect.FAVOR_UPDATE_COMPARATOR);
                if (result.size() == collectList.size()) {
                    endlessScrollListener.setEnabled(false);
                    loadMoreFooter.setState(LoadMoreFooter.STATE_NO_MORE);
                    hotAdapter.notifyDataSetChanged();
                }
                return result;
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
                    hotColAdapter.setCollectList(collects);
                    notifyRvDataSetChanged();
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
                    notifyRvDataSetChanged();
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
