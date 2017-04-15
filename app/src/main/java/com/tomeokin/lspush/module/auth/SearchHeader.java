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
package com.tomeokin.lspush.module.auth;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.decay.recyclerview.adapter.Section;
import com.decay.recyclerview.adapter.SectionHolder;
import com.tomeokin.lspush.R;

public class SearchHeader extends Section {
    private View.OnClickListener listener;
    private SpannableString searchSpan;

    public SearchHeader(Context context, View.OnClickListener listener) {
        super(R.layout.layout_search_textview);
        this.listener = listener;

        Drawable searchIcon = ContextCompat.getDrawable(context, R.drawable.search);
        searchIcon = DrawableCompat.wrap(searchIcon);
        DrawableCompat.setTint(searchIcon, ContextCompat.getColor(context, R.color.blue_gray_30));
        searchIcon.setBounds(0, 0, searchIcon.getIntrinsicWidth(), searchIcon.getIntrinsicHeight());
        ImageSpan imageSpan = new ImageSpan(searchIcon, ImageSpan.ALIGN_BOTTOM);

        String search = context.getString(R.string.search);
        searchSpan = new SpannableString(search + search);
        searchSpan.setSpan(imageSpan, 0, search.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    @Override
    public RecyclerView.ViewHolder createSection(View view) {
        final TextHolder textHolder = new TextHolder(view);
        textHolder.textView.setOnClickListener(listener);
        return textHolder;
    }

    @Override
    public void populate(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TextHolder) {
            TextHolder textHolder = (TextHolder) holder;
            textHolder.textView.setTag(R.id.section_item_position, position);
            textHolder.textView.setText(searchSpan);
        }
    }

    public static class TextHolder extends SectionHolder {
        TextView textView;

        public TextHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.search_tv);
        }
    }
}
