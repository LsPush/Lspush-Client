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
package com.decay.recyclerview.section;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.decay.recyclerview.adapter.Section;
import com.tomeokin.lspush.R;

public class LoadMoreFooter extends Section implements View.OnClickListener {
    public static final int STATE_HIDE = -1;
    public static final int STATE_LOADING = 0;
    public static final int STATE_ERROR = 1;
    public static final int STATE_NO_MORE = 2;
    private int loadMoreState;
    private String loadingMore;
    private String loadMoreFailed;
    private String noMore;
    private OnLoadMoreClickListener onLoadMoreClickListener;

    public LoadMoreFooter(Context context, OnLoadMoreClickListener listener) {
        super(R.layout.layout_footer_loading_more);
        loadMoreState = STATE_HIDE;
        Resources res = context.getResources();
        loadingMore = res.getString(R.string.footer_loading_more);
        loadMoreFailed = res.getString(R.string.footer_load_more_failed);
        noMore = res.getString(R.string.footer_no_more);
        onLoadMoreClickListener = listener;
    }

    public String getLoadingMoreText() {
        return loadingMore;
    }

    public void setLoadingMoreText(String loadingMore) {
        this.loadingMore = loadingMore;
    }

    public String getLoadMoreFailedText() {
        return loadMoreFailed;
    }

    public void setLoadMoreFailedText(String loadMoreFailed) {
        this.loadMoreFailed = loadMoreFailed;
    }

    public String getNoMoreText() {
        return noMore;
    }

    public void setNoMoreText(String noMore) {
        this.noMore = noMore;
    }

    public void setState(int state) {
        if (loadMoreState != state) {
            loadMoreState = state;
        }
    }

    @Override
    public RecyclerView.ViewHolder createSection(View view) {
        FooterHolder holder = new FooterHolder(view);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    @Override
    public void populate(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterHolder) {
            final FooterHolder footerHolder = (FooterHolder) holder;
            bindItem(footerHolder, loadMoreState);
        }
    }

    @Override
    public void onClick(View v) {
        onLoadMoreClickListener.onLoadMoreClick(this, loadMoreState);
    }

    public void bindItem(FooterHolder holder, int state) {
        switch (state) {
            case STATE_LOADING: {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.loadMoreDescTv.setText(loadingMore);
                break;
            }
            case STATE_ERROR: {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
                holder.loadMoreDescTv.setText(loadMoreFailed);
                break;
            }
            case STATE_HIDE: {
                holder.itemView.setVisibility(View.GONE);
                break;
            }
            case STATE_NO_MORE:
            default: {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
                holder.loadMoreDescTv.setText(noMore);
                break;
            }
        }
    }

    public interface OnLoadMoreClickListener {
        void onLoadMoreClick(LoadMoreFooter footer, int state);
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        View loadMoreFooter;
        ProgressBar progressBar;
        TextView loadMoreDescTv;

        public FooterHolder(View itemView) {
            super(itemView);
            loadMoreFooter = itemView.findViewById(R.id.load_more_footer);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            loadMoreDescTv = (TextView) itemView.findViewById(R.id.load_more_desc_tv);
        }
    }
}
