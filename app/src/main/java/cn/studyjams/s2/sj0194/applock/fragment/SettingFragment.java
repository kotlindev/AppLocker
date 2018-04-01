package cn.studyjams.s2.sj0194.applock.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.activity.AboutActivity;
import cn.studyjams.s2.sj0194.applock.activity.FeedBackActivity;
import cn.studyjams.s2.sj0194.applock.activity.HelpingActivity;
import cn.studyjams.s2.sj0194.applock.activity.SettingActivity;


/**
 * Created by pan on 17-5-6.
 */

public class SettingFragment extends BaseFragment implements View.OnClickListener {

    private FragmentActivity mContext;

    public SettingFragment() {
        //无参构造函数
        super(null);
    }

    @SuppressLint("ValidFragment")
    public SettingFragment(String tabTitle) {
        super(tabTitle);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //填充布局
        View view = inflater.inflate(R.layout.fragment_setting, null);

        //获取布局中各个子控件并注册其点击事件
        view.findViewById(R.id.cv_setting).setOnClickListener(this);
        view.findViewById(R.id.cv_helping).setOnClickListener(this);
        view.findViewById(R.id.cv_feedback).setOnClickListener(this);
        view.findViewById(R.id.cv_about).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cv_setting:
                startActivity(new Intent(getContext(), SettingActivity.class));
                break;
            case R.id.cv_helping:
                startActivity(new Intent(getContext(), HelpingActivity.class));
                break;
            case R.id.cv_feedback:
                //弹出设置对话框
                startActivity(new Intent(getContext(), FeedBackActivity.class));
                break;
            case R.id.cv_about:
                startActivity(new Intent(getContext(), AboutActivity.class));
                break;
            default:
                break;
        }
    }
}
