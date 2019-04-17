package com.android.imusic.music.bean;

import android.text.TextUtils;
import com.music.player.lib.bean.BaseMediaInfo;

/**
 * TinyHung@Outlook.com
 * 2019/3/6
 */

public class MediaInfo extends BaseMediaInfo  {
    //Item类型
    public static final int ITEM_UNKNOWN=0;
    public static final int ITEM_DEFAULT=1;
    public static final int ITEM_TITLE=2;
    public static final int ITEM_MORE=3;
    public static final int ITEM_MUSIC=4;
    public static final int ITEM_ALBUM=5;

    //本地音乐
    public static final String TAG_LOCATION = "tag_location";
    //最近播放
    public static final String TAG_LAST_PLAYING = "tag_last_playing";
    //收藏
    public static final String TAG_COLLECT = "tag_collect";
    /**
     * 数据类别
     */
    //默认
    public static final String ITEM_CLASS_TYPE_DEFAULT="item_default";
    //专辑
    public static final String ITEM_CLASS_TYPE_ALBUM="item_album";
    //音乐
    public static final String ITEM_CLASS_TYPE_MUSIC="item_music";
    //标题
    public static final String ITEM_CLASS_TYPE_TITLE="item_title";
    //更多
    public static final String ITEM_CLASS_TYPE_MORE="item_title";

    private int itemType;

    /**
     * image : http://5b0988e595225.cdn.sohucs.com/images/20180118/4800d6c3304b45a5925d1795d75b05a2.jpeg
     * title : 周杰伦精选
     * desp :
     * tag_id : zhoujielun
     * class_enty : item_album
     */

    private Object image;
    private String title;
    private String desp;
    private String tag_id;
    private String class_enty;

    public Object getImage() {
        return image;
    }

    public void setImage(Object image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public String getClass_enty() {
        return class_enty;
    }

    public void setClass_enty(String class_enty) {
        this.class_enty = class_enty;
    }

    public int getItemType() {
        itemType=ITEM_UNKNOWN;
        if(!TextUtils.isEmpty(class_enty)){
            if(class_enty.equals(ITEM_CLASS_TYPE_ALBUM)){
                itemType=ITEM_ALBUM;
            } else if(class_enty.equals(ITEM_CLASS_TYPE_MUSIC)){
                itemType=ITEM_MUSIC;
            } else if(class_enty.equals(ITEM_CLASS_TYPE_TITLE)){
                itemType=ITEM_TITLE;
            } else if(class_enty.equals(ITEM_CLASS_TYPE_MORE)){
                itemType=ITEM_MORE;
            } else if(class_enty.equals(ITEM_CLASS_TYPE_DEFAULT)){
                itemType=ITEM_DEFAULT;
            }
        }
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
}