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
package com.tomeokin.lspush.framework.nav;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;

public class NavOption {
    final String tag;
    final int requestCode;
    final int[] anim;
    final int transition;
    final boolean addFirstIntoBackStack;

    private NavOption(String tag, int requestCode, int[] anim, int transition, boolean addFirstIntoBackStack) {
        this.tag = tag;
        this.requestCode = requestCode;
        this.anim = anim;
        this.transition = transition;
        this.addFirstIntoBackStack = addFirstIntoBackStack;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        String tag;
        int requestCode = NavUtils.DEFAULT_REQUEST_CODE;
        int[] anim;
        int transition = FragmentTransaction.TRANSIT_UNSET;
        boolean addFirstIntoBackStack;

        public Builder tag(@NonNull String tag) {
            this.tag = tag;
            return this;
        }

        public Builder requestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        public Builder anim(@NonNull int[] anim) {
            this.anim = anim;
            return this;
        }

        public Builder transit(int transit) {
            this.transition = transit;
            return this;
        }

        public Builder addFirstIntoBackStack(boolean addFirstIntoBackStack) {
            this.addFirstIntoBackStack = addFirstIntoBackStack;
            return this;
        }

        public NavOption build() {
            return new NavOption(tag, requestCode, anim, transition, addFirstIntoBackStack);
        }
    }
}
