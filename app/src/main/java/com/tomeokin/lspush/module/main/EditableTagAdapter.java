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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomeokin.lspush.R;
import com.tomeokin.lspush.widget.DeleteTextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EditableTagAdapter extends RecyclerView.Adapter<EditableTagAdapter.EditableTagHolder>
    implements View.OnClickListener, DeleteTextView.OnTextDeleteListener {

    private LinkedList<String> tagList = new LinkedList<>();
    private final OnTagClickListener onTagClickListener;
    private RecyclerView tagRv;

    public EditableTagAdapter(@NonNull OnTagClickListener listener) {
        onTagClickListener = listener;
    }

    public void setTagList(List<String> tags) {
        if (tags == null) {
            tagList.clear();
        } else {
            tagList.addAll(tags);
        }
        notifyDataSetChanged();
    }

    public void addTag(@NonNull String tag) {
        int index = tagList.indexOf(tag);
        if (index != -1) {
            scrollToPosition(index);
            return;
        }
        index = tagList.size();
        tagList.add(tag);
        notifyItemInserted(index);
        scrollToPosition(index);
    }

    public void scrollToPosition(int pos) {
        if (tagRv == null) return;

        RecyclerView.LayoutManager layoutManager = tagRv.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            return;
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        linearLayoutManager.scrollToPositionWithOffset(pos, 0);
    }

    public void removeTag(String tag) {
        int index = tagList.indexOf(tag);
        if (index != -1) {
            tagList.remove(index);
        }
        notifyItemRemoved(index);
    }

    public List<String> getTagList() {
        return new ArrayList<>(tagList);
    }

    @Override
    public EditableTagHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_editable, parent, false);
        EditableTagHolder holder = new EditableTagHolder(view);
        holder.tagTv.setOnClickListener(this);
        holder.tagTv.setOnTextDeleteListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(EditableTagHolder holder, int position) {
        final String tag = tagList.get(position);
        holder.itemView.setTag(R.id.recycler_tag_item_id, tag);
        holder.bindItem(tag);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag(R.id.recycler_tag_item_id);
        onTagClickListener.onTagClick(tag);
    }

    @Override
    public void onDeleteText(DeleteTextView view, String text) {
        String tag = (String) view.getTag(R.id.recycler_tag_item_id);
        removeTag(tag);
        onTagClickListener.onTagDelete(text);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        tagRv = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        tagRv = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public interface OnTagClickListener {
        void onTagClick(String tag);

        void onTagDelete(String tag);
    }

    public static class EditableTagHolder extends RecyclerView.ViewHolder {
        DeleteTextView tagTv;

        public EditableTagHolder(View itemView) {
            super(itemView);
            tagTv = (DeleteTextView) itemView.findViewById(R.id.tag_tv);
        }

        public void bindItem(String tag) {
            tagTv.setText(tag);
        }
    }
}
