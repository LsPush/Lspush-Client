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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tomeokin.lspush.R;

import java.util.ArrayList;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagHolder> implements View.OnClickListener {
    private List<String> tagList = new ArrayList<>(0);
    private int group = -1;
    private final OnTagClickListener onTagClickListener;

    public TagAdapter(@NonNull OnTagClickListener onTagClickListener) {
        this.onTagClickListener = onTagClickListener;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public void setTagList(List<String> tags) {
        if (tags == null) {
            tagList = new ArrayList<>(0);
        } else {
            tagList = tags;
        }
        notifyDataSetChanged();
    }

    @Override
    public TagHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        TagHolder holder = new TagHolder(view);
        holder.tagTv.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(TagHolder holder, int position) {
        holder.itemView.setTag(R.id.recycler_item_position, position);

        final String tag = tagList.get(position);
        holder.bindItem(tag);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag(R.id.recycler_item_position);
        onTagClickListener.onTagClick(group, position, tagList.get(position));
    }

    public interface OnTagClickListener {
        void onTagClick(int group, int position, String tag);
    }

    public static class TagHolder extends RecyclerView.ViewHolder {
        TextView tagTv;

        public TagHolder(View itemView) {
            super(itemView);
            tagTv = (TextView) itemView.findViewById(R.id.tag_tv);
        }

        public void bindItem(String tag) {
            tagTv.setText(tag);
        }
    }
}
