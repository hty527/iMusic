package com.music.player.lib.adapter.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/23
 * BaseAdapter T：数据类型，V：ViewHolder
 */

public abstract class BaseAdapter<T,V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {

    protected static final String TAG = "BaseAdapter";
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected List<T> mData;

    public BaseAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    public BaseAdapter(Context context,List<T> data){
        mInflater = LayoutInflater.from(context);
        this.mContext=context;
        this.mData=data;
    }

    @Override
    public V onCreateViewHolder(ViewGroup parent, int viewType) {
        if(null==mContext){
            mContext=parent.getContext();
        }
        return inCreateViewHolder(parent,viewType);
    }

    @Override
    public void onBindViewHolder(V holder, final int position) {
        if(getData().size()>0){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null!=mOnItemClickListener){
                        mOnItemClickListener.onItemClick(v,position,getItemId(position));
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(null!=mOnLongItemClickListener){
                        mOnLongItemClickListener.onItemLongClick(v,position,getItemId(position));
                        return true;
                    }
                    return false;
                }
            });
            inBindViewHolder(holder,position);
        }
    }

    @Override
    public void onBindViewHolder(V holder, final int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if(getData().size()>0){
            if(payloads.isEmpty()){
                onBindViewHolder(holder,position);
            }else{
                inBindViewHolder(holder,position,payloads);
            }
        }
    }

    @Override
    public int getItemCount() {
        return null==mData?0:mData.size();
    }

    public Context getContext(){
        return mContext;
    }

    /**
     * ViewHolder初始化
     * @param viewGroup
     * @param viewType
     * @return
     */
    public abstract V inCreateViewHolder(ViewGroup viewGroup,int viewType);

    /**
     * 全量刷新
     * @param viewHolder
     * @param position
     */
    public abstract void inBindViewHolder(V viewHolder,int position);

    /**
     * 局部刷新
     * @param viewHolder
     * @param position
     * @param payloads
     */
    protected void inBindViewHolder(V viewHolder,int position,List<Object> payloads){}

    /**
     * 返回数据集中的元素
     * @param position 列表位置
     * @return
     */
    protected T getItemData(int position){
        if(null!=mData&&mData.size()>position){
            return mData.get(position);
        }
        return null;
    }

    /**
     * 返回列表数据集
     * @return
     */
    public List<T> getData(){
        if(null==mData){
            mData=new ArrayList<>();
        }
        return mData;
    }

    /**
     * 更新适配器
     * @param data
     */
    public void setNewData(List<T> data){
        if(null==mData){
            mData=new ArrayList<>();
        }
        mData.clear();
        if(null!=data&&data.size()>0){
            mData.addAll(data);
            notifyDataSetChanged();
            return;
        }
        notifyDataSetChanged();
    }

    /**
     * 追加一个数据集
     * @param data
     */
    public void addData(List<T> data){
        if(null==mData){
            mData=new ArrayList<>();
        }
        mData.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * 追加一个元素至底部
     * @param data
     */
    public void addData(T data){
        if(null==mData){
            mData=new ArrayList<>();
        }
        mData.add(data);
        notifyDataSetChanged();
    }

    /**
     * 向顶部添加一个元素
     * @param data
     */
    public void addDataToTop(T data){
        if(null==mData){
            mData=new ArrayList<>();
        }
        mData.add(0,data);
        notifyDataSetChanged();
    }

    /**
     * 对应生命周期调用
     */
    public void onDestroy() {
        mInflater=null;
        if(null!=mData){
            mData.clear();
            mData=null;
            notifyDataSetChanged();
        }
        mContext=null;mOnItemClickListener=null;
    }

    /**
     * ITEM CLICK
     */
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * ITEM LONG CLICK
     */
    private OnItemLongClickListener mOnLongItemClickListener;

    public void setOnLongItemClickListener(OnItemLongClickListener onLongItemClickListener) {
        mOnLongItemClickListener = onLongItemClickListener;
    }

    /**
     * LOAD MORE LISTSNER
     */
    private OnLoadMoreListener mLoadMoreListener;

    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener,RecyclerView recyclerView) {
        if(null==loadMoreListener){
            recyclerView.removeOnScrollListener(mLoadMoreListener);
        }
        mLoadMoreListener = loadMoreListener;
        if(null!=mLoadMoreListener){
            recyclerView.addOnScrollListener(mLoadMoreListener);
        }
    }

    /**
     * 加载成功
     */
    public void onLoadComplete() {
        if(null!=mLoadMoreListener){
            mLoadMoreListener.onLoadComplete();
        }
    }

    /**
     * 加载完成
     */
    public void onLoadEnd() {
        if(null!=mLoadMoreListener){
            mLoadMoreListener.onLoadEnd();
        }
    }

    /**
     * 加载失败
     */
    public void onLoadError() {
        if(null!=mLoadMoreListener){
            mLoadMoreListener.onLoadError();
        }
    }

    /**
     * 设置占位布局
     * @param emptyView
     */
    public void setEmptyView(View emptyView) {
    }

    private class ExpandViewHolder extends RecyclerView.ViewHolder {

        public ExpandViewHolder(View emptyView) {
            super(emptyView);
        }
    }
}