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
import android.view.View;
import android.widget.TextView;

import com.decay.recyclerview.adapter.Section;
import com.tomeokin.lspush.R;

public class RecentTopFooter extends Section {
    private boolean showingNoMore = false;

    public RecentTopFooter() {
        super(R.layout.layout_recent_top_footer);
    }

    public boolean isShowingNoMore() {
        return showingNoMore;
    }

    public void setShowingNoMore(boolean showingNoMore) {
        this.showingNoMore = showingNoMore;
    }

    @Override
    public RecyclerView.ViewHolder createSection(View view) {
        return new RecentTopFooterHolder(view);
    }

    @Override
    public void populate(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecentTopFooterHolder) {
            RecentTopFooterHolder recentTopFooterHolder = (RecentTopFooterHolder) holder;
            if (showingNoMore) {
                recentTopFooterHolder.noCollectTv.setVisibility(View.VISIBLE);
            } else {
                recentTopFooterHolder.noCollectTv.setVisibility(View.GONE);
            }
        }
    }

    public static class RecentTopFooterHolder extends RecyclerView.ViewHolder {
        TextView noCollectTv;

        public RecentTopFooterHolder(View itemView) {
            super(itemView);
            noCollectTv = (TextView) itemView.findViewById(R.id.no_collect_footer);
        }
    }
}
