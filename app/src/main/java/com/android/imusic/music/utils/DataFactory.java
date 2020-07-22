package com.android.imusic.music.utils;

import com.android.imusic.MusicApplication;
import com.android.imusic.music.bean.AlbumInfo;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.net.bean.ResultData;
import com.android.imusic.net.bean.ResultList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by TinyHung@outlook.com
 * 2019/7/24
 */

public class DataFactory {

    private static final String TAG = "DataFactory";
    private volatile static DataFactory mInstance;

    public static synchronized DataFactory getInstance() {
        synchronized (DataFactory.class) {
            if (null == mInstance) {
                mInstance = new DataFactory();
            }
        }
        return mInstance;
    }

    /**
     * 返回首页音乐列表
     * @return
     */
    public List<AudioInfo> getIndexMusic() {
        String fromAssets = getFromAssets("index_list.json");
        ResultData<ResultList<AudioInfo>> music= new Gson().fromJson(fromAssets, new TypeToken<ResultData<ResultList<AudioInfo>>>() {}.getType());
        return music.getData().getList();
    }

    /**
     * 返回专辑音乐列表
     * @return
     */
    public AlbumInfo getMusicByAlbum(String album) {
        try {
            String fromAssets = getFromAssets(album+".json");
            ResultData<AlbumInfo> music= new Gson().fromJson(fromAssets, new TypeToken<ResultData<AlbumInfo>>() {}.getType());
            return music.getData();
        }catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 格式化资产目录下json文件
     * @param filePath
     * @return
     */
    public String getFromAssets(String filePath){
        InputStreamReader inputReader=null;
        BufferedReader bufReader=null;
        try {
            InputStream inputStream = MusicApplication.getContext().getAssets().open(filePath);
            inputReader = new InputStreamReader( inputStream);
            bufReader = new BufferedReader(inputReader);
            String line;
            String Result="";
            while((line = bufReader.readLine()) != null){
                Result += line;
            }
            bufReader.close();
            inputReader.close();
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
            if(null!=bufReader){
                try {
                    bufReader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if(null!=inputReader){
                try {
                    inputReader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }
}