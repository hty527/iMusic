package com.android.imusic.video.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.imusic.R;
import com.android.imusic.video.bean.OpenEyesIndexItemBean;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicJukeBoxBackgroundLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * 带有手势缩放的 Banner
 * 无限循环原理：
 * 1：PagerAdapter 中的 getCount() 方法返回 data.size()+2，意在真实的数据加上头部和尾部，使其永远触达不到边界
 * 2：PagerAdapter 中的 instantiateItem 实例化ViewGroup时，应获取真实的的position,position=position%data.size()得到真实position
 * 3：PagerAdapter 中的 finishUpdate 方法中处理滚动结束后边界交换逻辑,详见 finishUpdate 方法注释
 * 4：如果关心onPageSelected事件，还需要注意获取真实的position
 *
 */

public class TransformerVideoPager extends RelativeLayout{

    private List<OpenEyesIndexItemBean> mDataBeans;
    private MusicJukeBoxBackgroundLayout mBackgroundLayout;
    private ViewPager mViewPager;
    private TransformerViewpager mAdapter;
    private TextView mVideoIndexNum;

    public TransformerVideoPager(Context context) {
        this(context,null);
    }

    public TransformerVideoPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.video_pager_transforme,this);
        mViewPager = (ViewPager) findViewById(R.id.view_item_pager);
        //ViewPager的父容器高度确定
        RelativeLayout pagerLayout = (RelativeLayout) findViewById(R.id.re_item_pager_view);
        int screenWidth = MusicUtils.getInstance().getScreenWidth(context);
        int width = screenWidth * 8 / 10;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) pagerLayout.getLayoutParams();
        layoutParams.width=LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height=width*9/16;
        pagerLayout.setLayoutParams(layoutParams);
        //ViewPager宽度为父容器8/10，高度与父容器一致
        LayoutParams params=new LayoutParams(width, width*9/16);
        mViewPager.setLayoutParams(params);

        mAdapter = new TransformerViewpager();
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                //注意这里也需要获取真实的position
                position%=mDataBeans.size();
                setPagerData(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        //设置ViewPager切换效果
        mViewPager.setPageTransformer(true, new TransformerPageAnimation());
        mBackgroundLayout = (MusicJukeBoxBackgroundLayout) findViewById(R.id.background_view);
        mVideoIndexNum = (TextView) findViewById(R.id.view_index_num);
    }

    /**
     * 更新页签数据
     * @param position
     */
    private void setPagerData(int position) {
        if(null!=mDataBeans&&mDataBeans.size()>position) {
            //取出Card元素
            OpenEyesIndexItemBean indexItemBean = mDataBeans.get(position).getData().getContent().getData();
            mVideoIndexNum.setText((position + 1) + "/" + mDataBeans.size());
            if(null!=mBackgroundLayout){
                mBackgroundLayout.setBackgroundCover(indexItemBean.getCover().getBlurred(),100,false);
            }
        }
    }

    /**
     * 更新data
     * @param data 源数据
     * @param fiexdPosition 默认显示位置
     */
    public void setDatas(List<OpenEyesIndexItemBean> data, int fiexdPosition) {
        if(null!= mAdapter &&null!= mAdapter){
            if(null!=mDataBeans){
                mDataBeans.clear();
            }
            if(null==mDataBeans) mDataBeans=new ArrayList<>();
            mDataBeans.addAll(data);
            //在finishUpdate中实现无限循环的时，需将此值设置为data.size()+2,
            //以免finishUpdate中设置了setCurrentItem之后ViewPager的Item不加载
            mViewPager.setOffscreenPageLimit(data.size()+2);
            mAdapter.notifyDataSetChanged();
            if(data.size()>fiexdPosition){
                mViewPager.setCurrentItem(fiexdPosition);
            }
        }
        //更新Banner背景
        if(data.size()>fiexdPosition){
            setPagerData(fiexdPosition);
        }else{
            setPagerData(0);
        }
    }

    private class TransformerViewpager extends PagerAdapter {
        /**
         * 此处返回数量为真实Item数量+2，头+尾，结合finishUpdate方法达到无限循环
         * @return
         */
        @Override
        public int getCount() {
            return mDataBeans ==null?0:mDataBeans.size()+2;
        }
        //每次调用了notifyDataSetChanged都将重绘
        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position%=mDataBeans.size();
            OpenEyesIndexItemBean movieItem = mDataBeans.get(position);
            TransformerMoiveItem moiveItem=new TransformerMoiveItem(getContext());
            moiveItem.setData(movieItem);
            container.addView(moiveItem);
            return moiveItem;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if(object instanceof TransformerMoiveItem){
                TransformerMoiveItem moiveItem= (TransformerMoiveItem) object;
                moiveItem.onDestroy();
            }
            container.removeView((View)object);
        }

        /**
         * 已完全
         * @param container
         */
        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
            int currentItem = mViewPager.getCurrentItem();
            //如果已经滚动至第一个，则重新定位至data.size()+2-1的位置
            if(currentItem==0){
                currentItem=mDataBeans.size();
                //如果已经滚动至data.size()+2-1，则重新定位至第一个位置，保留左侧一个
            }else if(currentItem==(mDataBeans.size()+2-1)){
                currentItem=1;
            }
            mViewPager.setCurrentItem(currentItem,false);
        }
    }

    /**
     * 对应生命周期调用
     */
    public void onDestroy(){
        if(null!=mDataBeans) mDataBeans.clear();
        if(null!=mAdapter){
            mAdapter.notifyDataSetChanged();
        }
        if(null!=mBackgroundLayout){
            mBackgroundLayout.onDestroy();
            mBackgroundLayout=null;
        }
        if(null!=mViewPager){
            mViewPager.removeAllViews();
            mViewPager=null;
        }
    }
}