package com.music.player.lib.adapter.base;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.music.player.lib.util.Logger;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/24
 * Load More
 */

public abstract class OnLoadMoreListener extends RecyclerView.OnScrollListener implements OnLoadMorePresenter{

    private static final String TAG = "OnLoadMoreListener";
    //是否正在加载更多中
    private boolean isLoadingMore=false;
    //用来标记是否正在向最后一个滑动
    boolean isSlidingToLast = false;

    public abstract void onLoadMore();

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){
            LinearLayoutManager layoutManager= (LinearLayoutManager) recyclerView.getLayoutManager();
            // 当停止滑动时
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //获取最后一个完全显示的ItemPosition ,角标值
                int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                //所有条目,数量值
                int totalItemCount = layoutManager.getItemCount();
                // 判断是否滚动到底部，并且是向右滚动
                if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
                    if(!isLoadingMore){
                        isLoadingMore=true;
                        OnLoadMoreListener.this.onLoadMore();
                    }
                }
            }
        }else if(recyclerView.getLayoutManager() instanceof GridLayoutManager){
            GridLayoutManager layoutManager= (GridLayoutManager) recyclerView.getLayoutManager();
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();
                if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
                    if(!isLoadingMore){
                        isLoadingMore=true;
                        OnLoadMoreListener.this.onLoadMore();
                    }
                }
            }
        }else{
            //暂不支持瀑布流加载更多
            Logger.d(TAG,"OnLoadMoreListener-->暂不支持瀑布流加载更多");
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (dy > 0) {
            isSlidingToLast = true;
        } else {
            isSlidingToLast = false;
        }
    }

    /**
     * 加载成功
     */
    @Override
    public void onLoadComplete() {
        isLoadingMore=false;
    }

    /**
     * 加载完成
     */
    @Override
    public void onLoadEnd() {
        isLoadingMore=false;
    }

    /**
     * 加载失败
     */
    @Override
    public void onLoadError() {
        isLoadingMore=false;
    }
}