package cn.studyjams.s2.sj0194.applock.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import cn.studyjams.s2.sj0194.applock.fragment.BaseFragment;


/**
 * Created by pan on 17-5-6.
 */

public class HomeFragmentAdapter extends FragmentPagerAdapter {

    private final List<BaseFragment> fragments;

    public HomeFragmentAdapter(FragmentManager fm, List<BaseFragment> fragmentList) {
        super(fm);
        this.fragments = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).getTabText();
    }
}
