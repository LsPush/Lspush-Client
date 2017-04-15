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
package com.tomeokin.lspush.module.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.decay.glide.CircleTransform;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserFollowAdapter extends RecyclerView.Adapter<UserFollowAdapter.FollowHolder> implements View.OnClickListener {
    private List<User> userList = new ArrayList<>(0);
    private final OnUserFollowListener onUserFollowListener;
    private final Context context;

    public UserFollowAdapter(@NonNull Context context, @NonNull OnUserFollowListener onUserFollowListener) {
        this.onUserFollowListener = onUserFollowListener;
        this.context = context;
    }

    public void setUserList(List<User> userList) {
        if (userList == null) {
            this.userList.clear();
        } else {
            this.userList = userList;
        }
        notifyDataSetChanged();
    }

    public List<User> getUserList() {
        return userList;
    }

    public List<User> updateUserItem(int position, User user) {
        userList.set(position, user);
        notifyDataSetChanged();
        return userList;
    }

    @Override
    public FollowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_follow, parent, false);
        final FollowHolder holder = new FollowHolder(view);
        holder.followTv.setOnClickListener(this);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(FollowHolder holder, int position) {
        holder.itemView.setTag(R.id.recycler_item_position, position);
        holder.followTv.setTag(R.id.recycler_item_position, position);

        User user = userList.get(position);
        holder.usernameTv.setText(user.getUsername());

        // @formatter:off
        Glide.with(context)
            .load(user.getAvatar())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
            .transform(new CircleTransform(context))
            .into(holder.userAvatarIv);
        // @formatter:on

        updateFollow(holder.followTv, user);
    }

    private void updateFollow(TextView followTv, User user) {
        if (user.isHasFollow()) {
            setFollowing(followTv);
        } else {
            setUnfollowing(followTv);
        }
    }

    private void setFollowing(TextView followTv) {
        followTv.setText(R.string.had_followed);
        followTv.setTextColor(ContextCompat.getColor(context, R.color.blue_gray_30));
    }

    private void setUnfollowing(TextView followTv) {
        followTv.setText(R.string.follow_user);
        followTv.setTextColor(ContextCompat.getColor(context, R.color.blue_5_whiteout));
    }

    @Override
    public void onClick(View v) {
        int pos = (int) v.getTag(R.id.recycler_item_position);
        if (pos < 0 || pos >= userList.size()) return;
        User user = userList.get(pos);

        int id = v.getId();
        if (id == R.id.follow_tv) {
            onUserFollowListener.onFollowBtnClick(this, pos, user);
        } else {
            onUserFollowListener.onUserItemClick(this, pos, user);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public interface OnUserFollowListener {
        void onUserItemClick(UserFollowAdapter adapter, int pos, User user);

        void onFollowBtnClick(UserFollowAdapter adapter, int pos, User user);
    }

    public class FollowHolder extends RecyclerView.ViewHolder {
        ImageView userAvatarIv;
        TextView usernameTv;
        TextView followTv;

        public FollowHolder(View itemView) {
            super(itemView);
            userAvatarIv = (ImageView) itemView.findViewById(R.id.user_avatar_iv);
            usernameTv = (TextView) itemView.findViewById(R.id.username_tv);
            followTv = (TextView) itemView.findViewById(R.id.follow_tv);
        }
    }
}
