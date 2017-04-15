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
package com.tomeokin.lspush.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DeleteTextView extends AppCompatTextView {
    private Drawable deleteDrawable;
    private OnTextDeleteListener onTextDeleteListener;

    public DeleteTextView(Context context) {
        super(context);
        init();
    }

    public DeleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DeleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Drawable[] compoundDrawables = getCompoundDrawables();
        deleteDrawable = compoundDrawables[2];
        if (deleteDrawable != null) {
            deleteDrawable.setBounds(0, 0, (int) (deleteDrawable.getIntrinsicWidth() / 1.1f),
                (int) (deleteDrawable.getIntrinsicHeight() / 1.1f));
        }
        setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], deleteDrawable, compoundDrawables[3]);
    }

    public OnTextDeleteListener getOnTextDeleteListener() {
        return onTextDeleteListener;
    }

    public void setOnTextDeleteListener(OnTextDeleteListener listener) {
        onTextDeleteListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (deleteDrawable != null
            && event.getAction() == MotionEvent.ACTION_UP
            && event.getX() > getWidth() - getPaddingRight() - deleteDrawable.getIntrinsicWidth()) {

            if (onTextDeleteListener != null) {
                onTextDeleteListener.onDeleteText(this, getText().toString());
            }

            event.setAction(MotionEvent.ACTION_CANCEL);
            super.onTouchEvent(event);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public interface OnTextDeleteListener {
        void onDeleteText(DeleteTextView view, String text);
    }
}
