/*
 * Copyright 2017 LsPush
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
package com.decay.recyclerview.adapter;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.decay.logger.Log;
import com.decay.logger.Logger;
import com.tomeokin.lspush.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Log log = Logger.tag("Recycler-Extension").debug(BuildConfig.DEBUG).subTag("Section");

    private List<Section> headers = new ArrayList<>(1);
    private List<Section> footers = new ArrayList<>(1);

    private RecyclerView.Adapter<RecyclerView.ViewHolder> rvAdapter;

    public SectionAdapter(@NonNull RecyclerView.Adapter<?> adapter) {
        //noinspection unchecked
        rvAdapter = (RecyclerView.Adapter<RecyclerView.ViewHolder>) adapter;
    }

    public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> getRecyclerAdapter() {
        return rvAdapter;
    }

    public void addHeader(Section header) {
        headers.add(header);
        notifyDataSetChanged();
    }

    public void removeHeader(@NonNull Section header) {
        headers.remove(header);
        notifyDataSetChanged();
    }

    public void removeHeader(int position) {
        if (isHeaderPosition(position)) {
            headers.remove(position);
            notifyDataSetChanged();
        }
    }

    public boolean isHeaderPosition(int position) {
        return position >= 0 && position < headers.size();
    }

    public void addFooter(Section footer) {
        footers.add(footer);
        notifyDataSetChanged();
    }

    public void removeFooter(@NonNull Section footer) {
        footers.remove(footer);
        notifyDataSetChanged();
    }

    public void removeFooter(int position) {
        if (isFooterPosition(position)) {
            footers.remove(position);
            notifyDataSetChanged();
        }
    }

    public int getAdapterPosition(int position) {
        return position - headers.size();
    }

    public void notifyAdapterItemChanged(int position) {
        notifyItemChanged(position + headers.size());
    }

    private int getIndexOfFooters(int position) {
        return position - headers.size() - rvAdapter.getItemCount();
    }

    public boolean isFooterPosition(int position) {
        final int pos = getIndexOfFooters(position);
        return pos >= 0 && pos < footers.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType < 0) {
            Section section = querySectionByViewType(viewType);
            if (section == null) {
                log.w("Missing section for viewType %d! \n Dispatch it to rvAdapter", viewType);
                return dispatchOnCreateViewHolder(parent, viewType);
            }

            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View layout = inflater.inflate(section.layout, parent, false);
            return section.createSection(layout);
        } else {
            return dispatchOnCreateViewHolder(parent, viewType);
        }
    }

    public boolean hasViewType(int viewType) {
        return querySectionByViewType(viewType) != null;
    }

    private RecyclerView.ViewHolder dispatchOnCreateViewHolder(ViewGroup parent, int viewType) {
        return rvAdapter.onCreateViewHolder(parent, viewType);
    }

    private Section querySectionByViewType(@IntRange(to = -1) int viewType) {
        for (Section header : headers) {
            if (header.viewType == viewType) return header;
        }
        for (Section footer : footers) {
            if (footer.viewType == viewType) return footer;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeaderPosition(position)) {
            Section header = headers.get(position);
            header.populate(holder, position);
        } else if (isFooterPosition(position)) {
            Section footer = footers.get(getIndexOfFooters(position));
            footer.populate(holder, position);
        } else {
            rvAdapter.onBindViewHolder(holder, getAdapterPosition(position));
        }
    }

    @Override
    public int getItemCount() {
        return headers.size() + rvAdapter.getItemCount() + footers.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return headers.get(position).viewType;
        } else if (isFooterPosition(position)) {
            return footers.get(getIndexOfFooters(position)).viewType;
        } else {
            return rvAdapter.getItemViewType(position - headers.size());
        }
    }
}
