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
package com.decay.recyclerview.listener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean enabled = true;
    private boolean loading = false;
    private int visibleThreshold = 3; // visibleRows * spanCount
    private int[] mLastVisibleItemPositions;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLoading() {
        return loading;
    }

    public int getVisibleThreshold() {
        return visibleThreshold;
    }

    public void setVisibleThreshold(int mVisibleThreshold) {
        this.visibleThreshold = mVisibleThreshold;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (mLayoutManager == null) {
            mLayoutManager = recyclerView.getLayoutManager();
        }

        if (shouldLoadMore(mLayoutManager)) {
            onLoadMore();
        }
    }

    public boolean shouldLoadMore(RecyclerView.LayoutManager manager) {
        int totalItemCount = manager.getItemCount();
        int lastVisibleItem = findLastVisibleItemPosition(manager);
        int visibleThreshold = this.visibleThreshold;
        visibleThreshold = Math.max(visibleThreshold, manager.getChildCount());
        return enabled && !loading && totalItemCount <= lastVisibleItem + visibleThreshold;
    }

    public int findLastVisibleItemPosition(RecyclerView.LayoutManager manager) {
        if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findLastVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager) manager;
            if (mLastVisibleItemPositions == null) {
                mLastVisibleItemPositions = new int[lm.getSpanCount()];
            }

            lm.findLastVisibleItemPositions(mLastVisibleItemPositions);
            // get maximum element within the list
            return getLastVisibleItem(mLastVisibleItemPositions);
        }
        return 0;
    }

    public int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    public abstract void onLoadMore();
}
