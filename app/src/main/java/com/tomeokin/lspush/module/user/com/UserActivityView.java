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

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.decay.glide.CircleTransform;
import com.decay.recyclerview.decoration.DividerDecoration;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.model.UserProfile;
import com.tomeokin.lspush.framework.ViewCallback;
import com.tomeokin.lspush.framework.ViewContent;
import com.tomeokin.lspush.module.user.UserCollectAdapter;

import java.util.List;

public class UserActivityView extends ViewContent<UserActivityView.Callback> {
    private ImageView userAvatarIv;
    private TextView usernameTv;
    private TextView userDescTv;
    private TextView followTv;

    private View followingCountLayout;
    private TextView followingCountTv;
    private View followersCountLayout;
    private TextView followersCountTv;

    private View shareLayout;
    private TextView shareCountTv;
    private View favorLayout;
    private TextView favorCountTv;

    private RecyclerView listRv;
    private UserCollectAdapter adapter;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_user;
    }

    @Override
    public void populateLayout(@NonNull View view, final Callback callback) {
        super.populateLayout(view, callback);

        userAvatarIv = (ImageView) view.findViewById(R.id.user_avatar);
        usernameTv = (TextView) view.findViewById(R.id.username);
        userDescTv = (TextView) view.findViewById(R.id.user_desc);
        followTv = (TextView) view.findViewById(R.id.follow_tv);

        followingCountLayout = view.findViewById(R.id.following_count_layout);
        followingCountTv = (TextView) view.findViewById(R.id.following_count_tv);
        followersCountLayout = view.findViewById(R.id.followers_count_layout);
        followersCountTv = (TextView) view.findViewById(R.id.followers_count_tv);

        followingCountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onFollowingCountClick();
            }
        });
        followingCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followingCountTv.callOnClick();
            }
        });
        followersCountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onFollowerCountClick();
            }
        });
        followersCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followersCountTv.callOnClick();
            }
        });

        shareLayout = view.findViewById(R.id.share_layout);
        shareCountTv = (TextView) view.findViewById(R.id.share_count_tv);
        favorLayout = view.findViewById(R.id.favor_layout);
        favorCountTv = (TextView) view.findViewById(R.id.favor_count_tv);

        listRv = (RecyclerView) view.findViewById(R.id.hot_share_rv);

        followTv.setVisibility(View.GONE);
        followTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onFollowBtnClick();
            }
        });

        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onShareLayoutClick();
            }
        });
        favorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onFavorLayoutClick();
            }
        });

        listRv.setLayoutManager(new LinearLayoutManager(context));
        RecyclerView.ItemAnimator animator = listRv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        DividerDecoration decoration = new DividerDecoration(context, DividerDecoration.VERTICAL);
        decoration.setEndOffset(1);
        listRv.addItemDecoration(decoration);
        adapter = new UserCollectAdapter(callback);
        listRv.setAdapter(adapter);
    }

    public void setupUser(User user) {
        if (user != null) {
            setupUser(user.getUsername(), user.getAvatar(), user.getDescription());
        } else {
            setDefaultAvatar();
            usernameTv.setText(null);
            userDescTv.setText(R.string.no_description);
        }
    }

    public List<Collect> updateCollectItem(int position, Collect collect) {
        return adapter.updateCollectItem(position, collect);
    }

    private void setupUser(String username, String userAvatar, String userDesc) {
        usernameTv.setText(username);

        // @formatter:off
        Glide.with(context)
            .load(userAvatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
            .transform(new CircleTransform(context))
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .into(userAvatarIv);
        // @formatter:on

        if (TextUtils.isEmpty(userDesc)) {
            userDescTv.setText(R.string.no_description);
        } else {
            userDescTv.setText(userDesc);
        }
    }

    private void setDefaultAvatar() {
        // @formatter:off
        Glide.with(context)
            .load(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
            .transform(new CircleTransform(context))
            .into(userAvatarIv);
        // @formatter:on
    }

    public void updateUserProfile(UserProfile profile) {
        final User user = profile.getUser();
        setupUser(user);

        if (viewCallback.isOwn()) {
            followTv.setVisibility(View.GONE);
        } else {
            followTv.setVisibility(View.VISIBLE);
            updateFollowProfile(profile);
        }

        followingCountTv.setText(String.valueOf(profile.getFollowingCount()));
        followersCountTv.setText(String.valueOf(profile.getFollowersCount()));

        shareCountTv.setText(String.valueOf(profile.getShareCount()));
        favorCountTv.setText(String.valueOf(profile.getFavorCount()));

        adapter.setCollectList(profile.getHotShareCollect());
    }

    public void updateFollowProfile(UserProfile profile) {
        if (profile.isHasFollow()) {
            setFollowing();
        } else {
            setUnfollowing();
        }
    }

    public void setFollowing() {
        followTv.setText(R.string.had_followed);
        followTv.setTextColor(ContextCompat.getColor(context, R.color.blue_gray_30));
    }

    public void setUnfollowing() {
        followTv.setText(R.string.follow_user);
        followTv.setTextColor(ContextCompat.getColor(context, R.color.blue_5_whiteout));
    }

    public interface Callback extends ViewCallback, UserCollectAdapter.OnCollectClickListener {
        void onFollowBtnClick();

        void onShareLayoutClick();

        void onFavorLayoutClick();

        boolean isOwn();

        void onFollowingCountClick();

        void onFollowerCountClick();
    }
}
