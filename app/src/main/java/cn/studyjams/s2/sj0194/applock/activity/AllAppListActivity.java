package cn.studyjams.s2.sj0194.applock.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.adapter.AllAppListAdapter;
import cn.studyjams.s2.sj0194.applock.db.dao.AppLockDao;
import cn.studyjams.s2.sj0194.applock.db.domain.AppInfo;
import cn.studyjams.s2.sj0194.applock.engine.AppInfoProvider;
import cn.studyjams.s2.sj0194.applock.util.ConstantClass;


/**
 * Created by pan on 17-5-6.
 */

public class AllAppListActivity extends AppCompatActivity {
    private static final String TAG = "AllAppListActivity";
    private static final int UPDATE_DATA_FINISHED = 101;//更新UI的状态码
    private static final int SHOW_DATA_CHANGED = 102;
    private TextView tv_system_app_num;
    private TextView tv_usr_app_num;
    private ListView lv_app_list;
    private TextView tv_lock_des;

    private List<AppInfo> appInfoList;
    private List<String> mLockListPackages = null;
    int systemAppNum = 0;//系统应用数目
    private AppLockDao appLockDao;
    private InnerReceiver innerReceiver;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_DATA_FINISHED:
                    //适配数据
                    lv_app_list.setAdapter(new AllAppListAdapter(mLockListPackages, appInfoList, getApplicationContext()));
                    //显示各种应用类型及数目
                    showLockedAppStatus();
                    break;
                case SHOW_DATA_CHANGED:
                    showLockedAppStatus();
                    finish();
                    break;
            }
        }
    };

    /**
     * 显示应用加锁状态信息
     */
    private void showLockedAppStatus() {
        tv_system_app_num.setText(getString(R.string.system_app) + systemAppNum);
        tv_usr_app_num.setText(getString(R.string.user_app) + (appInfoList.size() - systemAppNum));
        tv_lock_des.setText(getString(R.string.locked_app) + mLockListPackages.size());
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //为了能监听到按钮，在setContentView之前添加,未添加的话home键监听无效，设置窗体属性
        this.getWindow().setFlags(0x80000000, 0x80000000);

        setContentView(R.layout.activity_app_list);
        //初始化UI
        initUI();
        //初始化数据
        intData();
        //初始化广播接收者
        initReceiver();
    }


    private void intData() {
        /*
        * 加载数据是耗时的操作，所以在子线程进行
        * */
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.构造数据
                appInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
                //2.区分已加锁应用和未加锁应用
                mLockListPackages = new ArrayList<>();

                //获取数据库中的已加锁应用
                Log.i(TAG, "所有应用的信息如下：");
                List<String> allLockApps = AppLockDao.getInstance(getApplicationContext()).findAll();
                for (AppInfo appInfo : appInfoList) {
                    Log.i(TAG, "应用包名:" + appInfo.getPackageName());
                    Log.i(TAG, "应用名:" + appInfo.getAppName());
                    Log.i(TAG, "是否时系统应用:" + appInfo.isSystemApp());
                    //在遍历的同时，把数据库中已经加锁应用记录下来
                    if (allLockApps.contains(appInfo.getPackageName())) {
                        //在包名匹配的情况下把应用记录下来
                        mLockListPackages.add(appInfo.getPackageName());
                    }
                    //记录系统应用数目
                    if (appInfo.isSystemApp()) {
                        ++systemAppNum;
                    }
                }

                // 数据加载完毕，通知主线程更新UI
                Message message = Message.obtain();
                message.what = UPDATE_DATA_FINISHED;
                mHandler.sendEmptyMessage(0);
                mHandler.sendMessage(message);
            }
        }).start();
    }

    private void initUI() {
        //设置沉浸 状态栏的模式
        ConstantClass.setActivityImmersive(this);

        //获取控件
        tv_system_app_num = (TextView) findViewById(R.id.tv_system_app_num);
        tv_usr_app_num = (TextView) findViewById(R.id.tv_usr_app_num);
        lv_app_list = (ListView) findViewById(R.id.lv_app_list);
        tv_lock_des = (TextView) findViewById(R.id.tv_lock_des);

        //获取并初始化ToolBar
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        //设置ActionBar的返回菜单支持
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
        }

        //注册ListView的监听事件
        lv_app_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //将原有的可选框状态取反
                CheckBox lockState = (CheckBox) view.findViewById(R.id.cb_lock_state);
                lockState.setChecked(!lockState.isChecked());
                if (lockState.isChecked()) {
                    //如果按钮属于点击状态，将应用添加到加锁应用
                    mLockListPackages.add(appInfoList.get(position).getPackageName());
                } else {
                    //如果按钮属于非点击状态，将应用从已加锁应用中减去
                    if (mLockListPackages.contains(appInfoList.get(position).getPackageName())) {
                        mLockListPackages.remove(appInfoList.get(position).getPackageName());
                        Log.i(TAG, "加锁应用集合数据发生变化，删除去:" + appInfoList.get(position).getPackageName());
                    }
                }


            }
        });

        // 初始化浮动按钮
        ImageButton fab = (ImageButton) findViewById(R.id.add_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //当按钮被点击后，把应用包名存入数据库
                if (appLockDao == null) {
                    //创建数据库操作对象
                    appLockDao = new AppLockDao(getApplicationContext());
                }

                //创建新线程，对数据库进行操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //先将原来所有数据删除
                        for (String name : appLockDao.findAll()) {
                            appLockDao.delete(name);
                            Log.i(TAG, "数据库正在删除：" + name);
                        }
                        //再执行数据插入操作,使用if语句做容错处理
                        if (mLockListPackages.size() > 0) {
                            for (int i = 0; i < mLockListPackages.size(); i++) {
                                appLockDao.insert(mLockListPackages.get(i));
                            }
                           /* for (String lockApp : mLockListPackages) {
                                appLockDao.insert(lockApp);
                                Log.i(TAG, "数据库正在添加：" + lockApp);
                            }*/
                        }
                        Log.i(TAG, "应用数据已经更改");
                        //提示主线程弹出提示信息
                        Message message = Message.obtain();
                        message.what = SHOW_DATA_CHANGED;
                        mHandler.sendMessage(message);
                    }
                }).start();

                //showMessage(v);
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    *
    * 以下代码重写home事件按钮
    *
    * */

    private void initReceiver() {
        //创建广播
        innerReceiver = new InnerReceiver();
        //动态注册广播
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //启动广播
        registerReceiver(innerReceiver, intentFilter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_HOME == keyCode) {
            return true;//return true;拦截事件传递,从而屏蔽back键。
        }
        return super.onKeyDown(keyCode, event);
    }


    class InnerReceiver extends BroadcastReceiver {

        final String SYSTEM_DIALOG_REASON_KEY = "reason";

        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        //用户点击Home按钮时，结束密码解锁界面
                        finish();
                        Log.d(TAG, "home键被监听");
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(innerReceiver);
        super.onDestroy();
    }
}
