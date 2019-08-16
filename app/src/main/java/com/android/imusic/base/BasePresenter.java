package com.android.imusic.base;

import java.lang.ref.WeakReference;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * MVP Base P
 */

public abstract class BasePresenter<V extends BaseContract.BaseView,M extends BaseEngin>
        implements BaseContract.BasePresenter<V>{

    protected static final String TAG = "BasePresenter";
    //MVP中的V
    protected WeakReference<V> mViewRef;
    //MVP中的M
    protected WeakReference<M> mNetEnginRef;

    /**
     * 初始化构造
     */
    protected BasePresenter(){
        mNetEnginRef=new WeakReference<M>(createEngin());
    }

    /**
     * 子类必须实例化M
     * @return 泛型M
     */
    protected abstract M createEngin();

    /**
     * 返回 Date Model
     * @return DataModel
     */
    public synchronized WeakReference<M> getNetEngin(){
        if(null==mNetEnginRef||null==mNetEnginRef.get()){
            mNetEnginRef=new WeakReference<M>(createEngin());
        }
        return mNetEnginRef;
    }

    /**
     * 是否正在请求中
     * @return true:请求中，false:未工作
     */
    public boolean isRequsting() {
        if(null!=mNetEnginRef&&null!=mNetEnginRef.get()){
            return mNetEnginRef.get().isRequsting();
        }
        return false;
    }

    /**
     * V和P之间关系绑定
     * @param view mvp中的v
     */
    @Override
    public void attachView(V view) {
        this.mViewRef=new WeakReference<V>(view);
    }

    /**
     * V和P之间关系解绑
     */
    @Override
    public void detachView() {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.clear();
        }
        mViewRef=null;
        if(null!=mNetEnginRef&&null!=mNetEnginRef.get()){
            mNetEnginRef.get().onDestroy();
            mNetEnginRef.clear();
        }
        mNetEnginRef=null;
    }
}