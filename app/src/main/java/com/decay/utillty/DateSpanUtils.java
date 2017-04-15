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

import android.support.v4.util.ArrayMap;
import android.text.format.DateUtils;

import org.threeten.bp.LocalDateTime;

import java.util.Date;

public class DateSpanUtils {
    private long now;
    private ArrayMap<Long, String> dateMap;

    public DateSpanUtils() {
        updateNow();
    }

    public DateSpanUtils(Date now) {
        updateNow(now);
    }

    public void updateNow() {
        updateNow(com.decay.utillty.DateUtils.toDate(LocalDateTime.now().plusSeconds(1)));
    }

    public void updateNow(Date now) {
        this.now = now.getTime();
        if (dateMap == null) {
            dateMap = new ArrayMap<>(8);
        } else {
            dateMap.clear();
        }
    }

    public CharSequence format(Date target) {
        long date = target.getTime();
        String dateStr = dateMap.get(date);
        if (dateStr == null) {
            dateStr = DateUtils.getRelativeTimeSpanString(date, now, 0, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
            dateMap.put(date, dateStr);
        }
        return dateStr;
    }
}
