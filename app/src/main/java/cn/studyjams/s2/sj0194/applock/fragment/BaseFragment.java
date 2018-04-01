package cn.studyjams.s2.sj0194.applock.fragment;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;

/**
 * Created by pan on 17-5-6.
 */
@SuppressLint("ValidFragment")
public class BaseFragment extends Fragment {
    private String tabTitle;

    /**
     * 获取BaseFragment对象的tab标题
     *
     * @return 返回String型的字符串
     */
    public String getTabText() {
        return tabTitle;
    }

    public BaseFragment() {

    }

    public BaseFragment(String tabTitle) {
        this.tabTitle = tabTitle;
    }
}
