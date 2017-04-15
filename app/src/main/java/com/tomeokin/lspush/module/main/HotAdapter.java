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

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.decay.recyclerview.adapter.SectionAdapter;

public class HotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_RECENT_TOP_COLLECT = 11;
    private static final int ITEM_HOT_COLLECT = 13;

    private final SectionAdapter recentTopAdapter;
    private final SectionAdapter hotAdapter;

    public HotAdapter(SectionAdapter recentTopAdapter, SectionAdapter hotAdapter) {
        this.recentTopAdapter = recentTopAdapter;
        this.hotAdapter = hotAdapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_RECENT_TOP_COLLECT || recentTopAdapter.hasViewType(viewType)) {
            return recentTopAdapter.onCreateViewHolder(parent, viewType);
        } else {
            return hotAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int pos = position;
        if (pos >= 0 && pos < recentTopAdapter.getItemCount()) {
            //noinspection unchecked
            recentTopAdapter.onBindViewHolder(holder, pos);
            return;
        }
        pos -= recentTopAdapter.getItemCount();
        if (pos >= 0 && pos < hotAdapter.getItemCount()) {
            //noinspection unchecked
            hotAdapter.onBindViewHolder(holder, pos);
        }
    }

    @Override
    public int getItemCount() {
        return recentTopAdapter.getItemCount() + hotAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        int pos = position;
        if (pos >= 0 && pos < recentTopAdapter.getItemCount()) {
            int viewType = recentTopAdapter.getItemViewType(pos);
            return viewType < 0 ? viewType : ITEM_RECENT_TOP_COLLECT;
        }
        pos -= recentTopAdapter.getItemCount();
        if (pos >= 0 && pos < hotAdapter.getItemCount()) {
            int viewType = hotAdapter.getItemViewType(pos);
            return viewType < 0 ? viewType : ITEM_HOT_COLLECT;
        }
        return super.getItemViewType(pos);
    }
}
