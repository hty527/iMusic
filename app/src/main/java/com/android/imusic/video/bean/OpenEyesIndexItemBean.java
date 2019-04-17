package com.android.imusic.video.bean;

import android.text.TextUtils;

import com.video.player.lib.constants.VideoConstants;

import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * 开眼-列表 listBean
 * 一维数组
 */

public class OpenEyesIndexItemBean {

    //Item类型
    public static final int ITEM_UNKNOWN=0;
    public static final int ITEM_CARD=1;
    public static final int ITEM_TITLE=2;
    public static final int ITEM_FOLLOW=3;
    public static final int ITEM_VIDEO=4;
    public static final int ITEM_CARD_VIDEO=5;
    public static final int ITEM_BANNER=6;
    public static final int ITEM_VIDEO_HEADER=7;
    //视频
    public static final int ITEM_NOIMAL=8;
    //基本数据
    private int adIndex;
    //条目类型
    //squareCardCollection
    private String type;
    //本地区分条目类型
    private int itemType;

    //详细元素
    private long id;
    private boolean ad;
    private long date;
    private long duration;
    private long idx;
    private long releaseTime;

    private int searchWeight;
    private int count;
    private OpenEyesHeader header;
    private OpenEyesContent content;
    private OpenEyesIndexItemBean data;
    private List<OpenEyesIndexItemBean> itemList;
    private String actionUrl;
    private String text;


    private String category;
    private boolean collected;
    private boolean ifLimitVideo;
    private boolean played;
    private boolean autoPlay;
    private boolean shade;
    private String dataType;
    private String remark;
    private String resourceType;
    private String slogan;
    private String description;
    private String descriptionEditor;
    private String library;
    private String playUrl;
    private String title;
    //广告的
    private String image;

    private List<OpenEyesPlayInfo> playInfo;
    private List<OpenEyesTag> tags;

    private OpenEyesAuthor author;
    private Consumption consumption;
    private Cover cover;
    private Provider provider;
    private WebUrl webUrl;

    private VideoParams videoParams;



    public int getItemType() {
        if(TextUtils.isEmpty(type)){
            itemType=ITEM_UNKNOWN;
        }else if("squareCardCollection".equals(type)){
            itemType=ITEM_CARD;
        }else if("textCard".equals(type)){
            itemType=ITEM_TITLE;
        }else if("followCard".equals(type)){
            itemType=ITEM_FOLLOW;
        }else if("videoSmallCard".equals(type)){
            itemType=ITEM_VIDEO;
        }else if("banner2".equals(type)||"banner".equals(type)){
            itemType=ITEM_BANNER;
        }else if("video".equals(type)){
            //CARD video
            itemType=ITEM_CARD_VIDEO;
        }else if(VideoConstants.VIDEO_HEADER.equals(type)){
            itemType=ITEM_VIDEO_HEADER;
        }else if("NORMAL".equals(type)){
            itemType=ITEM_NOIMAL;
        }
        return itemType;
    }

    public VideoParams getVideoParams() {
        return videoParams;
    }

    public void setVideoParams(VideoParams videoParams) {
        this.videoParams = videoParams;
    }

    public int getAdIndex() {
        return adIndex;
    }

    public void setAdIndex(int adIndex) {
        this.adIndex = adIndex;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isAd() {
        return ad;
    }

    public void setAd(boolean ad) {
        this.ad = ad;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getIdx() {
        return idx;
    }

    public void setIdx(long idx) {
        this.idx = idx;
    }

    public long getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(long releaseTime) {
        this.releaseTime = releaseTime;
    }

    public int getSearchWeight() {
        return searchWeight;
    }

    public void setSearchWeight(int searchWeight) {
        this.searchWeight = searchWeight;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public OpenEyesHeader getHeader() {
        return header;
    }

    public void setHeader(OpenEyesHeader header) {
        this.header = header;
    }

    public OpenEyesContent getContent() {
        return content;
    }

    public void setContent(OpenEyesContent content) {
        this.content = content;
    }

    public OpenEyesIndexItemBean getData() {
        return data;
    }

    public void setData(OpenEyesIndexItemBean data) {
        this.data = data;
    }

    public List<OpenEyesIndexItemBean> getItemList() {
        return itemList;
    }

    public void setItemList(List<OpenEyesIndexItemBean> itemList) {
        this.itemList = itemList;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public boolean isIfLimitVideo() {
        return ifLimitVideo;
    }

    public void setIfLimitVideo(boolean ifLimitVideo) {
        this.ifLimitVideo = ifLimitVideo;
    }

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public boolean isShade() {
        return shade;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionEditor() {
        return descriptionEditor;
    }

    public void setDescriptionEditor(String descriptionEditor) {
        this.descriptionEditor = descriptionEditor;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<OpenEyesPlayInfo> getPlayInfo() {
        return playInfo;
    }

    public void setPlayInfo(List<OpenEyesPlayInfo> playInfo) {
        this.playInfo = playInfo;
    }

    public List<OpenEyesTag> getTags() {
        return tags;
    }

    public void setTags(List<OpenEyesTag> tags) {
        this.tags = tags;
    }

    public OpenEyesAuthor getAuthor() {
        return author;
    }

    public void setAuthor(OpenEyesAuthor author) {
        this.author = author;
    }

    public Consumption getConsumption() {
        return consumption;
    }

    public void setConsumption(Consumption consumption) {
        this.consumption = consumption;
    }

    public Cover getCover() {
        return cover;
    }

    public void setCover(Cover cover) {
        this.cover = cover;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public WebUrl getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(WebUrl webUrl) {
        this.webUrl = webUrl;
    }

    public class Consumption{

        /**
         * collectionCount : 213
         * replyCount : 4
         * shareCount : 51
         */

        private int collectionCount;
        private int replyCount;
        private int shareCount;

        public int getCollectionCount() {
            return collectionCount;
        }

        public void setCollectionCount(int collectionCount) {
            this.collectionCount = collectionCount;
        }

        public int getReplyCount() {
            return replyCount;
        }

        public void setReplyCount(int replyCount) {
            this.replyCount = replyCount;
        }

        public int getShareCount() {
            return shareCount;
        }

        public void setShareCount(int shareCount) {
            this.shareCount = shareCount;
        }
    }


    public class Cover{

        /**
         * blurred : http://img.kaiyanapp.com/3caf5628a7f4ea525949225715376182.png?imageMogr2/quality/60/format/jpg
         * detail : http://img.kaiyanapp.com/7b6399dbc663ac20dcaab9fe58b140ff.png?imageMogr2/quality/60/format/jpg
         * feed : http://img.kaiyanapp.com/7b6399dbc663ac20dcaab9fe58b140ff.png?imageMogr2/quality/60/format/jpg
         * homepage : http://img.kaiyanapp.com/7b6399dbc663ac20dcaab9fe58b140ff.png?imageView2/1/w/720/h/560/format/jpg/q/75|watermark/1/image/aHR0cDovL2ltZy5rYWl5YW5hcHAuY29tL2JsYWNrXzMwLnBuZw==/dissolve/100/gravity/Center/dx/0/dy/0|imageslim
         */

        private String blurred;
        private String detail;
        private String feed;
        private String homepage;

        public String getBlurred() {
            return blurred;
        }

        public void setBlurred(String blurred) {
            this.blurred = blurred;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getFeed() {
            return feed;
        }

        public void setFeed(String feed) {
            this.feed = feed;
        }

        public String getHomepage() {
            return homepage;
        }

        public void setHomepage(String homepage) {
            this.homepage = homepage;
        }
    }

    public class Provider{

        /**
         * alias : youtube
         * icon : http://img.kaiyanapp.com/fa20228bc5b921e837156923a58713f6.png
         * name : YouTube
         */

        private String alias;
        private String icon;
        private String name;

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public class WebUrl{

        /**
         * forWeibo : http://www.eyepetizer.net/detail.html?vid=155005&resourceType=video&utm_campaign=routine&utm_medium=share&utm_source=weibo&uid=0
         * raw : http://www.eyepetizer.net/detail.html?vid=155005
         */

        private String forWeibo;
        private String raw;

        public String getForWeibo() {
            return forWeibo;
        }

        public void setForWeibo(String forWeibo) {
            this.forWeibo = forWeibo;
        }

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }
    }
}