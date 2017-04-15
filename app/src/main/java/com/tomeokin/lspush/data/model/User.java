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

public class User {
    private long id;
    private String username;
    private String phone;
    private String country;
    private String password;
    private String avatar;
    private String description;

    private boolean hasFollow;

    public void triggerFollow() {
        hasFollow = true;
    }

    public void triggerUnFollow() {
        hasFollow = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHasFollow() {
        return hasFollow;
    }

    public void setHasFollow(boolean hasFollow) {
        this.hasFollow = hasFollow;
    }

    public User profile() {
        User user = new User();
        user.id = id;
        user.username = username;
        user.avatar = avatar;
        user.description = description;
        user.hasFollow = hasFollow;
        return user;
    }

    @Override
    public String toString() {
        // @formatter:off
        return "User{"
            + "id=" + id
            + ", username='" + username + '\''
            + ", phone='" + phone + '\''
            + ", country='" + country + '\''
            + ", password='" + password + '\''
            + ", avatar='" + avatar + '\''
            + ", description='" + description + '\''
            + '}';
        // @formatter:on
    }
}
