package com.android.imusic.music.bean;

import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 */

public class SearchResult {

    /**
     * allowerr : 0
     * correctiontip :
     * correctiontype : 0
     * forcecorrection : 0
     * istag : 0
     * istagresult : 0
     * tab : 全部
     * timestamp : 1553308684
     * total : 446
     */

    private int allowerr;
    private String correctiontip;
    private int correctiontype;
    private int forcecorrection;
    private int istag;
    private int istagresult;
    private String tab;
    private int timestamp;
    private int total;
    List<SearchResultInfo> info;

    public int getAllowerr() {
        return allowerr;
    }

    public void setAllowerr(int allowerr) {
        this.allowerr = allowerr;
    }

    public String getCorrectiontip() {
        return correctiontip;
    }

    public void setCorrectiontip(String correctiontip) {
        this.correctiontip = correctiontip;
    }

    public int getCorrectiontype() {
        return correctiontype;
    }

    public void setCorrectiontype(int correctiontype) {
        this.correctiontype = correctiontype;
    }

    public int getForcecorrection() {
        return forcecorrection;
    }

    public void setForcecorrection(int forcecorrection) {
        this.forcecorrection = forcecorrection;
    }

    public int getIstag() {
        return istag;
    }

    public void setIstag(int istag) {
        this.istag = istag;
    }

    public int getIstagresult() {
        return istagresult;
    }

    public void setIstagresult(int istagresult) {
        this.istagresult = istagresult;
    }

    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<SearchResultInfo> getInfo() {
        return info;
    }

    public void setInfo(List<SearchResultInfo> info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "allowerr=" + allowerr +
                ", correctiontip='" + correctiontip + '\'' +
                ", correctiontype=" + correctiontype +
                ", forcecorrection=" + forcecorrection +
                ", istag=" + istag +
                ", istagresult=" + istagresult +
                ", tab='" + tab + '\'' +
                ", timestamp=" + timestamp +
                ", total=" + total +
                ", info=" + info +
                '}';
    }
}
