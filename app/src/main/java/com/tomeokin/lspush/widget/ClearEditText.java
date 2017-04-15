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
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ClearEditText extends AppCompatEditText implements View.OnFocusChangeListener {
    private Drawable clearDrawable;
    private boolean isLastTextFull;

    public ClearEditText(Context context) {
        super(context);
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        clearDrawable = getCompoundDrawables()[2];
        setClearIconVisible(false);
        setOnFocusChangeListener(this);
    }

    public void setClearIconVisible(boolean visible) {
        if (clearDrawable == null) {
            return;
        }

        Drawable right = visible ? clearDrawable : null;
        if (right != null) {
            right.setBounds(0, 0, (int) (right.getIntrinsicWidth() / 1.1f), (int) (right.getIntrinsicHeight() / 1.1f));
        }
        Drawable[] compoundDrawables = getCompoundDrawables();
        setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], right, compoundDrawables[3]);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            isLastTextFull = getText().length() > 0;
            setClearIconVisible(isLastTextFull);
        } else {
            setClearIconVisible(false);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        boolean isEmpty = TextUtils.isEmpty(getText());
        if (isLastTextFull == isEmpty) {
            isLastTextFull = !isEmpty;
            setClearIconVisible(isLastTextFull);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLastTextFull
            && clearDrawable != null
            && event.getAction() == MotionEvent.ACTION_UP
            && event.getX() > getWidth() - getPaddingRight() - clearDrawable.getIntrinsicWidth()) {
            setText("");
            requestFocus();
        }

        return super.onTouchEvent(event);
    }
}
