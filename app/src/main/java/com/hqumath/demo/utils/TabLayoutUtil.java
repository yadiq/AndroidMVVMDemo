package com.hqumath.demo.utils;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.tabs.TabLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * ****************************************************************
 * 文件名称 : TabLayoutUtils
 * 作    者 : gyd
 * 创建时间 : 2017/11/20 13:52
 * 文件描述 : tablayout 通用函数
 * 版权声明 :
 * ****************************************************************
 */
public class TabLayoutUtil {

    private int defaultTabLayoutTexSize;//默认字号14dp
    private int defaultTabLayoutItemMargin;//按钮左右间距16dp

    public TabLayoutUtil(Activity context) {
        defaultTabLayoutTexSize = CommonUtil.dp2px(context, 16);
        defaultTabLayoutItemMargin = CommonUtil.dp2px(context, 16);
    }

    /**
     * 计算margin
     * Tablayout的宽度 = 屏幕宽度
     */
    public int onInitTabButtonWidth(Activity context, ArrayList<String> titleList) {
        return onInitTabButtonWidth(context, 0, titleList, false);
    }

    /**
     * 计算margin
     * Tablayout的宽度 = 屏幕宽度 减去 padding
     *
     * @param padding 需要减去的宽度
     * @param fixed   是否固定，不可左右滑动
     */
    public int onInitTabButtonWidth(Activity context, int padding, ArrayList<String> titleList, boolean fixed) {
        String allTitle = "";
        for (String title : titleList) {
            allTitle += title;
        }

        //获取屏幕宽度
        DisplayMetrics displaymetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;
        screenWidth -= padding;

        //tablayout中的margin
        int nMargin = defaultTabLayoutItemMargin;
        if (!TextUtils.isEmpty(allTitle)) {
            int strWidth = (int) getStringWidth(allTitle, defaultTabLayoutTexSize);
            if (screenWidth > strWidth || fixed) {
                nMargin = (screenWidth - strWidth) / titleList.size() / 2;
            }
        }
        return nMargin;
    }

    /**
     * 使用反射修改tablayout 的样式
     */
    public void setIndicator(TabLayout tabs, int margin, ArrayList<String> titleList) {
        Class<?> tabLayout = tabs.getClass();
        Field tabStrip = null;
        //support28时，mTabStrip改为slidingTabIndicator
        /*try {
            tabStrip = tabLayout.getDeclaredField("mTabStrip");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }*/
        try {
            tabStrip = tabLayout.getDeclaredField("slidingTabIndicator");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }

        tabStrip.setAccessible(true);
        LinearLayout llTab = null;
        try {
            llTab = (LinearLayout) tabStrip.get(tabs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < llTab.getChildCount(); i++) {
            View child = llTab.getChildAt(i);
            child.setBackgroundResource(0);
            child.setPadding(0, 0, 0, 0);
            //计算文字宽度
            int width = (int) getStringWidth(titleList.get(i), defaultTabLayoutTexSize);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
            params.width = width;
            params.leftMargin = margin;
            params.rightMargin = margin;
            child.setLayoutParams(params);
            child.invalidate();
            child.setTag(titleList.get(i));
        }
    }

    public static class TabLayoutFragmentAdapter extends FragmentStatePagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titleList;

        public TabLayoutFragmentAdapter(FragmentManager fragmentManager, ArrayList<Fragment> fragments,
                                        ArrayList<String> titleList) {
            super(fragmentManager);
            this.fragments = fragments;
            this.titleList = titleList;
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }
    }

    /**
     * 适配器
     */
    public static class TabLayoutViewAdapter extends PagerAdapter {

        private ArrayList<String> titleList;
        private ArrayList<View> viewList;

        public TabLayoutViewAdapter(ArrayList<String> titleList, ArrayList<View> viewList) {
            this.titleList = titleList;
            this.viewList = viewList;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(viewList.get(position));
            return viewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(viewList.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }
    }

    /**
     * 测量文字宽度
     */
    private float getStringWidth(String str, int size) {
        if (str == null || str.isEmpty())
            str = " ";
        Paint paint = new Paint();
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        paint.setTypeface(font);
        paint.setTextSize(size);
        return paint.measureText(str);
    }
}
