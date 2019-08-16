package com.android.imusic.music.bean;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 */

public class SearchResulParam {

    /**
     * cid : 5678206
     * display : 0
     * display_rate : 0
     * hash_offset : {"end_byte":960129,"end_ms":60000,"file_type":0,"offset_hash":"A635FEFCF2F1831CA1F53A9508A9777C","start_byte":0,"start_ms":0}
     * musicpack_advance : 0
     * pay_block_tpl : 1
     * roaming_astrict : 0
     */

    private int cid;
    private int display;
    private int display_rate;
    /**
     * end_byte : 960129
     * end_ms : 60000
     * file_type : 0
     * offset_hash : A635FEFCF2F1831CA1F53A9508A9777C
     * start_byte : 0
     * start_ms : 0
     */

    private HashOffsetBean hash_offset;
    private int musicpack_advance;
    private int pay_block_tpl;
    private int roaming_astrict;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getDisplay() {
        return display;
    }

    public void setDisplay(int display) {
        this.display = display;
    }

    public int getDisplay_rate() {
        return display_rate;
    }

    public void setDisplay_rate(int display_rate) {
        this.display_rate = display_rate;
    }

    public HashOffsetBean getHash_offset() {
        return hash_offset;
    }

    public void setHash_offset(HashOffsetBean hash_offset) {
        this.hash_offset = hash_offset;
    }

    public int getMusicpack_advance() {
        return musicpack_advance;
    }

    public void setMusicpack_advance(int musicpack_advance) {
        this.musicpack_advance = musicpack_advance;
    }

    public int getPay_block_tpl() {
        return pay_block_tpl;
    }

    public void setPay_block_tpl(int pay_block_tpl) {
        this.pay_block_tpl = pay_block_tpl;
    }

    public int getRoaming_astrict() {
        return roaming_astrict;
    }

    public void setRoaming_astrict(int roaming_astrict) {
        this.roaming_astrict = roaming_astrict;
    }

    public static class HashOffsetBean {
        private int end_byte;
        private int end_ms;
        private int file_type;
        private String offset_hash;
        private int start_byte;
        private int start_ms;

        public int getEnd_byte() {
            return end_byte;
        }

        public void setEnd_byte(int end_byte) {
            this.end_byte = end_byte;
        }

        public int getEnd_ms() {
            return end_ms;
        }

        public void setEnd_ms(int end_ms) {
            this.end_ms = end_ms;
        }

        public int getFile_type() {
            return file_type;
        }

        public void setFile_type(int file_type) {
            this.file_type = file_type;
        }

        public String getOffset_hash() {
            return offset_hash;
        }

        public void setOffset_hash(String offset_hash) {
            this.offset_hash = offset_hash;
        }

        public int getStart_byte() {
            return start_byte;
        }

        public void setStart_byte(int start_byte) {
            this.start_byte = start_byte;
        }

        public int getStart_ms() {
            return start_ms;
        }

        public void setStart_ms(int start_ms) {
            this.start_ms = start_ms;
        }
    }

    @Override
    public String toString() {
        return "SearchResulParam{" +
                "cid=" + cid +
                ", display=" + display +
                ", display_rate=" + display_rate +
                ", hash_offset=" + hash_offset +
                ", musicpack_advance=" + musicpack_advance +
                ", pay_block_tpl=" + pay_block_tpl +
                ", roaming_astrict=" + roaming_astrict +
                '}';
    }
}
