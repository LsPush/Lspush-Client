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
import android.widget.Toast;

import com.decay.di.ProvideComponent;
import com.decay.recyclerview.adapter.SectionAdapter;
import com.decay.recyclerview.decoration.DividerDecoration;
import com.decay.recyclerview.listener.EndlessRecyclerOnScrollListener;
import com.decay.recyclerview.section.LoadMoreFooter;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.internal.CurrentUser;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.remote.RxRequestAdapter;
import com.tomeokin.lspush.framework.ToolbarActivity;
import com.tomeokin.lspush.framework.prefer.Prefer;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.user.UserFollowAdapter;
import com.tomeokin.lspush.module.user.di.UserComponent;

import java.util.List;

import javax.inject.Inject;

public class UserFollowActivity extends ToolbarActivity
    implements ProvideComponent<UserComponent>, UserFollowAdapter.OnUserFollowListener {
    public static final String ARG_USER_ID = "arg.user.id";
    public static final String ARG_IS_FOLLOWER = "arg.is.follower";

    private long userId;
    private boolean isFollower;

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView listRv;
    SectionAdapter sectionAdapter;
    LoadMoreFooter loadMoreFooter;
    UserFollowAdapter userCollectAdapter;
    EndlessRecyclerOnScrollListener endlessScrollListener;
    private final int PAGE_COUNT = 20;

    @Inject CurrentUser currentUser;

    @Inject Prefer prefer;
    @Inject UserPresenter userPresenter;

    public static Bundle create(Prefer prefer, User user, boolean isFollower) {
        prefer.put(String.valueOf(user.getId()), user);
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_USER_ID, user.getId());
        bundle.putBoolean(ARG_IS_FOLLOWER, isFollower);
        return bundle;
    }

    @Override
    public UserComponent component() {
        return ((App) getApplication()).userComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getLongExtra(ARG_USER_ID, -1);
            isFollower = intent.getBooleanExtra(ARG_IS_FOLLOWER, false);
        }
        if (userId <= 0) {
            Toast.makeText(this, "缺少用户信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        component().inject(this);

        User user = prefer.get(String.valueOf(userId), User.class);
        String title = isFollower ? "关注%s的人" : "%s的关注";
        if (userId == currentUser.getCurrentUser().getId()) {
            setToolbarTitle(String.format(title, "我"));
        } else {
            setToolbarTitle(String.format(title, user.getUsername()));
        }
        setDisplayHomeAsUpEnabled(true);

        setContentLayout(R.layout.activity_user_follow);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCollectList();
            }
        });
        listRv = (RecyclerView) findViewById(R.id.user_follow_rv);
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
        userCollectAdapter = new UserFollowAdapter(context(), this);
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
        userPresenter.getUserFollowers(userId, isFollower, 0, PAGE_COUNT,
            new RxRequestAdapter<List<User>>(context(), false) {
                @Override
                public void onRequestSuccess(List<User> data) {
                    publishUserList(true, data);
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
        userPresenter.getUserFollowers(userId, isFollower, page, PAGE_COUNT,
            new RxRequestAdapter<List<User>>(context(), false) {
                @Override
                public void onRequestSuccess(List<User> data) {
                    publishUserList(false, data);
                }

                @Override
                public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                    super.onRequestFailed(t, message);
                    loadMoreFooter.setState(LoadMoreFooter.STATE_ERROR);
                    sectionAdapter.notifyDataSetChanged();
                }
            });
    }

    public void publishUserList(boolean update, List<User> userList) {
        if (userList.size() < PAGE_COUNT) {
            endlessScrollListener.setEnabled(false);
            loadMoreFooter.setState(LoadMoreFooter.STATE_NO_MORE);
            sectionAdapter.notifyDataSetChanged();
        } else {
            endlessScrollListener.setEnabled(true);
            loadMoreFooter.setState(LoadMoreFooter.STATE_HIDE);
            sectionAdapter.notifyDataSetChanged();
        }

        if (update) {
            userCollectAdapter.setUserList(userList);
        } else {
            List<User> oldUserList = userCollectAdapter.getUserList();
            oldUserList.addAll(userList);
            userCollectAdapter.setUserList(oldUserList);
        }
    }

    @Override
    public void onUserItemClick(UserFollowAdapter adapter, int pos, User user) {
        navUtils.startActivity(UserActivity.class, UserActivity.create(prefer, user));
    }

    @Override
    public void onFollowBtnClick(final UserFollowAdapter adapter, final int pos, final User user) {
        if (user.isHasFollow()) {
            user.triggerUnFollow();
            adapter.updateUserItem(pos, user);
            sectionAdapter.notifyDataSetChanged();
            userPresenter.unfollowUser(user.getId(), new RxRequestAdapter<Void>(context()) {
                @Override
                public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                    super.onRequestFailed(null, "取消关注失败");
                    user.triggerFollow();
                    adapter.updateUserItem(pos, user);
                    sectionAdapter.notifyDataSetChanged();
                }
            });
        } else {
            user.triggerFollow();
            adapter.updateUserItem(pos, user);
            sectionAdapter.notifyDataSetChanged();
            userPresenter.followUser(user.getId(), new RxRequestAdapter<Void>(context()) {
                @Override
                public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                    super.onRequestFailed(null, "添加关注失败");
                    user.triggerUnFollow();
                    adapter.updateUserItem(pos, user);
                    sectionAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
