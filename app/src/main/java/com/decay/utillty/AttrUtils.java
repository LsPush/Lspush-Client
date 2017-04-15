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
package com.decay.utillty;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleableRes;
import android.support.v4.content.ContextCompat;

public class AttrUtils implements Recyclable {
    private Context context;
    private TypedArray typedArray;

    public static AttrUtils of(@NonNull Context context, @NonNull TypedArray typedArray) {
        return new AttrUtils(context, typedArray);
    }

    private AttrUtils(@NonNull Context context, @NonNull TypedArray typedArray) {
        this.context = context;
        this.typedArray = typedArray;
    }

    public float getDimension(@StyleableRes int index, @DimenRes int dimen) {
        return typedArray.getDimension(index, context.getResources().getDimension(dimen));
    }

    public int getDimensionInt(@StyleableRes int index, @DimenRes int dimen) {
        return typedArray.getDimensionPixelSize(index, context.getResources().getDimensionPixelSize(dimen));
    }

    public int getColor(@StyleableRes int index, @ColorRes int color) {
        return typedArray.getColor(index, ContextCompat.getColor(context, color));
    }

    public Drawable getDrawable(@StyleableRes int index, @DrawableRes int res) {
        Drawable drawable = typedArray.getDrawable(index);
        return drawable != null ? drawable : ContextCompat.getDrawable(context, res);
    }

    public Drawable getDrawable(@StyleableRes int index) {
        return typedArray.getDrawable(index);
    }

    @Override
    public void recycle() {
        typedArray.recycle();
        typedArray = null;
        context = null;
    }
}
