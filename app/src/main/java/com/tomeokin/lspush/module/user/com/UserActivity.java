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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.decay.di.ProvideComponent;
import com.decay.logger.Log;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.internal.CurrentUser;
import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.model.UserProfile;
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

public class UserActivity extends ToolbarActivity
    implements ProvideComponent<UserComponent>, UserActivityView.Callback {
    public static final int REQUEST_OPEN_COLLECT = 333;

    public static final String ARG_USER_ID = "arg.user.id";
    private final Log log = AppLogger.of("UserActivity");

    UserActivityView viewObj;
    User user;
    UserProfile userProfile;

    @Inject CurrentUser currentUser;
    @Inject Prefer prefer;
    @Inject UserPresenter userPresenter;

    @Override
    public UserComponent component() {
        return ((App) getApplication()).userComponent(this);
    }

    public static Bundle create(Prefer prefer, User user) {
        prefer.put(String.valueOf(user.getId()), user);
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_USER_ID, user.getId());
        return bundle;
    }

    public boolean parseIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        component().inject(this);
        if (!parseIntent()) {
            Toast.makeText(this, "缺少用户信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setToolbarTitle(R.string.user_page);
        setDisplayHomeAsUpEnabled(true);
        viewObj = new UserActivityView();
        setContentLayout(viewObj.getLayoutRes());
        viewObj.populateLayout(contentLayout, this);

        viewObj.setupUser(user);
        updateUser();
    }

    public void updateUser() {
        userPresenter.getUserProfile(user.getId(), new RxRequestAdapter<UserProfile>(context()) {
            @Override
            public void onRequestSuccess(UserProfile data) {
                super.onRequestSuccess(data);
                userProfile = data;
                user = data.getUser();
                viewObj.updateUserProfile(data);
            }
        });
    }

    @Override
    public void onFollowBtnClick() {
        if (isOwn()) return;

        if (userProfile.isHasFollow()) {
            userProfile.triggerUnFollow();
            viewObj.updateFollowProfile(userProfile);
            userPresenter.unfollowUser(user.getId(), new RxRequestAdapter<Void>(context()) {
                @Override
                public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                    super.onRequestFailed(null, "取消关注失败");
                    userProfile.triggerFollow();
                    viewObj.updateFollowProfile(userProfile);
                }
            });
        } else {
            userProfile.triggerFollow();
            viewObj.updateFollowProfile(userProfile);
            userPresenter.followUser(user.getId(), new RxRequestAdapter<Void>(context()) {
                @Override
                public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                    super.onRequestFailed(null, "添加关注失败");
                    userProfile.triggerUnFollow();
                    viewObj.updateFollowProfile(userProfile);
                }
            });
        }
    }

    @Override
    public void onShareLayoutClick() {
        navUtils.startActivity(UserCollectActivity.class, UserCollectActivity.create(prefer, user));
    }

    @Override
    public void onFavorLayoutClick() {
        navUtils.startActivity(UserCollectActivity.class, UserCollectActivity.create(prefer, user, true));
    }

    @Override
    public void onCollectClick(UserCollectAdapter adapter, final int pos, final Collect collect) {
        navUtils.startActivityForResult(CollectWebViewActivity.class,
            CollectWebViewActivity.create(prefer, collect, false), new ActivityResultAdapter(REQUEST_OPEN_COLLECT) {
                @Override
                public void onRequestSuccess(Intent data) {
                    Collect result = CollectWebViewActivity.resolveCollect(prefer, data);
                    if (result == null) {
                        log.w("the collect is not be null");
                        return;
                    }

                    List<Collect> collects = viewObj.updateCollectItem(pos, result);
                    userProfile.setHotShareCollect(collects);
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
        /* no-op */
    }

    @Override
    public boolean isOwn() {
        return currentUser.getCurrentUser().getId() == user.getId();
    }

    @Override
    public void onFollowingCountClick() {
        if (!isOwn()) return;

        navUtils.startActivity(UserFollowActivity.class, UserFollowActivity.create(prefer, user, false));
    }

    @Override
    public void onFollowerCountClick() {
        if (!isOwn()) return;

        navUtils.startActivity(UserFollowActivity.class, UserFollowActivity.create(prefer, user, true));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        ((App) getApplication()).releaseUserComponent();
        super.onDestroy();
    }
}
