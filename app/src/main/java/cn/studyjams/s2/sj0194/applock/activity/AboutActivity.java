package cn.studyjams.s2.sj0194.applock.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.util.ConstantClass;


/**
 * Created by pan on 17-5-15.
 */

public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";
    private TextView tv_version;
    private PackageManager packageManager;
    private InnerReceiver innerReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //为了能监听到按钮，在setContentView之前添加,未添加的话home键监听无效，设置窗体属性
        this.getWindow().setFlags(0x80000000, 0x80000000);

        setContentView(R.layout.activity_about);

        initUI();

        initData();

        //初始化广播接收者
        initReceiver();
    }

    private void initData() {
        tv_version.setText(getVersionName());
    }

    /**
     * 获取软件版本
     *
     * @return 返回软件版本
     */
    private String getVersionName() {
        //1.获取包管理对象
        packageManager = getPackageManager();
        //2.获取版本信息，参数：1.包名，2.传0代表基本信息
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            //3.获取对应名称
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "1";
    }

    private void initUI() {
        //设置沉浸 状态栏的模式
        ConstantClass.setActivityImmersive(this);

        //获取并初始化ToolBar
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        //设置ActionBar的返回菜单支持
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
        }

        //获取软件版本的显示文本
        tv_version = (TextView) findViewById(R.id.tv_version);

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
