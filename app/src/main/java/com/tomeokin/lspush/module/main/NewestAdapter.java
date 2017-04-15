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
package com.tomeokin.lspush.module.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.decay.glide.CircleTransform;
import com.decay.utillty.DateUtils;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class NewestAdapter extends RecyclerView.Adapter<NewestAdapter.NewestHolder>
    implements View.OnClickListener, TagAdapter.OnTagClickListener {
    private List<Collect> collectList = new ArrayList<>(0);
    private final OnItemClickListener onItemClickListener;

    public NewestAdapter(@NonNull OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public void setCollectList(List<Collect> collects) {
        if (collects == null) {
            collectList = new ArrayList<>();
        } else {
            collectList = collects;
        }
        notifyDataSetChanged();
    }

    public void updateCollectItem(int position, Collect collect) {
        collectList.set(position, collect);
        notifyDataSetChanged();
    }

    public List<Collect> getCollectList() {
        return collectList;
    }

    @Override
    public NewestHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_newest, parent, false);
        NewestHolder holder = new NewestHolder(view, this);
        holder.itemView.setOnClickListener(this);
        holder.userAvatar.setOnClickListener(this);
        holder.username.setOnClickListener(this);
        holder.favorBtn.setOnClickListener(this);
        holder.commentBtn.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(NewestHolder holder, int position) {
        holder.itemView.setTag(R.id.recycler_item_position, position);
        holder.userAvatar.setTag(R.id.recycler_item_position, position);
        holder.username.setTag(R.id.recycler_item_position, position);
        holder.favorBtn.setTag(R.id.recycler_item_position, position);
        holder.commentBtn.setTag(R.id.recycler_item_position, position);

        Collect collect = collectList.get(position);
        holder.bindItem(position, collect);
    }

    @Override
    public int getItemCount() {
        return collectList.size();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int position = (int) v.getTag(R.id.recycler_item_position);
        Collect collect = collectList.get(position);
        switch (id) {
            case R.id.user_avatar:
            case R.id.username: {
                User user = collect.getUser();
                onItemClickListener.onUserClick(position, collect, user);
                break;
            }
            case R.id.favor_btn: {
                onItemClickListener.onFavorClick(v, position, collect);
                break;
            }
            case R.id.comment_btn: {
                onItemClickListener.onCommentClick(position, collect);
                break;
            }
            case R.id.item_newest:
            default: {
                onItemClickListener.onCollectClick(position, collect);
                break;
            }
        }
    }

    @Override
    public void onTagClick(int group, int position, String tag) {
        Collect collect = collectList.get(group);
        onItemClickListener.onTagClick(group, collect, tag);
    }

    public interface OnItemClickListener {
        void onUserClick(int position, Collect collect, User user);

        void onCollectClick(int position, Collect collect);

        void onFavorClick(View view, int position, Collect collect);

        void onCommentClick(int position, Collect collect);

        void onTagClick(int position, Collect collect, String tag);
    }

    public static class NewestHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar;
        TextView username;
        TextView updateTime;
        TextView collectTitle;
        TextView collectDesc;
        ImageView collectImage;
        RecyclerView tagRv;
        ImageView favorBtn;
        TextView favorCountTv;
        ImageView commentBtn;
        TextView commentCountTv;
        TagAdapter tagAdapter;

        public NewestHolder(View itemView, @NonNull TagAdapter.OnTagClickListener listener) {
            super(itemView);
            userAvatar = (ImageView) itemView.findViewById(R.id.user_avatar);
            username = (TextView) itemView.findViewById(R.id.username);
            updateTime = (TextView) itemView.findViewById(R.id.update_time);
            collectTitle = (TextView) itemView.findViewById(R.id.collect_title);
            collectDesc = (TextView) itemView.findViewById(R.id.collect_desc);
            collectImage = (ImageView) itemView.findViewById(R.id.collect_image);
            tagRv = (RecyclerView) itemView.findViewById(R.id.tag_rv);
            favorBtn = (ImageView) itemView.findViewById(R.id.favor_btn);
            favorCountTv = (TextView) itemView.findViewById(R.id.favor_count_tv);
            commentBtn = (ImageView) itemView.findViewById(R.id.comment_btn);
            commentCountTv = (TextView) itemView.findViewById(R.id.comment_count_tv);

            tagAdapter = new TagAdapter(listener);
            tagRv.setLayoutManager(
                new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            tagRv.setAdapter(tagAdapter);
        }

        public void bindItem(int position, Collect collect) {
            final Context context = itemView.getContext();
            final int TEXT_COLOR_YES = ContextCompat.getColor(context, R.color.blue_4_whiteout);
            final int TEXT_COLOR_NO = ContextCompat.getColor(context, R.color.blue_gray_30);

            final User user = collect.getUser();
            // @formatter:off
            Glide.with(context)
                .load(user.getAvatar())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
                .transform(new CircleTransform(context))
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(userAvatar);
            // @formatter:on
            username.setText(user.getUsername());

            updateTime.setText(DateUtils.toFormatDateTime(collect.getUpdateDate()));
            collectTitle.setText(collect.getTitle());
            if (TextUtils.isEmpty(collect.getDescription())) {
                collectDesc.setText(null);
                collectDesc.setVisibility(View.GONE);
            } else {
                collectDesc.setVisibility(View.VISIBLE);
                collectDesc.setText(collect.getDescription());
            }

            if (collect.getImage() == null || collect.getImage().trim().length() == 0) {
                collectImage.setVisibility(View.GONE);
            } else {
                collectImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                    .load(collect.getImage())
                    .error(R.drawable.loading_failed)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(new GlideDrawableImageViewTarget(collectImage, 1));
            }

            tagAdapter.setGroup(position);
            if (collect.getTags() == null || collect.getTags().size() == 0) {
                tagRv.setVisibility(View.GONE);
                tagAdapter.setTagList(null);
            } else {
                tagRv.setVisibility(View.VISIBLE);
                tagAdapter.setTagList(collect.getTags());
            }

            if (collect.isHasFavor()) {
                favorCountTv.setTextColor(TEXT_COLOR_YES);
                favorBtn.setImageResource(R.drawable.favor_yes);
            } else {
                favorCountTv.setTextColor(TEXT_COLOR_NO);
                favorBtn.setImageResource(R.drawable.favor_no);
            }
            favorCountTv.setText(String.valueOf(collect.getFavorCount()));
            commentCountTv.setText(String.valueOf(collect.getCommentCount()));
        }
    }
}
