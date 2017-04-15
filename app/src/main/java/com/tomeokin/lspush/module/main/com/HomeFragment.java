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
import com.tomeokin.lspush.module.main.NewestAdapter;
import com.tomeokin.lspush.module.main.di.MainComponent;
import com.tomeokin.lspush.module.search.com.SearchActivity;
import com.tomeokin.lspush.module.user.com.UserActivity;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class HomeFragment extends NavFragment implements NewestAdapter.OnItemClickListener {
    public static final int REQUEST_OPEN_COLLECT = 101;
    private Log log = AppLogger.of("HomeFragment");

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView listRv;
    private SectionAdapter sectionAdapter;
    private LoadMoreFooter loadMoreFooter;
    private NewestAdapter newestAdapter;
    private EndlessRecyclerOnScrollListener endlessScrollListener;
    PublishSubject<List<Collect>> collectSubject = PublishSubject.create();
    private final int PAGE_COUNT = 20;

    @Inject HomePresenter homePresenter;
    @Inject Prefer prefer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {

        component(MainComponent.class).inject(this);

        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        //Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        //toolbar.inflateMenu(R.menu.menu_home);
        //toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        //    @Override
        //    public boolean onMenuItemClick(MenuItem item) {
        //        int id = item.getItemId();
        //        if (id == R.id.action_add_collect) {
        //            navUtils.startActivity(CollectEditorActivity.class, CollectEditorActivity.create(null));
        //        } else if (id == R.id.action_search) {
        //            Toast.makeText(context(), "search", Toast.LENGTH_SHORT).show();
        //        } else {
        //            return false;
        //        }
        //        return true;
        //    }
        //});

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCollectList();
            }
        });
        listRv = (RecyclerView) view.findViewById(R.id.newest_rv);
        RecyclerView.ItemAnimator animator = listRv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        listRv.setLayoutManager(new LinearLayoutManager(context()));
        DividerDecoration decoration = new DividerDecoration(context(), DividerDecoration.VERTICAL);
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
        newestAdapter = new NewestAdapter(this);
        sectionAdapter = new SectionAdapter(newestAdapter);
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

        return view;
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
        homePresenter.getNewestCollect(0, PAGE_COUNT, new RxRequestAdapter<List<Collect>>(context(), false) {
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
        int page = newestAdapter.getItemCount() / PAGE_COUNT;
        homePresenter.getNewestCollect(page, PAGE_COUNT, new RxRequestAdapter<List<Collect>>(context(), false) {
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
            newestAdapter.setCollectList(collectList);
        } else {
            collectSubject.onNext(collectList);
        }
    }

    private Func1<List<Collect>, List<Collect>> combineSortList() {
        return new Func1<List<Collect>, List<Collect>>() {
            @Override
            public List<Collect> call(List<Collect> collects) {
                final List<Collect> collectList = newestAdapter.getCollectList();
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
                    newestAdapter.setCollectList(collects);
            }});
        // @formatter:on
    }

    @Override
    public void onUserClick(int position, Collect collect, User user) {
        navUtils.startActivity(UserActivity.class, UserActivity.create(prefer, user));
    }

    @Override
    public void onCollectClick(final int position, Collect collect) {
        openCollect(position, collect, false);
    }

    @Override
    public void onCommentClick(int position, Collect collect) {
        openCollect(position, collect, true);
    }

    public void openCollect(final int position, Collect collect, boolean showComment) {
        navUtils.startActivityForResult(CollectWebViewActivity.class,
            CollectWebViewActivity.create(prefer, collect, showComment),
            new ActivityResultAdapter(REQUEST_OPEN_COLLECT) {
                @Override
                public void onRequestSuccess(Intent data) {
                    Collect result = CollectWebViewActivity.resolveCollect(prefer, data);
                    if (result == null) {
                        log.w("the collect is not be null");
                        return;
                    }

                    newestAdapter.updateCollectItem(position, result);
                    sectionAdapter.notifyAdapterItemChanged(position);
                }
            });
    }

    @Override
    public void onFavorClick(View view, final int position, final Collect collect) {
        collect.toggleFavor();
        sectionAdapter.notifyAdapterItemChanged(position);

        if (collect.isHasFavor()) {
            homePresenter.addFavor(collect.getId(), new RxRequestAdapter<Void>(context(), false) {
                @Override
                public void onRequestSuccess(Void data) {}

                @Override
                public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                    super.onRequestFailed(t, message);
                    collect.toggleFavor();
                    sectionAdapter.notifyAdapterItemChanged(position);
                }
            });
        } else {
            homePresenter.removeFavor(collect.getId(), new RxRequestAdapter<Void>(context(), false) {
                @Override
                public void onRequestSuccess(Void data) {}

                @Override
                public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                    super.onRequestFailed(t, message);
                    collect.toggleFavor();
                    sectionAdapter.notifyAdapterItemChanged(position);
                }
            });
        }
    }

    @Override
    public void onTagClick(int position, Collect collect, String tag) {
        navUtils.startActivity(SearchActivity.class,
            SearchActivity.create(LsPushService.SEARCH_COLLECT_OPTION_TAG, LsPushService.SEARCH_COLLECT_GROUP_ALL, tag,
                -1));
    }
}
