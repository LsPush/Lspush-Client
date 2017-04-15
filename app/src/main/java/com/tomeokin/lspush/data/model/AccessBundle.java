/*
 * Copyright 2016 TomeOkin
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
package com.tomeokin.lspush.data.model;

import com.decay.utillty.DateUtils;
import com.tomeokin.lspush.app.crypto.CryptoToken;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.Date;

public class AccessBundle {
    private static final long EXPIRE_TIME = 24 * 60; // minutes
    private static final long SHOULD_REFRESH = EXPIRE_TIME / 6;
    private static final Duration SHOULD_REFRESH_DURATION = Duration.of(SHOULD_REFRESH, ChronoUnit.MINUTES);
    private static final Duration DEADLINE_DURATION = Duration.of(10, ChronoUnit.MINUTES);

    private Date expireTime;
    // 提供 user 供后续使用
    private User user;

    private CryptoToken expireToken;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public CryptoToken getExpireToken() {
        return expireToken;
    }

    public void updateExpireToken(Date expireTime, CryptoToken expireToken) {
        this.expireTime = expireTime;
        this.expireToken = expireToken;
    }

    /**
     * @return -1 需要手动登录，0 口令超时，1 时间充足
     */
    public int checkTokenTimeOut(boolean checkSufficient) {
        if (expireTime == null) {
            return -1;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expire = DateUtils.toLocalDateTime(expireTime);
        Duration duration = Duration.between(now, expire);
        Duration deadlineDiff = duration.minus(checkSufficient ? SHOULD_REFRESH_DURATION : DEADLINE_DURATION);
        if (deadlineDiff.isNegative()) {
            return 0;
        }

        return deadlineDiff.isNegative() ? 0 : 1;
    }

    @Override
    public String toString() {
        return "AccessBundle{" +
            "expireTime=" + expireTime +
            ", user=" + user +
            ", expireToken=" + expireToken +
            '}';
    }
}
