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
package com.decay.recyclerview.index;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.decay.logger.Logger;
import com.decay.recyclerview.callback.OnIndexChangeListener;
import com.decay.utillty.AttrUtils;
import com.decay.utillty.Toolkit;
import com.tomeokin.lspush.BuildConfig;
import com.tomeokin.lspush.R;

import java.util.ArrayList;
import java.util.List;

public class IndexBar extends View {
    private TextPaint textPaint;
    private float textHeight;
    private float verticalSpacing;
    private List<String> indexTitles;
    private OnIndexChangeListener listener;
    private float contentHeight;

    private Drawable searchIcon;
    private float halfIconWidth;
    private float iconOffsetY, iconScale; // searchIcon draw argument

    public IndexBar(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public IndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public IndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IndexBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // notify system that we will consume touch event
        setClickable(true);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndexBar, defStyleAttr,
            defStyleRes);
        AttrUtils attrUtils = AttrUtils.of(context, typedArray);

        float textSize = attrUtils.getDimension(R.styleable.IndexBar_android_textSize, R.dimen.index_bar_textSize);
        int textColor = attrUtils.getColor(R.styleable.IndexBar_android_textColor, R.color.index_bar_textColor);
        searchIcon = attrUtils.getDrawable(R.styleable.IndexBar_index_search);

        int paddingLeft = attrUtils.getDimensionInt(R.styleable.IndexBar_android_paddingLeft,
            R.dimen.index_bar_horizontalPadding);
        int paddingRight = attrUtils.getDimensionInt(R.styleable.IndexBar_android_paddingRight,
            R.dimen.index_bar_horizontalPadding);
        int paddingTop = attrUtils.getDimensionInt(R.styleable.IndexBar_android_paddingTop,
            R.dimen.index_bar_verticalPadding);
        int paddingBottom = attrUtils.getDimensionInt(R.styleable.IndexBar_android_paddingBottom,
            R.dimen.index_bar_verticalPadding);
        verticalSpacing = attrUtils.getDimension(R.styleable.IndexBar_android_verticalSpacing,
            R.dimen.index_bar_verticalSpacing);
        Drawable background = attrUtils.getDrawable(R.styleable.IndexBar_android_background,
            R.drawable.index_bar_background);
        Toolkit.tryToRecycle(attrUtils);

        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        ViewCompat.setBackground(this, background);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textHeight = textPaint.descent() - textPaint.ascent() + verticalSpacing * 2;

        if (searchIcon != null) {
            int w = searchIcon.getIntrinsicWidth();
            int h = searchIcon.getIntrinsicHeight();
            searchIcon.setBounds(0, 0, w, h);

            int size = Math.max(w, h);
            iconScale = textHeight / size;
            Logger.tag("IndexBar").debug(BuildConfig.DEBUG).log("size: %d, textHeight %f", size, textHeight);

            halfIconWidth = w * iconScale / 2f;
            iconOffsetY = (textHeight - h * iconScale) / 2f;
        }

        if (isInEditMode()) {
            int count = 'Z' - 'A' + 1;
            indexTitles = new ArrayList<>(count + 1);
            for (int i = 0; i < count; i++) {
                indexTitles.add(Character.toString((char) ('A' + i)));
            }
            indexTitles.add("#");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (indexTitles == null || indexTitles.size() == 0) {
            super.onMeasure(0, 0);
            return;
        }

        float width = 0, height = 0;

        for (String title : indexTitles) {
            width = Math.max(width, textPaint.measureText(title));
            height += textHeight;
        }

        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();

        if (searchIcon != null) {
            height += textHeight;
        }

        contentHeight = height;

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // match_parent or fixed value
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        }

        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (indexTitles == null || indexTitles.size() == 0) return;

        float offsetX = getWidth() / 2.0f;
        float offsetY = getOffsetY();
        if (searchIcon != null) {
            canvas.save();
            float iconOffsetX = offsetX - halfIconWidth;
            canvas.translate(iconOffsetX, offsetY + iconOffsetY);
            canvas.scale(iconScale, iconScale);
            searchIcon.draw(canvas);
            canvas.restore();

            offsetY += textHeight;
        }

        offsetY -= verticalSpacing * 2;
        for (String title : indexTitles) {
            offsetY += textHeight;
            canvas.drawText(title, offsetX, offsetY, textPaint);
        }
    }

    private float getOffsetY() {
        float offsetY = getPaddingTop();
        if (contentHeight < getHeight()) {
            offsetY += (getHeight() - contentHeight) / 2.0f;
        }
        return offsetY;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // dispatch system event
        super.onTouchEvent(event);

        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // keep press state when move out of the view bound
                setPressed(true);

                float y = event.getY();
                int index = (int) ((y - getOffsetY()) / textHeight);
                if (index < 0) index = 0;
                if (searchIcon != null) {
                    index = Math.min(index, indexTitles.size()) - 1;
                } else {
                    index = Math.min(index, indexTitles.size() - 1);
                }
                if (listener != null) listener.onIndexSelected(index, index >= 0 ? indexTitles.get(index) : null);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                if (listener != null) listener.onIndexSelectedRelease();
        }

        return true;
    }

    public List<String> getIndexTitles() {
        return indexTitles;
    }

    public void setIndexTitles(List<String> titles) {
        this.indexTitles = titles;
    }

    public OnIndexChangeListener getOnIndexChangeListener() {
        return listener;
    }

    public void setOnIndexChangeListener(OnIndexChangeListener listener) {
        this.listener = listener;
    }
}
