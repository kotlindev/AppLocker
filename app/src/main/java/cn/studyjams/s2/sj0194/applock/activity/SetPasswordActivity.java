package cn.studyjams.s2.sj0194.applock.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.takwolf.android.lock9.Lock9View;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.util.ConstantClass;
import cn.studyjams.s2.sj0194.applock.util.Md5Util;
import cn.studyjams.s2.sj0194.applock.util.SpUtil;


/**
 * Created by pan on 17-5-14.
 */

public class SetPasswordActivity extends AppCompatActivity {

    private Lock9View lock_9_view;
    private TextView tv_des;//描述文本
    String firstPs, secondPs;//两次输入的密码
    private boolean isEnter = false;//标记密码第一次是否验证成功,默认未成功
    private InnerReceiver innerReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //为了能监听到按钮，在setContentView之前添加,未添加的话home键监听无效，设置窗体属性
        this.getWindow().setFlags(0x80000000, 0x80000000);

        setContentView(R.layout.activity_set_password);

        initUI();

        initData();


        //初始化广播接收者
        initReceiver();
    }

    private void initData() {
        /*
        * 回调九宫个密码设置
        * */
        lock_9_view.setCallBack(new Lock9View.CallBack() {
            @Override
            public void onFinish(String password) {
                if (!isEnter) {
                    //如果密码未验证，则先验证密码
                    String strPassword = SpUtil.getString(getApplicationContext(), ConstantClass.LOCKED_PASSWORD, null);
                    if (strPassword != null && Md5Util.encoder(password).equals(strPassword)) {
                        //验证成功时，标记为成功
                        tv_des.setText(R.string.set_password_activity_enter_new_password_hint);
                        Toast.makeText(getApplicationContext(), R.string.set_password_activity_verified, Toast.LENGTH_SHORT).show();
                        isEnter = true;
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.set_password_activity_password_error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //密码已经验证成功，直接开始设置
                    if (password.length() < 4) {
                        Toast.makeText(getApplicationContext(), R.string.set_password_activity_short_hint, Toast.LENGTH_SHORT).show();
                    } else {
                        if (firstPs == null) {
                            //第一次输入
                            firstPs = password;
                            tv_des.setText(R.string.set_password_activity_confirm_hint);
                            Toast.makeText(getApplicationContext(), R.string.set_password_activity_confirm_new_hint, Toast.LENGTH_SHORT).show();
                        } else {
                            //第二次输入
                            secondPs = password;

                            //确定密码
                            if (!firstPs.equals(secondPs)) {
                                firstPs = null;
                                firstPs = null;
                                Toast.makeText(getApplicationContext(), R.string.set_password_activity_different_hint, Toast.LENGTH_SHORT).show();
                                tv_des.setText(R.string.set_password_activity_enter_new_hint);
                            } else {
                                //保存密码
                                SpUtil.putString(getApplicationContext(), ConstantClass.LOCKED_PASSWORD, Md5Util.encoder(password));
                                Toast.makeText(getApplicationContext(), R.string.set_password_activity_saved_hint, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }
                }


            }
        });
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

        tv_des = (TextView) findViewById(R.id.tv_des);
        tv_des.setText(R.string.activity_setting_saved_password);

        lock_9_view = (Lock9View) findViewById(R.id.lock_9_view);
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
