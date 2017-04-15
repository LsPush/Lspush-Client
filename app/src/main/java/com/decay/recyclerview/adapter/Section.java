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
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class Section {
    public int viewType;
    public @LayoutRes int layout;

    public Section(@LayoutRes int layout) {
        this.layout = layout;
        this.viewType = -layout;
    }

    public Section(@LayoutRes int layout, @IntRange(to = -1) int viewType) {
        this.layout = layout;
        this.viewType = viewType;
    }

    public RecyclerView.ViewHolder createSection(View view) {
        return new SectionHolder(view);
    }

    public void populate(RecyclerView.ViewHolder holder, int position) {

    }
}
