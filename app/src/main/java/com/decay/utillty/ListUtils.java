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
package com.decay.utillty;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListUtils {
    public static <T> List<T> combineSortList(@NonNull List<T> first, @NonNull List<T> second,
        @NonNull Comparator<T> comparator) {
        if (second.size() == 0) return new ArrayList<>(first);

        int fCount = first.size();
        int sCount = second.size();

        int pos = fCount;
        boolean noFound = true;
        for (int i = fCount - 1; i >= 0; i--) {
            T left = first.get(i);
            T right = second.get(0);
            int result = comparator.compare(left, right);
            if (result == 0) {
                pos = i;
                noFound = false;
                break;
            } else if (result < 0) {
                pos = i;
                break;
            } else {
                pos = i - 1;
            }
        }

        // first 总小于 second
        if (pos >= fCount) {
            List<T> third = new ArrayList<>(fCount + sCount);
            third.addAll(first);
            third.addAll(second);
            return third;
        }

        // 两个 list 存在相同的部分
        List<T> third = new ArrayList<>(pos + sCount);
        third.addAll(first.subList(0, noFound ? pos + 1 : pos));
        third.add(second.get(0));

        int pi = pos + 1, pj = 1;
        for (; pi < fCount; pi++) {
            T left = first.get(pi);

            boolean inc = false;
            for (int j = pj; j < sCount; j++) {
                T right = second.get(j);

                int result = comparator.compare(left, right);
                if (result == 0) {
                    pj = j + 1;
                    third.add(right);
                    inc = true;
                    break;
                } else if (result > 0) {
                    pj = j + 1;
                    third.add(right);
                } else {
                    pj = j;
                    third.add(left);
                    break;
                }
            }

            if (pj >= sCount) {
                if (inc) pi++;
                break;
            }
        }

        if (pi < fCount) {
            third.addAll(first.subList(pi, fCount));
        }

        if (pj < sCount) {
            third.addAll(second.subList(pj, sCount));
        }
        return third;
    }
}
