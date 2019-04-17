package com.android.imusic.music.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 * 搜索结果
 */

public class SearchResultInfo {

    /**
     * 320filesize : 8608960
     * 320hash : 2398ddfc7c7420ab22a77dfeae4ca551
     * 320privilege : 10
     * Accompany : 1
     * album_audio_id : 39612569
     * album_id : 1645030
     * album_name : 周杰伦的床边故事
     * audio_id : 22084042
     * bitrate : 128
     * duration : 215
     * extname : mp3
     * fail_process : 4
     * fail_process_320 : 4
     * fail_process_sq : 4
     * feetype : 0
     * filename : 周杰伦 - 告白气球
     * filesize : 3443771
     * fold_type : 0
     * group : []
     * hash : 5fce4cbcb96d6025033bce2025fc3943
     * isnew : 0
     * isoriginal : 0
     * m4afilesize : 880014
     * mvhash : aff3b6219c15d030f957b82ff50aa91e
     * old_cpy : 0
     * othername :
     * othername_original :
     * ownercount : 110685
     * pay_type : 3
     * pay_type_320 : 3
     * pay_type_sq : 3
     * pkg_price : 1
     * pkg_price_320 : 1
     * pkg_price_sq : 1
     * price : 200
     * price_320 : 200
     * price_sq : 200
     * privilege : 8
     * rp_publish : 1
     * rp_type : audio
     * singername : 周杰伦
     * songname : 告白气球
     * songname_original : 告白气球
     * source :
     * sourceid : 0
     * sqfilesize : 25058814
     * sqhash : b2c0a23919eee8b47831ffaa2604107f
     * sqprivilege : 10
     * srctype : 1
     * topic :
     * topic_url :
     */

    @SerializedName("320filesize")
    private int value320filesize;
    @SerializedName("320hash")
    private String value320hash;
    @SerializedName("320privilege")
    private int value320privilege;
    private int Accompany;
    private int album_audio_id;
    private String album_id;
    private String album_name;
    private String album_img;
    private long audio_id;
    private int bitrate;
    private int duration;
    private String extname;
    private int fail_process;
    private int fail_process_320;
    private int fail_process_sq;
    private int feetype;
    private String filename;
    private int filesize;
    private int fold_type;
    private String hash;
    private int isnew;
    private int isoriginal;
    private int m4afilesize;
    private String mvhash;
    private int old_cpy;
    private String othername;
    private String othername_original;
    private int ownercount;
    private int pay_type;
    private int pay_type_320;
    private int pay_type_sq;
    private int pkg_price;
    private int pkg_price_320;
    private int pkg_price_sq;
    private int price;
    private int price_320;
    private int price_sq;
    private int privilege;
    private int rp_publish;
    private String rp_type;
    private String singername;
    private String songname;
    private String songname_original;
    private String source;
    private int sourceid;
    private int sqfilesize;
    private String sqhash;
    private int sqprivilege;
    private int srctype;
    private String topic;
    private String topic_url;
    private long id;
    /**
     * cid : 5678206
     * display : 0
     * display_rate : 0
     * hash_offset : {"end_byte":960129,"end_ms":60000,"file_type":0,"offset_hash":"A635FEFCF2F1831CA1F53A9508A9777C","start_byte":0,"start_ms":0}
     * musicpack_advance : 0
     * pay_block_tpl : 1
     * roaming_astrict : 0
     */

    private SearchResulParam trans_param;
    private List<SearchResultInfo> group;
    //是否正在播放
    private boolean selected;

    public String getAlbum_img() {
        return album_img;
    }

    public void setAlbum_img(String album_img) {
        this.album_img = album_img;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getValue320filesize() {
        return value320filesize;
    }

    public void setValue320filesize(int value320filesize) {
        this.value320filesize = value320filesize;
    }

    public String getValue320hash() {
        return value320hash;
    }

    public void setValue320hash(String value320hash) {
        this.value320hash = value320hash;
    }

    public int getValue320privilege() {
        return value320privilege;
    }

    public void setValue320privilege(int value320privilege) {
        this.value320privilege = value320privilege;
    }

    public int getAccompany() {
        return Accompany;
    }

    public void setAccompany(int Accompany) {
        this.Accompany = Accompany;
    }

    public int getAlbum_audio_id() {
        return album_audio_id;
    }

    public void setAlbum_audio_id(int album_audio_id) {
        this.album_audio_id = album_audio_id;
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public long getAudio_id() {
        return audio_id;
    }

    public void setAudio_id(long audio_id) {
        this.audio_id = audio_id;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getExtname() {
        return extname;
    }

    public void setExtname(String extname) {
        this.extname = extname;
    }

    public int getFail_process() {
        return fail_process;
    }

    public void setFail_process(int fail_process) {
        this.fail_process = fail_process;
    }

    public int getFail_process_320() {
        return fail_process_320;
    }

    public void setFail_process_320(int fail_process_320) {
        this.fail_process_320 = fail_process_320;
    }

    public int getFail_process_sq() {
        return fail_process_sq;
    }

    public void setFail_process_sq(int fail_process_sq) {
        this.fail_process_sq = fail_process_sq;
    }

    public int getFeetype() {
        return feetype;
    }

    public void setFeetype(int feetype) {
        this.feetype = feetype;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public int getFold_type() {
        return fold_type;
    }

    public void setFold_type(int fold_type) {
        this.fold_type = fold_type;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getIsnew() {
        return isnew;
    }

    public void setIsnew(int isnew) {
        this.isnew = isnew;
    }

    public int getIsoriginal() {
        return isoriginal;
    }

    public void setIsoriginal(int isoriginal) {
        this.isoriginal = isoriginal;
    }

    public int getM4afilesize() {
        return m4afilesize;
    }

    public void setM4afilesize(int m4afilesize) {
        this.m4afilesize = m4afilesize;
    }

    public String getMvhash() {
        return mvhash;
    }

    public void setMvhash(String mvhash) {
        this.mvhash = mvhash;
    }

    public int getOld_cpy() {
        return old_cpy;
    }

    public void setOld_cpy(int old_cpy) {
        this.old_cpy = old_cpy;
    }

    public String getOthername() {
        return othername;
    }

    public void setOthername(String othername) {
        this.othername = othername;
    }

    public String getOthername_original() {
        return othername_original;
    }

    public void setOthername_original(String othername_original) {
        this.othername_original = othername_original;
    }

    public int getOwnercount() {
        return ownercount;
    }

    public void setOwnercount(int ownercount) {
        this.ownercount = ownercount;
    }

    public int getPay_type() {
        return pay_type;
    }

    public void setPay_type(int pay_type) {
        this.pay_type = pay_type;
    }

    public int getPay_type_320() {
        return pay_type_320;
    }

    public void setPay_type_320(int pay_type_320) {
        this.pay_type_320 = pay_type_320;
    }

    public int getPay_type_sq() {
        return pay_type_sq;
    }

    public void setPay_type_sq(int pay_type_sq) {
        this.pay_type_sq = pay_type_sq;
    }

    public int getPkg_price() {
        return pkg_price;
    }

    public void setPkg_price(int pkg_price) {
        this.pkg_price = pkg_price;
    }

    public int getPkg_price_320() {
        return pkg_price_320;
    }

    public void setPkg_price_320(int pkg_price_320) {
        this.pkg_price_320 = pkg_price_320;
    }

    public int getPkg_price_sq() {
        return pkg_price_sq;
    }

    public void setPkg_price_sq(int pkg_price_sq) {
        this.pkg_price_sq = pkg_price_sq;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPrice_320() {
        return price_320;
    }

    public void setPrice_320(int price_320) {
        this.price_320 = price_320;
    }

    public int getPrice_sq() {
        return price_sq;
    }

    public void setPrice_sq(int price_sq) {
        this.price_sq = price_sq;
    }

    public int getPrivilege() {
        return privilege;
    }

    public void setPrivilege(int privilege) {
        this.privilege = privilege;
    }

    public int getRp_publish() {
        return rp_publish;
    }

    public void setRp_publish(int rp_publish) {
        this.rp_publish = rp_publish;
    }

    public String getRp_type() {
        return rp_type;
    }

    public void setRp_type(String rp_type) {
        this.rp_type = rp_type;
    }

    public String getSingername() {
        return singername;
    }

    public void setSingername(String singername) {
        this.singername = singername;
    }

    public String getSongname() {
        return songname;
    }

    public void setSongname(String songname) {
        this.songname = songname;
    }

    public String getSongname_original() {
        return songname_original;
    }

    public void setSongname_original(String songname_original) {
        this.songname_original = songname_original;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getSourceid() {
        return sourceid;
    }

    public void setSourceid(int sourceid) {
        this.sourceid = sourceid;
    }

    public int getSqfilesize() {
        return sqfilesize;
    }

    public void setSqfilesize(int sqfilesize) {
        this.sqfilesize = sqfilesize;
    }

    public String getSqhash() {
        return sqhash;
    }

    public void setSqhash(String sqhash) {
        this.sqhash = sqhash;
    }

    public int getSqprivilege() {
        return sqprivilege;
    }

    public void setSqprivilege(int sqprivilege) {
        this.sqprivilege = sqprivilege;
    }

    public int getSrctype() {
        return srctype;
    }

    public void setSrctype(int srctype) {
        this.srctype = srctype;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic_url() {
        return topic_url;
    }

    public void setTopic_url(String topic_url) {
        this.topic_url = topic_url;
    }

    public SearchResulParam getTrans_param() {
        return trans_param;
    }

    public void setTrans_param(SearchResulParam trans_param) {
        this.trans_param = trans_param;
    }

    public List<SearchResultInfo> getGroup() {
        return group;
    }

    public void setGroup(List<SearchResultInfo> group) {
        this.group = group;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "SearchResultInfo{" +
                "value320filesize=" + value320filesize +
                ", value320hash='" + value320hash + '\'' +
                ", value320privilege=" + value320privilege +
                ", Accompany=" + Accompany +
                ", album_audio_id=" + album_audio_id +
                ", album_id='" + album_id + '\'' +
                ", album_name='" + album_name + '\'' +
                ", album_img='" + album_img + '\'' +
                ", audio_id=" + audio_id +
                ", bitrate=" + bitrate +
                ", duration=" + duration +
                ", extname='" + extname + '\'' +
                ", fail_process=" + fail_process +
                ", fail_process_320=" + fail_process_320 +
                ", fail_process_sq=" + fail_process_sq +
                ", feetype=" + feetype +
                ", filename='" + filename + '\'' +
                ", filesize=" + filesize +
                ", fold_type=" + fold_type +
                ", hash='" + hash + '\'' +
                ", isnew=" + isnew +
                ", isoriginal=" + isoriginal +
                ", m4afilesize=" + m4afilesize +
                ", mvhash='" + mvhash + '\'' +
                ", old_cpy=" + old_cpy +
                ", othername='" + othername + '\'' +
                ", othername_original='" + othername_original + '\'' +
                ", ownercount=" + ownercount +
                ", pay_type=" + pay_type +
                ", pay_type_320=" + pay_type_320 +
                ", pay_type_sq=" + pay_type_sq +
                ", pkg_price=" + pkg_price +
                ", pkg_price_320=" + pkg_price_320 +
                ", pkg_price_sq=" + pkg_price_sq +
                ", price=" + price +
                ", price_320=" + price_320 +
                ", price_sq=" + price_sq +
                ", privilege=" + privilege +
                ", rp_publish=" + rp_publish +
                ", rp_type='" + rp_type + '\'' +
                ", singername='" + singername + '\'' +
                ", songname='" + songname + '\'' +
                ", songname_original='" + songname_original + '\'' +
                ", source='" + source + '\'' +
                ", sourceid=" + sourceid +
                ", sqfilesize=" + sqfilesize +
                ", sqhash='" + sqhash + '\'' +
                ", sqprivilege=" + sqprivilege +
                ", srctype=" + srctype +
                ", topic='" + topic + '\'' +
                ", topic_url='" + topic_url + '\'' +
                ", trans_param=" + trans_param +
                ", group=" + group +
                ", selected=" + selected +
                ", id=" + id +
                '}';
    }
}
