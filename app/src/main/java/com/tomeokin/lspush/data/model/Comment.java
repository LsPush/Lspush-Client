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

import java.util.Comparator;
import java.util.Date;

public class Comment {
    public static final Comparator<Comment> UPDATE_COMPARATOR = new Comparator<Comment>() {
        @Override
        public int compare(Comment o1, Comment o2) {
            int update = o1.updateDate.compareTo(o2.getUpdateDate());
            return update != 0 ? update : compare(o1.id, o2.id);
        }

        private int compare(long x, long y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    };

    private long id;
    private User user;
    private Date updateDate;
    private String comment;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Comment{" +
            "id=" + id +
            ", user=" + user +
            ", updateDate=" + updateDate +
            ", comment='" + comment + '\'' +
            '}';
    }
}
