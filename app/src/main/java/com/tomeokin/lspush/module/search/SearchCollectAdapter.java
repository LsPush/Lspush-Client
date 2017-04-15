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
package com.tomeokin.lspush.module.search;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.decay.utillty.DateSpanUtils;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.module.main.TagAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchCollectAdapter extends RecyclerView.Adapter<SearchCollectAdapter.SearchCollectHolder>
    implements View.OnClickListener, TagAdapter.OnTagClickListener {
    private List<Collect> collectList = new ArrayList<>(0);
    OnCollectClickListener onCollectClickListener;
    DateSpanUtils dateSpanUtils = new DateSpanUtils();

    public SearchCollectAdapter(OnCollectClickListener onCollectClickListener) {
        this.onCollectClickListener = onCollectClickListener;
    }

    public void setCollectList(List<Collect> collectList) {
        if (collectList == null) {
            this.collectList.clear();
        } else {
            this.collectList = collectList;
        }
        dateSpanUtils.updateNow();
        notifyDataSetChanged();
    }

    public List<Collect> getCollectList() {
        return collectList;
    }

    public List<Collect> updateCollectItem(int position, Collect collect) {
        collectList.set(position, collect);
        dateSpanUtils.updateNow();
        notifyDataSetChanged();
        return collectList;
    }

    @Override
    public SearchCollectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.item_search_collect, parent, false);
        final SearchCollectHolder holder = new SearchCollectHolder(view, this);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(SearchCollectHolder holder, int position) {
        holder.itemView.setTag(R.id.recycler_item_position, position);

        final Context context = holder.itemView.getContext();
        Collect collect = collectList.get(position);

        holder.colTitleTv.setText(collect.getTitle());
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

        holder.tagAdapter.setGroup(position);
        if (collect.getTags() == null || collect.getTags().size() == 0) {
            holder.tagRv.setVisibility(View.GONE);
            holder.tagAdapter.setTagList(null);
        } else {
            holder.tagRv.setVisibility(View.VISIBLE);
            holder.tagAdapter.setTagList(collect.getTags());
        }

        final User user = collect.getUser();
        String note = context.getString(R.string.search_collect_bottom_note, collect.getFavorCount(),
            collect.getCommentCount(), user.getUsername(), dateSpanUtils.format(collect.getUpdateDate()));
        holder.bottomNoteTv.setText(note);
    }

    @Override
    public void onClick(View v) {
        final int pos = (int) v.getTag(R.id.recycler_item_position);
        Collect collect = collectList.get(pos);
        onCollectClickListener.onCollectClick(this, pos, collect);
    }

    @Override
    public int getItemCount() {
        return collectList.size();
    }

    @Override
    public void onTagClick(int group, int position, String tag) {
        onCollectClickListener.onTagClick(tag);
    }

    public class SearchCollectHolder extends RecyclerView.ViewHolder {
        ImageView colDescImgIv;
        TextView colTitleTv;
        TextView bottomNoteTv;
        RecyclerView tagRv;
        TagAdapter tagAdapter;

        public SearchCollectHolder(View itemView, TagAdapter.OnTagClickListener listener) {
            super(itemView);
            colDescImgIv = (ImageView) itemView.findViewById(R.id.col_descImg);
            colTitleTv = (TextView) itemView.findViewById(R.id.col_title);
            tagRv = (RecyclerView) itemView.findViewById(R.id.tag_rv);
            bottomNoteTv = (TextView) itemView.findViewById(R.id.bottomNote_tv);

            tagAdapter = new TagAdapter(listener);
            tagRv.setLayoutManager(
                new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            tagRv.setAdapter(tagAdapter);
        }
    }

    public interface OnCollectClickListener {
        void onCollectClick(SearchCollectAdapter adapter, int pos, Collect collect);

        void onTagClick(String tag);
    }
}
