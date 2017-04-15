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
package com.tomeokin.lspush.data.internal;

import android.content.Context;

import com.decay.di.qualifier.AppContext;
import com.decay.logger.Log;
import com.decay.utillty.DateUtils;
import com.google.gson.Gson;
import com.tomeokin.lspush.app.crypto.CryptoToken;
import com.tomeokin.lspush.app.crypto.PropertyStorage;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.model.AccessBundle;
import com.tomeokin.lspush.data.model.User;

import java.util.Date;
import java.util.List;

public class CurrentUser {
    public static final String PROPERTY_NAME = "currentUser";
    private Log log = AppLogger.of("CurrentUser");

    private final Gson gson;
    private final Context context;

    public CurrentUser(Gson gson, @AppContext Context context) {
        this.gson = gson;
        this.context = context;
    }

    private List<User> history;
    private AccessBundle current;
    private String authToken;

    public List<User> getHistoryUser() {
        return history;
    }

    public void setHistoryUser(List<User> history) {
        this.history = history;
    }

    public void setInstance(AccessBundle current) {
        this.current = current;
        updateAuthToken();
    }

    public AccessBundle getAccessBundle() {
        if (current == null) {
            reloadUserInfo();
        }
        return current;
    }

    //public void updateExpireToken(Date expireTime, CryptoToken expireToken) {
    //    if (current == null) current = new AccessBundle();
    //    current.updateExpireToken(expireTime, expireToken);
    //    updateAuthToken();
    //}

    public User getCurrentUser() {
        if (current == null) {
            reloadUserInfo();
        }
        return current == null ? null : current.getUser();
    }

    public String getAccount() {
        if (current == null) {
            reloadUserInfo();
        }
        if (current == null || current.getUser() == null) return null;
        return current.getUser().getPhone();
    }

    public String getPassword() {
        if (current == null) {
            reloadUserInfo();
        }
        if (current == null || current.getUser() == null) return null;
        return current.getUser().getPassword();
    }

    public CryptoToken getExpireToken() {
        if (current == null) {
            reloadUserInfo();
        }
        return current != null ? current.getExpireToken() : null;
    }

    public Date getExpireTime() {
        if (current == null) {
            reloadUserInfo();
        }
        return current != null ? current.getExpireTime() : null;
    }

    private void updateAuthToken() {
        CryptoToken token = getExpireToken();
        if (token == null) return;

        try {
            authToken = gson.toJson(token);
        } catch (Throwable cause) {
            log.log(cause, "Convert token to json failed");
        }
    }

    public String getAuthToken() {
        if (authToken == null) {
            updateAuthToken();
        }
        return authToken;
    }

    public boolean isLogout() {
        return current == null
            || current.getUser() == null
            || current.getUser().getPassword() == null
            || current.getUser().getPhone() == null
            || authToken != null;
    }

    public void reloadUserInfo() {
        PropertyStorage storage = new PropertyStorage(context);
        String accessInfo = storage.getString(CurrentUser.PROPERTY_NAME);
        if (accessInfo != null) {
            AccessBundle accessBundle = gson.fromJson(accessInfo, AccessBundle.class);
            setInstance(accessBundle);

            final User user = accessBundle.getUser();
            log.log("user: %s", user.toString());
            final CryptoToken cryptoToken = accessBundle.getExpireToken();
            log.log("expire token: %s", cryptoToken.toString());
            final Date expireTime = accessBundle.getExpireTime();
            log.log("expire time: %s", DateUtils.toFormatDateTime(expireTime));
        }
    }
}
