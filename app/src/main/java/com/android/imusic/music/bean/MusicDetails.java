package com.android.imusic.music.bean;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/23
 */

public class MusicDetails {

    public static final int ITEM_ID_NEXT_PLAY = 1;
    public static final int ITEM_ID_SHARE = 2;
    public static final int ITEM_ID_DETELE = 3;
    public static final int ITEM_ID_COLLECT = 4;
    private long id;
    private int icon;
    private int itemID;
    private String title;
    private String path;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
