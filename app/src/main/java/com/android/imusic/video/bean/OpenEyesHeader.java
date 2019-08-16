package com.android.imusic.video.bean;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 */

public class OpenEyesHeader {

    /**
     * actionUrl : eyepetizer://feed?tabIndex=2
     * font : bigBold
     * id : 5
     * rightText : 查看往期
     * subTitle : MONDAY, APRIL 8
     * subTitleFont : lobster
     * textAlign : left
     * title : 开眼编辑精选
     */

    private String actionUrl;
    private String font;
    private int id;
    private String rightText;
    private String subTitle;
    private String subTitleFont;
    private String textAlign;
    private String title;

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getSubTitleFont() {
        return subTitleFont;
    }

    public void setSubTitleFont(String subTitleFont) {
        this.subTitleFont = subTitleFont;
    }

    public String getTextAlign() {
        return textAlign;
    }

    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
