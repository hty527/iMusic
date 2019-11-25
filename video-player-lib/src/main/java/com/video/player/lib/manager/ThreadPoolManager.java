package com.video.player.lib.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by TinyHung@outlook.com
 * 2019/11/7
 * 线程管理
 */

public class ThreadPoolManager {

    private static ThreadPoolManager mInstance;
    private ExecutorService mCachedThreadPool;

    public static synchronized ThreadPoolManager getInstance() {
        synchronized (ThreadPoolManager.class) {
            if (null == mInstance) {
                mInstance = new ThreadPoolManager();
            }
        }
        return mInstance;
    }

    public void run(Runnable runnable){
        if(null==mCachedThreadPool){
            //newCachedThreadPool：创建一个可缓存线程池，如果线程池长度超过处理需求，可以灵活回收空闲线程，若无可回收则新建线程
            //newFixedThreadPool：创建一个定长线程池，可以控制线程最大并发数，超过的线程会在队列中等待
            //newScheduledThreadPool：创建一个定长线程池，支持定时及周期性任务执行
            //newSingleThreadExecutor：创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序执行
            mCachedThreadPool = Executors.newSingleThreadExecutor();
        }
        mCachedThreadPool.execute(runnable);
    }

    public void stop(){
        try {
            if(null!=mCachedThreadPool){
                mCachedThreadPool.shutdown();
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    public void reset(){
        try {
            if(null!=mCachedThreadPool){
                mCachedThreadPool.shutdown();
                mCachedThreadPool=null;
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }
}