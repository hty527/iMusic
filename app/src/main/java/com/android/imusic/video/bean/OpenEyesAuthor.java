package com.android.imusic.video.bean;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 */

public class OpenEyesAuthor {

    /**
     * approvedNotReadyVideoCount : 0
     * description : 为广告人的精彩创意点赞
     * expert : false
     * follow : {"followed":false,"itemId":2162,"itemType":"author"}
     * icon : http://img.kaiyanapp.com/98beab66d3885a139b54f21e91817c4f.jpeg
     * id : 2162
     * ifPgc : true
     * latestReleaseTime : 1554598828000
     * link :
     * name : 开眼广告精选
     * recSort : 0
     * shield : {"itemId":2162,"itemType":"author","shielded":false}
     * videoNum : 1119
     */

    private int approvedNotReadyVideoCount;
    private String description;
    private boolean expert;
    /**
     * followed : false
     * itemId : 2162
     * itemType : author
     */

    private FollowBean follow;
    private String icon;
    private int id;
    private boolean ifPgc;
    private long latestReleaseTime;
    private String link;
    private String name;
    private int recSort;
    /**
     * itemId : 2162
     * itemType : author
     * shielded : false
     */

    private ShieldBean shield;
    private int videoNum;

    public int getApprovedNotReadyVideoCount() {
        return approvedNotReadyVideoCount;
    }

    public void setApprovedNotReadyVideoCount(int approvedNotReadyVideoCount) {
        this.approvedNotReadyVideoCount = approvedNotReadyVideoCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isExpert() {
        return expert;
    }

    public void setExpert(boolean expert) {
        this.expert = expert;
    }

    public FollowBean getFollow() {
        return follow;
    }

    public void setFollow(FollowBean follow) {
        this.follow = follow;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isIfPgc() {
        return ifPgc;
    }

    public void setIfPgc(boolean ifPgc) {
        this.ifPgc = ifPgc;
    }

    public long getLatestReleaseTime() {
        return latestReleaseTime;
    }

    public void setLatestReleaseTime(long latestReleaseTime) {
        this.latestReleaseTime = latestReleaseTime;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRecSort() {
        return recSort;
    }

    public void setRecSort(int recSort) {
        this.recSort = recSort;
    }

    public ShieldBean getShield() {
        return shield;
    }

    public void setShield(ShieldBean shield) {
        this.shield = shield;
    }

    public int getVideoNum() {
        return videoNum;
    }

    public void setVideoNum(int videoNum) {
        this.videoNum = videoNum;
    }

    public static class FollowBean {
        private boolean followed;
        private int itemId;
        private String itemType;

        public boolean isFollowed() {
            return followed;
        }

        public void setFollowed(boolean followed) {
            this.followed = followed;
        }

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

        public String getItemType() {
            return itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }
    }

    public static class ShieldBean {
        private int itemId;
        private String itemType;
        private boolean shielded;

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

        public String getItemType() {
            return itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

        public boolean isShielded() {
            return shielded;
        }

        public void setShielded(boolean shielded) {
            this.shielded = shielded;
        }
    }
}
