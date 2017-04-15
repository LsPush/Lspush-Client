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
package com.decay.recyclerview.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.View;

import com.decay.recyclerview.callback.HeaderProvider;
import com.decay.utillty.AttrUtils;
import com.decay.utillty.Toolkit;
import com.tomeokin.lspush.R;

public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {
    private Paint textPaint;
    private Paint bgPaint;
    private float textHeight;
    private float horizontalSpacing;
    private float rowHeight;
    private HeaderProvider itemProvider;
    private boolean sticky = true;

    public StickyHeaderDecoration(Context context, @NonNull HeaderProvider provider) {
        this(context, R.style.StickyHeaderDecoration, provider);
    }

    public StickyHeaderDecoration(Context context, @StyleRes int style, @NonNull HeaderProvider provider) {
        this(context, style, 0, provider);
    }

    public StickyHeaderDecoration(Context context, @StyleRes int style, @AttrRes int defStyleAttr,
        @NonNull HeaderProvider provider) {

        final TypedArray typedArray = context.obtainStyledAttributes(null, R.styleable.StickyHeaderDecoration,
            defStyleAttr, style);
        AttrUtils attrUtils = AttrUtils.of(context, typedArray);

        float textSize = attrUtils.getDimension(R.styleable.StickyHeaderDecoration_android_textSize,
            R.dimen.header_decoration_textSize);
        int textColor = attrUtils.getColor(R.styleable.StickyHeaderDecoration_android_textColor,
            R.color.header_decoration_textColor);
        int background = attrUtils.getColor(R.styleable.StickyHeaderDecoration_android_background,
            R.color.header_decoration_background);
        horizontalSpacing = attrUtils.getDimension(R.styleable.StickyHeaderDecoration_android_horizontalSpacing,
            R.dimen.header_decoration_horizontalSpacing);
        float verticalSpacing = attrUtils.getDimension(R.styleable.StickyHeaderDecoration_android_verticalSpacing,
            R.dimen.header_decoration_verticalSpacing);

        Toolkit.tryToRecycle(attrUtils);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textHeight = textPaint.descent() - textPaint.ascent();
        rowHeight = textHeight + verticalSpacing * 2;

        bgPaint = new Paint();
        bgPaint.setColor(background);

        itemProvider = provider;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // outRect.set(0, 0, 0, 0);
        super.getItemOffsets(outRect, view, parent, state);

        int pos = parent.getChildAdapterPosition(view);
        if (pos == RecyclerView.NO_POSITION) return;

        String text = itemProvider.getHeaderText(pos);
        if (text == null) return;
        if (pos != 0 && text.equals(itemProvider.getHeaderText(pos - 1))) return;

        outRect.set(0, (int) rowHeight, 0, 0);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final int childCount = parent.getChildCount();
        final int itemCount = state.getItemCount();

        String lastItem, currentItem = null;
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(view);
            if (pos == RecyclerView.NO_POSITION) continue;

            lastItem = currentItem;
            currentItem = itemProvider.getHeaderText(pos);
            if (currentItem == null || currentItem.equals(lastItem)) continue;

            float rowY = view.getTop();
            if (sticky) {
                rowY = Math.max(rowY, rowHeight);
                if (pos + 1 < itemCount) {
                    String nextItem = itemProvider.getHeaderText(pos + 1);
                    int viewBottom = view.getBottom();
                    // new item content
                    if (!currentItem.equals(nextItem) && viewBottom < rowY) {
                        rowY = viewBottom;
                    }
                }
            }

            c.drawRect(0, rowY - rowHeight, parent.getWidth(), rowY, bgPaint);
            c.drawText(currentItem, horizontalSpacing, rowY - rowHeight + textHeight, textPaint);
        }
    }

    public boolean isSticky() {
        return sticky;
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }
}
