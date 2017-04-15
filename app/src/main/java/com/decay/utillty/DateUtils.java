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

import android.text.TextUtils;
import android.text.format.DateFormat;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private DateUtils() {}

    public static Date parseDate(String date) {
        if (TextUtils.isEmpty(date)) return null;
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        return toDate(localDate);
    }

    public static Date parseDateTime(String datetime) {
        if (TextUtils.isEmpty(datetime)) return null;
        LocalDateTime localDateTime = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return toDate(localDateTime);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        Instant instant = DateTimeUtils.toInstant(date);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static LocalDate toLocalDate(Date date) {
        return toLocalDateTime(date).toLocalDate();
    }

    public static LocalTime toLocalTime(Date date) {
        return toLocalDateTime(date).toLocalTime();
    }

    public static Date toDate(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return DateTimeUtils.toDate(instant);
    }

    public static Date toDate(LocalDate localDate) {
        return toDate(localDate.atStartOfDay());
    }

    public static Date toDate(LocalDate localDate, LocalTime localTime) {
        return toDate(LocalDateTime.of(localDate, localTime));
    }

    public static Date toDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    public static String toFormatDate(Date date) {
        if (date == null) return null;
        return DateFormat.format("yyyy-MM-dd", date).toString();
    }

    public static String toFormatDateTime(Date date) {
        if (date == null) return null;
        return DateFormat.format("yyyy-MM-dd kk:mm:ss", date).toString();
    }
}
