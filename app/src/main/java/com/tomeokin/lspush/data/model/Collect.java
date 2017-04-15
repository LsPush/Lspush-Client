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

import com.decay.gson.GsonIgnore;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Collect {
    public static final Comparator<Collect> UPDATE_COMPARATOR = new Comparator<Collect>() {
        @Override
        public int compare(Collect o1, Collect o2) {
            // desc
            int update = o2.updateDate.compareTo(o1.getUpdateDate());
            return update != 0 ? update : compare(o2.id, o1.id);
        }

        private int compare(long x, long y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    };

    public static final Comparator<Collect> FAVOR_UPDATE_COMPARATOR = new Comparator<Collect>() {
        @Override
        public int compare(Collect o1, Collect o2) {
            // desc
            int favorOrder = compare(o2.getFavorCount(), o1.getFavorCount());
            return favorOrder != 0 ? favorOrder : compareUpdate(o1, o2);
        }

        private int compareUpdate(Collect o1, Collect o2) {
            // desc
            int update = o2.updateDate.compareTo(o1.getUpdateDate());
            return update != 0 ? update : compare(o2.id, o1.id);
        }

        private int compare(long x, long y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    };

    private long id;
    private User user;
    private String title;
    private String url;
    private String description;
    private String image;
    private Date createDate;
    private Date updateDate;

    private List<String> tags;
    private long favorCount;
    private boolean hasFavor;
    private long commentCount;

    @GsonIgnore private boolean hasRead;

    public void toggleFavor() {
        hasFavor = !hasFavor;
        if (hasFavor) {
            favorCount++;
        } else {
            favorCount--;
        }
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getFavorCount() {
        return favorCount;
    }

    public void setFavorCount(long favorCount) {
        this.favorCount = favorCount;
    }

    public boolean isHasFavor() {
        return hasFavor;
    }

    public void setHasFavor(boolean hasFavor) {
        this.hasFavor = hasFavor;
    }

    public boolean isHasRead() {
        return hasRead;
    }

    public void setHasRead(boolean hasRead) {
        this.hasRead = hasRead;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public String toString() {
        return "Collect{" +
            "id=" + id +
            ", user=" + user +
            ", title='" + title + '\'' +
            ", url='" + url + '\'' +
            ", description='" + description + '\'' +
            ", image='" + image + '\'' +
            ", createDate=" + createDate +
            ", updateDate=" + updateDate +
            ", tags=" + tags +
            ", favorCount=" + favorCount +
            ", hasFavor=" + hasFavor +
            ", commentCount=" + commentCount +
            ", hasRead=" + hasRead +
            '}';
    }
}
