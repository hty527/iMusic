package com.android.imusic.music.bean;

import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/23
 * 播放信息
 */

public class SearchMusicData {

    /**
     * album_id : 14275743
     * album_name : “用奋斗点亮幸福”江苏卫视2019跨年演唱会
     * audio_name : 毛不易 - 像我这样的人 (Live)
     * author_id : 722869
     * author_name : 毛不易
     * authors : [{"author_id":"722869","author_name":"毛不易","avatar":"http://singerimg.kugou.com/uploadpic/softhead/400/20180611/20180611160019456.jpg","is_publish":"1","sizable_avatar":"http://singerimg.kugou.com/uploadpic/softhead/{size}/20180611/20180611160019456.jpg"}]
     * bitrate : 128
     * filesize : 2657456
     * hash : 67f4b520ee80d68959f4bf8a213f6774
     * have_album : 1
     * have_mv : 0
     * img : http://imge.kugou.com/stdmusic/20181231/20181231214942859068.jpg
     * lyrics : [00:00.42]毛不易 - 像我这样的人(Live) [00:02.77]作词：毛不易 [00:04.05]作曲：毛不易 [00:15.46]像我这样优秀的人 [00:19.50]本该灿烂过一生 [00:23.14]怎么二十多年到头来 [00:27.12]还在人海里浮沉 [00:31.01]像我这样聪明的人 [00:34.85]早就告别了单纯 [00:38.63]怎么还是用了一段情 [00:42.57]去换一身伤痕 [00:46.51]像我这样迷茫的人 [00:50.35]像我这样寻找的人 [00:54.29]像我这样碌碌无为的人 [00:58.17]你还见过多少人 [01:21.42]像我这样庸俗的人 [01:25.22]从不喜欢装深沉 [01:28.95]怎么偶尔听到老歌时 [01:32.87]忽然也晃了神 [01:36.77]像我这样懦弱的人 [01:40.66]凡事都要留几分 [01:44.58]怎么曾经也会为了谁 [01:48.44]想过奋不顾身 [01:52.27]像我这样迷茫的人 [01:56.16]像我这样寻找的人 [01:59.69]像我这样碌碌无为的人 [02:04.03]你还见过多少人 [02:07.87]像我这样孤单的人 [02:11.60]像我这样傻的人 [02:15.59]像我这样不甘平凡的人 [02:19.33]世界上有多少人 [02:25.18]像我这样莫名其妙的人 [02:29.56]会不会有人心疼
     * play_url : http://fs.w.kugou.com/201903231336/164c9f4e555e51a1b7fae9e1aa5f00d8/G146/M07/02/14/cpQEAFwqEt2AMf4dACiMsITsnwk265.mp3
     * privilege : 8
     * privilege2 : 1000
     * song_name : 像我这样的人 (Live)
     * timelength : 166006
     * video_id : 0
     */

    private String album_id;
    private String album_name;
    private String audio_name;
    private String author_id;
    private String author_name;
    private int bitrate;
    private int filesize;
    private String hash;
    private int have_album;
    private int have_mv;
    private String img;
    private String lyrics;
    private String play_url;
    private int privilege;
    private String privilege2;
    private String song_name;
    private int timelength;
    private int video_id;
    /**
     * author_id : 722869
     * author_name : 毛不易
     * avatar : http://singerimg.kugou.com/uploadpic/softhead/400/20180611/20180611160019456.jpg
     * is_publish : 1
     * sizable_avatar : http://singerimg.kugou.com/uploadpic/softhead/{size}/20180611/20180611160019456.jpg
     */

    private List<SearchMusicAnchor> authors;

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

    public String getAudio_name() {
        return audio_name;
    }

    public void setAudio_name(String audio_name) {
        this.audio_name = audio_name;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getHave_album() {
        return have_album;
    }

    public void setHave_album(int have_album) {
        this.have_album = have_album;
    }

    public int getHave_mv() {
        return have_mv;
    }

    public void setHave_mv(int have_mv) {
        this.have_mv = have_mv;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getPlay_url() {
        return play_url;
    }

    public void setPlay_url(String play_url) {
        this.play_url = play_url;
    }

    public int getPrivilege() {
        return privilege;
    }

    public void setPrivilege(int privilege) {
        this.privilege = privilege;
    }

    public String getPrivilege2() {
        return privilege2;
    }

    public void setPrivilege2(String privilege2) {
        this.privilege2 = privilege2;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public int getTimelength() {
        return timelength;
    }

    public void setTimelength(int timelength) {
        this.timelength = timelength;
    }

    public int getVideo_id() {
        return video_id;
    }

    public void setVideo_id(int video_id) {
        this.video_id = video_id;
    }

    public List<SearchMusicAnchor> getAuthors() {
        return authors;
    }

    public void setAuthors(List<SearchMusicAnchor> authors) {
        this.authors = authors;
    }

    @Override
    public String toString() {
        return "SearchMusicData{" +
                "album_id='" + album_id + '\'' +
                ", album_name='" + album_name + '\'' +
                ", audio_name='" + audio_name + '\'' +
                ", author_id='" + author_id + '\'' +
                ", author_name='" + author_name + '\'' +
                ", bitrate=" + bitrate +
                ", filesize=" + filesize +
                ", hash='" + hash + '\'' +
                ", have_album=" + have_album +
                ", have_mv=" + have_mv +
                ", img='" + img + '\'' +
                ", lyrics='" + lyrics + '\'' +
                ", play_url='" + play_url + '\'' +
                ", privilege=" + privilege +
                ", privilege2='" + privilege2 + '\'' +
                ", song_name='" + song_name + '\'' +
                ", timelength=" + timelength +
                ", video_id=" + video_id +
                ", authors=" + authors +
                '}';
    }
}
