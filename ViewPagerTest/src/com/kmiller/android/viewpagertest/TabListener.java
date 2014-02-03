package com.kmiller.android.viewpagertest;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

public class TabListener<T extends Fragment> implements ActionBar.TabListener {

    private final Activity mActivity;
    private final String mTag;
    private ViewPager mPager;

    /** Constructor used each time a new tab is created.
      * @param activity  The host Activity, used to instantiate the fragment
      * @param tag  The identifier tag for the fragment
      */
    public TabListener(Activity activity, String tag, ViewPager pager) {
        this.mActivity = activity;
        this.mTag = tag;
        this.mPager = pager;
    }

    /* The following are each of the ActionBar.TabListener callbacks */

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
    	mPager.setCurrentItem(Integer.parseInt(mTag));
    	}

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    		//do nothing
    	}

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // User selected the already selected tab. Usually do nothing.
    }

	
}
