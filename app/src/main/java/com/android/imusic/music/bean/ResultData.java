package com.android.imusic.music.bean;

/**
 * TinyHung@Outlook.com
 * 2019/3/13
 */

public class ResultData<T> {

    private T data;
    private int code;//200
    private String msg;
    //酷狗API
    private int status;//1
    private int errcode;
    private String error;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ResultData{" +
                "data=" + data +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", status=" + status +
                ", errcode=" + errcode +
                ", error='" + error + '\'' +
                '}';
    }
}