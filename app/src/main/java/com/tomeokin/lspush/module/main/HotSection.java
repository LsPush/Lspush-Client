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

public class HotSection extends Section {

    public HotSection() {
        super(R.layout.layout_hot_collect_header);
    }

    @Override
    public RecyclerView.ViewHolder createSection(View view) {
        return new HotHolder(view);
    }

    public static class HotHolder extends RecyclerView.ViewHolder {
        TextView titleTv;

        public HotHolder(View itemView) {
            super(itemView);
            titleTv = (TextView) itemView.findViewById(R.id.title_tv);
        }
    }
}
