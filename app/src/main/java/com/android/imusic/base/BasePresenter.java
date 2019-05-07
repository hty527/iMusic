package com.android.imusic.base;

import com.android.imusic.music.net.MusicNetUtils;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * MVP Base P
 */

public abstract class BasePresenter<V extends BaseContract.BaseView,M extends MusicNetUtils>
        implements BaseContract.BasePresenter<V>{

    //MVP中的V
    protected V mView;
    //MVP中的M
    protected M mNetEngin;

    /**
     * 初始化构造
     */
    protected BasePresenter(){
        mNetEngin=createEngin();
    }

    /**
     * 子类必须实例化M
     * @return 泛型M
     */
    protected abstract M createEngin();

    /**
     * 是否正在请求中
     * @return true:请求中，false:未工作
     */
    public boolean isRequsting() {
        if(null!=mNetEngin){
            return mNetEngin.isRequsting();
        }
        return false;
    }

    /**
     * V和P之间关系绑定
     * @param view mvp中的v
     */
    @Override
    public void attachView(V view) {
        this.mView=view;
    }

    /**
     * V和P之间关系解绑
     */
    @Override
    public void detachView() {
        mView=null;
        if(null!=mNetEngin){
            mNetEngin.onDestroy();
            mNetEngin=null;
        }
    }
}