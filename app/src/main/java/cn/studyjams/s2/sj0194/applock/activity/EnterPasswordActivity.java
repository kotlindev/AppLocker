package cn.studyjams.s2.sj0194.applock.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.takwolf.android.lock9.Lock9View;

import java.util.List;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.db.domain.AppInfo;
import cn.studyjams.s2.sj0194.applock.engine.AppInfoProvider;
import cn.studyjams.s2.sj0194.applock.util.ConstantClass;
import cn.studyjams.s2.sj0194.applock.util.Md5Util;
import cn.studyjams.s2.sj0194.applock.util.SpUtil;


/**
 * 输入密码的Activity
 * Created by pan on 17-5-13.
 */

public class EnterPasswordActivity extends Activity {

    private static final String TAG = "EnterPasswordActivity";
    private ImageView iv_app_icon;
    private Lock9View lock_9_view;
    private List<AppInfo> appInfoList;
    private String packageName;
    private TextView tv_app_name;
    private boolean isSelf;//是否为应用自己打开的此界面
    private InnerReceiver innerReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //为了能监听到按钮，在setContentView之前添加,未添加的话home键监听无效，设置窗体属性
        this.getWindow().setFlags(0x80000000, 0x80000000);


        if (SpUtil.getInt(getApplicationContext(), ConstantClass.LOCK_STYLE, 0) == 0) {
            //加载默认样式
            setContentView(R.layout.activity_enterpassword_normal_style);
        } else {
            //加载圆形样式
            setContentView(R.layout.activity_enterpassword_circle_style);
        }
        //初始化UI
        initUI();
        //如果是本身的应用，设置本身图标和自己名字
        isSelf = getIntent().getExtras().getBoolean(ConstantClass.IS_SELF, false);
        if (isSelf) {
            //如果是极客程序锁本身打开的此界面，加载自己的资源极客
            iv_app_icon.setImageResource(R.drawable.ic_square_blue);
            tv_app_name.setText(R.string.app_name);
        } else {
            //如果是加锁应用打开的此界面，则加载加锁应用的数据
            initData();
        }


        //初始化广播接收者
        initReceiver();

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(innerReceiver);
        super.onDestroy();
    }

    private void initReceiver() {
        //创建广播
        innerReceiver = new InnerReceiver();
        //动态注册广播
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //启动广播
        registerReceiver(innerReceiver, intentFilter);
    }

    private void initData() {
        //获取所有应用信息，目的为了获取应用图标
        if (appInfoList == null) {
            appInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
        }

        //获取传过来的包名
        packageName = getIntent().getExtras().getString("package_name", getPackageName());//默认获取本应用的包名
        Log.i(TAG, "当前拦截的软件包名为：" + packageName);
        //将应用图标设置到锁屏界面上
        for (AppInfo appInfo : appInfoList) {
            if (appInfo.getPackageName().contains(packageName)) {
                iv_app_icon.setImageDrawable(appInfo.getAppIcon());
                tv_app_name.setText(appInfo.getAppName());
            }
        }
    }

    private void initUI() {
        //沉浸式状态栏
        ConstantClass.setActivityImmersive(this);
        //获取控件
        iv_app_icon = (ImageView) findViewById(R.id.iv_app_icon);
        tv_app_name = (TextView) findViewById(R.id.tv_app_name);
        lock_9_view = (Lock9View) findViewById(R.id.lock_9_view);

        lock_9_view.setCallBack(new Lock9View.CallBack() {
            @Override
            public void onFinish(String password) {
                //使用当前输入密码和已保存的应用锁密码对比
                if (Md5Util.encoder(password)
                        .equals(SpUtil.getString(getApplicationContext(), ConstantClass.LOCKED_PASSWORD, null))) {
                    if (isSelf) {
                        //进入主界面
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    } else {
                        //发送广播告知AppLockService服务不要再拦截此应用
                        Intent intent = new Intent("android.intent.action.SKIP");
                        intent.putExtra("package_name", packageName);
                        sendBroadcast(intent);
                        //输入密码正确后，解锁应用
                        Log.i(TAG, "发送已经解锁的广播，发送包名：" + packageName);
                    }
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast_content_passwoed_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "回退键被监听");
            //进入主界面
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);

            finish();

            return true;//return true;拦截事件传递,从而屏蔽back键。
        }
        if (KeyEvent.KEYCODE_HOME == keyCode) {
            return true;//return true;拦截事件传递,从而屏蔽back键。
        }
        return super.onKeyDown(keyCode, event);
    }


    class InnerReceiver extends BroadcastReceiver {

        final String SYSTEM_DIALOG_REASON_KEY = "reason";

        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

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
                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        finish();
                        Log.d(TAG, "多任务键被监听");
                    }
                }
            }
        }
    }
}
