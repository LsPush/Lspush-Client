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
package com.tomeokin.lspush.framework.prefer;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class Prefer {
    private final Context context;
    private final Gson gson;

    public Prefer(Context context, Gson gson) {
        this.context = context.getApplicationContext();
        this.gson = gson;
    }

    public <T> void put(@NonNull String key, T value) {
        final SharedPreferences preference = getPreference(value.getClass().getName());
        preference.edit().putString(key, gson.toJson(value)).apply();
    }

    public <T> T get(@NonNull String key, Class<T> clazz) {
        final SharedPreferences preference = getPreference(clazz.getName());
        String value = preference.getString(key, null);
        if (value == null) {
            return null;
        }
        try {
            return gson.fromJson(value, (Type) clazz);
        } catch (Throwable cause) {
            return null;
        }
    }

    private SharedPreferences getPreference(String value) {
        return context.getSharedPreferences(value, Context.MODE_APPEND);
    }
}
