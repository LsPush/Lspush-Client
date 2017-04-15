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
package com.tomeokin.lspush.data.model;

import java.io.Serializable;
import java.util.List;

public class UserProfile implements Serializable {
    private static final long serialVersionUID = -8875275569216573960L;

    private User user;
    private long followingCount;
    private long followersCount;
    private long shareCount;
    private long favorCount;
    private boolean hasFollow;
    private List<Collect> hotShareCollect;

    public long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(long followersCount) {
        this.followersCount = followersCount;
    }

    public long getShareCount() {
        return shareCount;
    }

    public void setShareCount(long shareCount) {
        this.shareCount = shareCount;
    }

    public long getFavorCount() {
        return favorCount;
    }

    public void setFavorCount(long favorCount) {
        this.favorCount = favorCount;
    }

    public boolean isHasFollow() {
        return hasFollow;
    }

    public void setHasFollow(boolean hasFollow) {
        this.hasFollow = hasFollow;
    }

    public List<Collect> getHotShareCollect() {
        return hotShareCollect;
    }

    public void setHotShareCollect(List<Collect> hotShareCollect) {
        this.hotShareCollect = hotShareCollect;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void triggerFollow() {
        hasFollow = true;
        followersCount++;
    }

    public void triggerUnFollow() {
        hasFollow = false;
        followersCount--;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
            "user=" + user +
            ", followingCount=" + followingCount +
            ", followersCount=" + followersCount +
            ", shareCount=" + shareCount +
            ", favorCount=" + favorCount +
            ", hasFollow=" + hasFollow +
            ", hotShareCollect=" + hotShareCollect +
            '}';
    }
}
