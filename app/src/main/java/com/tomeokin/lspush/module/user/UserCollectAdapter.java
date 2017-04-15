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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.decay.glide.CircleTransform;
import com.decay.utillty.DateUtils;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.module.main.TagAdapter;

import java.util.ArrayList;
import java.util.List;

public class UserCollectAdapter extends RecyclerView.Adapter<UserCollectAdapter.UserCollectHolder>
    implements View.OnClickListener, TagAdapter.OnTagClickListener {
    private List<Collect> collectList = new ArrayList<>(0);
    OnCollectClickListener onCollectClickListener;
    private boolean showUser;

    public UserCollectAdapter(@NonNull OnCollectClickListener listener) {
        this(false, listener);
    }

    public UserCollectAdapter(boolean showUser, @NonNull OnCollectClickListener listener) {
        onCollectClickListener = listener;
        this.showUser = showUser;
    }

    public void setCollectList(List<Collect> collectList) {
        if (collectList == null) {
            this.collectList.clear();
        } else {
            this.collectList = collectList;
        }
        notifyDataSetChanged();
    }

    public List<Collect> getCollectList() {
        return collectList;
    }

    public List<Collect> updateCollectItem(int position, Collect collect) {
        collectList.set(position, collect);
        notifyDataSetChanged();
        return collectList;
    }

    @Override
    public UserCollectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.layout_user_collect, parent, false);
        final UserCollectHolder holder = new UserCollectHolder(view, this);
        holder.itemView.setOnClickListener(this);
        holder.usernameTv.setOnClickListener(this);
        holder.userAvatarIv.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(UserCollectHolder holder, int position) {
        holder.itemView.setTag(R.id.recycler_item_position, position);
        holder.usernameTv.setTag(R.id.recycler_item_position, position);
        holder.userAvatarIv.setTag(R.id.recycler_item_position, position);

        final Context context = holder.itemView.getContext();
        Collect collect = collectList.get(position);

        if (showUser) {
            final User user = collect.getUser();
            holder.userLayout.setVisibility(View.VISIBLE);
            // @formatter:off
            Glide.with(context)
                .load(user.getAvatar())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
                .transform(new CircleTransform(context))
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(holder.userAvatarIv);
            // @formatter:on
            holder.usernameTv.setText(user.getUsername());
        } else {
            holder.userLayout.setVisibility(View.GONE);
        }

        holder.colTitleTv.setText(collect.getTitle());

        if (TextUtils.isEmpty(collect.getDescription())) {
            holder.colDescTv.setText(null);
            holder.colDescTv.setVisibility(View.GONE);
        } else {
            holder.colDescTv.setVisibility(View.VISIBLE);
            holder.colDescTv.setText(collect.getDescription());
        }

        if (collect.getImage() == null || collect.getImage().trim().length() == 0) {
            holder.colDescImgIv.setVisibility(View.GONE);
        } else {
            holder.colDescImgIv.setVisibility(View.VISIBLE);
            Glide.with(context)
                .load(collect.getImage())
                .error(R.drawable.loading_failed)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.colDescImgIv);
        }

        holder.updateTimeTv.setText(DateUtils.toFormatDateTime(collect.getUpdateDate()));

        holder.tagAdapter.setGroup(position);
        if (collect.getTags() == null || collect.getTags().size() == 0) {
            holder.tagRv.setVisibility(View.GONE);
            holder.tagAdapter.setTagList(null);
        } else {
            holder.tagRv.setVisibility(View.VISIBLE);
            holder.tagAdapter.setTagList(collect.getTags());
        }

        holder.favorCountTv.setText(context.getString(R.string.collect_count_desc, collect.getFavorCount()));
        holder.commentCountTv.setText(context.getString(R.string.comment_count_desc, collect.getCommentCount()));
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        final int pos = (int) v.getTag(R.id.recycler_item_position);
        Collect collect = collectList.get(pos);

        switch (id) {
            case R.id.username:
            case R.id.user_avatar:
                User user = collect.getUser();
                onCollectClickListener.onUserClick(user);
                break;
            case R.id.user_collect_item:
            default:
                onCollectClickListener.onCollectClick(this, pos, collect);
                break;
        }
    }

    @Override
    public void onTagClick(int group, int position, String tag) {
        onCollectClickListener.onTagClick(tag);
    }

    @Override
    public int getItemCount() {
        return collectList.size();
    }

    public class UserCollectHolder extends RecyclerView.ViewHolder {
        ImageView colDescImgIv;
        TextView colTitleTv;
        TextView colDescTv;
        RecyclerView tagRv;
        TextView updateTimeTv;
        TextView favorCountTv;
        TextView commentCountTv;
        TagAdapter tagAdapter;

        View userLayout;
        ImageView userAvatarIv;
        TextView usernameTv;

        public UserCollectHolder(View itemView, TagAdapter.OnTagClickListener listener) {
            super(itemView);
            colDescImgIv = (ImageView) itemView.findViewById(R.id.col_descImg);
            colTitleTv = (TextView) itemView.findViewById(R.id.col_title);
            colDescTv = (TextView) itemView.findViewById(R.id.col_desc);
            tagRv = (RecyclerView) itemView.findViewById(R.id.tag_rv);
            updateTimeTv = (TextView) itemView.findViewById(R.id.update_time);
            favorCountTv = (TextView) itemView.findViewById(R.id.favor_count_tv);
            commentCountTv = (TextView) itemView.findViewById(R.id.comment_count_tv);

            usernameTv = (TextView) itemView.findViewById(R.id.username);
            userAvatarIv = (ImageView) itemView.findViewById(R.id.user_avatar);
            userLayout = itemView.findViewById(R.id.user_layout);

            tagAdapter = new TagAdapter(listener);
            tagRv.setLayoutManager(
                new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            tagRv.setAdapter(tagAdapter);
        }
    }

    public interface OnCollectClickListener {
        void onCollectClick(UserCollectAdapter adapter, int pos, Collect collect);

        void onTagClick(String tag);

        void onUserClick(User user);
    }
}
