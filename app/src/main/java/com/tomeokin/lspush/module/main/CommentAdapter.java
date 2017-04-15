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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.decay.glide.CircleTransform;
import com.decay.utillty.DateSpanUtils;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.model.Comment;
import com.tomeokin.lspush.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> implements View.OnClickListener {
    private List<Comment> commentList = new ArrayList<>(0);
    private static DateSpanUtils dateSpanUtils = new DateSpanUtils();
    private final OnItemClickListener onItemClickListener;

    public CommentAdapter(@NonNull OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setCommentList(List<Comment> comments) {
        if (commentList == null) {
            commentList = new ArrayList<>();
        } else {
            commentList = comments;
        }
        dateSpanUtils.updateNow();
        notifyDataSetChanged();
    }

    public void addItemAt(int pos, Comment comment) {
        commentList.add(pos, comment);
        dateSpanUtils.updateNow();
        notifyDataSetChanged();
    }

    public List<Comment> getCommentList() {
        return commentList;
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        CommentHolder holder = new CommentHolder(view);
        holder.userAvatar.setOnClickListener(this);
        holder.username.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(CommentHolder holder, int position) {
        holder.userAvatar.setTag(R.id.recycler_item_position, position);
        holder.username.setTag(R.id.recycler_item_position, position);
        Comment comment = commentList.get(position);
        holder.bindItem(comment);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int position = (int) v.getTag(R.id.recycler_item_position);
        Comment comment = commentList.get(position);
        if (id == R.id.user_avatar || id == R.id.username) {
            User user = comment.getUser();
            onItemClickListener.onUserClick(position, comment, user);
        }
    }

    public interface OnItemClickListener {
        void onUserClick(int position, Comment comment, User user);
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar;
        TextView username;
        TextView updateTime;
        TextView commentText;

        public CommentHolder(View itemView) {
            super(itemView);
            userAvatar = (ImageView) itemView.findViewById(R.id.user_avatar);
            username = (TextView) itemView.findViewById(R.id.username);
            updateTime = (TextView) itemView.findViewById(R.id.update_time);
            commentText = (TextView) itemView.findViewById(R.id.comment_tv);
        }

        public void bindItem(Comment comment) {
            final Context context = itemView.getContext();

            final User user = comment.getUser();
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

            updateTime.setText(dateSpanUtils.format(comment.getUpdateDate()));
            commentText.setText(comment.getComment());
        }
    }
}
