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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.decay.glide.CircleTransform;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.internal.CurrentUser;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.framework.NavFragment;
import com.tomeokin.lspush.framework.prefer.Prefer;
import com.tomeokin.lspush.module.main.di.MainComponent;
import com.tomeokin.lspush.module.setting.com.SettingActivity;
import com.tomeokin.lspush.module.user.com.UserActivity;

import javax.inject.Inject;

public class MeFragment extends NavFragment {
    View userInfoLayout;
    ImageView userAvatarIv;
    TextView usernameTv;

    View settingTv;

    @Inject CurrentUser currentUser;
    @Inject Prefer prefer;

    User user;
    final View.OnClickListener onUserClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onUserClick();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_me, container, false);

        component(MainComponent.class).inject(this);

        user = currentUser.getCurrentUser();

        userInfoLayout = view.findViewById(R.id.user_info_layout);
        userAvatarIv = (ImageView) view.findViewById(R.id.user_avatar);
        usernameTv = (TextView) view.findViewById(R.id.username);

        settingTv = view.findViewById(R.id.setting_tv);
        settingTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSettingClick();
            }
        });

        setupUserView();
        return view;
    }

    public void setupUserView() {
        // @formatter:off
        Glide.with(context())
            .load(user.getAvatar())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
            .transform(new CircleTransform(context()))
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .into(userAvatarIv);
        // @formatter:on

        usernameTv.setText(user.getUsername());

        usernameTv.setOnClickListener(onUserClickListener);
        userAvatarIv.setOnClickListener(onUserClickListener);
        userInfoLayout.setOnClickListener(onUserClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            user = currentUser.getCurrentUser();
            setupUserView();
        }
    }

    public void onUserClick() {
        navUtils.startActivity(UserActivity.class, UserActivity.create(prefer, user));
    }

    public void onSettingClick() {
        navUtils.startActivity(SettingActivity.class);
    }
}
