package cn.studyjams.s2.sj0194.applock.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.activity.AllAppListActivity;
import cn.studyjams.s2.sj0194.applock.adapter.LockedAppListAdapter;
import cn.studyjams.s2.sj0194.applock.db.dao.AppLockDao;
import cn.studyjams.s2.sj0194.applock.db.domain.AppInfo;
import cn.studyjams.s2.sj0194.applock.engine.AppInfoProvider;


/**
 * Created by pan on 17-5-6.
 */

public class HomeFragment extends BaseFragment {

    private static final int UPDATE_DATA_FINISHED = 101;
    private FragmentActivity mContext;
    private static LockedAppListAdapter mAppInfoAdapter;
    private static String TAG = "HomeFragment";

    /*
   * 为CardView准备数据
   * */
    static List<AppInfo> mAppInfoList = new ArrayList<>();//应用集合

    static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_DATA_FINISHED:
                    //重新构造数据
                    mAppInfoAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private List<AppInfo> allSystemAppInfoList;
    private static List<String> allLockApps;
    private static AppLockDao instance;
    private RecyclerView recycler_view;

    public HomeFragment() {
        //空的构造函数
        super(null);
    }

    @SuppressLint("ValidFragment")
    public HomeFragment(String tabTitle) {
        super(tabTitle);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Log.i(TAG, "生命周期onCreate");
    }

    @Override
    public void onResume() {
        Log.i(TAG, "生命周期onResume");
        updateAppInfo();
        super.onResume();
    }

    /**
     * 更新应用数据
     */
    public void updateAppInfo() {
        /*
        * 先清理再加载，在模拟下拉刷新时可能用得到
        * */
        mAppInfoList.clear();
        if (allLockApps != null) {
            allLockApps.clear();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                 /*
        * 随机生成30各重复的列表项
        * */

                //从数据库获取已加锁应用包名
                //获取数据库操作对象实例
                if (instance == null) {
                    instance = AppLockDao.getInstance(getContext());
                }
                //执行查询操作
                allLockApps = instance.findAll();
                //遍历打印结果
                int i = 0;
                for (String lockApp : allLockApps) {
                    Log.i(TAG, "应用" + i++ + "的包名为：" + lockApp);
                }

                //获取系统所有应用
                if (allSystemAppInfoList == null) {
                    allSystemAppInfoList = AppInfoProvider.getAppInfoList(mContext);
                }
                for (AppInfo appInfo : allSystemAppInfoList) {
                    //如果当前应用包名存在加锁应用集合中，加其加入全局应用信息集合
                    if (allLockApps.contains(appInfo.getPackageName())) {
                        mAppInfoList.add(appInfo);
                    }
                }
                //发送加载数据完毕的消息
                Message message = Message.obtain();
                message.what = UPDATE_DATA_FINISHED;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "生命周期：onCreateView");

        View view = inflater.inflate(R.layout.fragment_home, null);
        //* 获取RecyclerView对象
        recycler_view = (RecyclerView) view.findViewById(R.id.recycler_view);
        // * 构造卡片式布局管理器
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 1);
        //给RecyclerView设置卡片式布局
        recycler_view.setLayoutManager(layoutManager);
        // * 创建构造器
        mAppInfoAdapter = new LockedAppListAdapter(mAppInfoList, new LockedAppListAdapter.OnAppItemDeletedListener() {
            @Override
            public void onAppItemDeleted(List<AppInfo> mAppInfoList) {
                //当条目被删除之后，通知构造器更新数据
                mAppInfoAdapter.notifyDataSetChanged();
            }
        });
        recycler_view.setAdapter(mAppInfoAdapter);

        //初始化主页按钮
        view.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AllAppListActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

}
