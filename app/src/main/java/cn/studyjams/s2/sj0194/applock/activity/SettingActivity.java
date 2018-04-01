package cn.studyjams.s2.sj0194.applock.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.service.AppLockService;
import cn.studyjams.s2.sj0194.applock.util.ConstantClass;
import cn.studyjams.s2.sj0194.applock.util.SpUtil;


/**
 * Created by pan on 17-5-12.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SettingActivity";
    private Switch switch_button;
    private TextView tv_des;
    private InnerReceiver innerReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //为了能监听到按钮，在setContentView之前添加,未添加的话home键监听无效，设置窗体属性
        this.getWindow().setFlags(0x80000000, 0x80000000);

        setContentView(R.layout.activity_setting);
        //初始化控件
        initUI();
        //初始化应用锁服务
        initAppLock();

        //初始化广播接收者
        initReceiver();
    }

    private void initAppLock() {
        //判断服务是否属于开启状态，再根据其状态做回显
        //boolean isRunning = ServiceUtil.isRunning(getApplicationContext(), "cn.jkdev.applock.service.AppLockService");
        //从SP中回显状态
        boolean isService = SpUtil.getBoolean(getApplicationContext(), ConstantClass.IS_SERVICE, false);
        switch_button.setChecked(isService);
        if (isService) {
            tv_des.setText(R.string.setting_activity_open_hint);
        } else {
            tv_des.setText(R.string.setting_activity_close_hint);
        }


        // 注册开关按钮的点击事件
        switch_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //如果按钮是开启状态，则开启应用锁服务
                    Log.i(TAG, "应用锁服务已开启");
                    // 注册应用锁服务
                    startService(new Intent(getApplicationContext(), AppLockService.class));
                    tv_des.setText(R.string.setting_activity_open_hint);
                } else {
                    //如果按钮切换到关闭状态，则注销应用锁服务
                    Log.i(TAG, "应用锁服务已关闭");
                    //注销密码应用锁服务
                    stopService(new Intent(getApplicationContext(), AppLockService.class));
                    tv_des.setText(R.string.setting_activity_close_hint);
                }
                //保存状态
                SpUtil.putBoolean(getApplicationContext(), ConstantClass.IS_SERVICE, isChecked);
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

        //获取控件，并注册点击事件

        //1.获取开关按钮
        switch_button = (Switch) findViewById(R.id.switch_button);
        tv_des = (TextView) findViewById(R.id.tv_des);

        //2.获取布局中的菜单项按钮
        findViewById(R.id.ll_style).setOnClickListener(this);
        findViewById(R.id.ll_reset).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_style:
                Log.i(TAG, "锁屏风格被点击");
                showStyleDialog();
                break;
            case R.id.ll_reset:
                Log.i(TAG, "重置密码被点击");
                startActivity(new Intent(getApplicationContext(), SetPasswordActivity.class));
                break;
        }
    }

    private void showStyleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //因为需要自定义对话框的样式，所以调用builder的create()方法和setView()方法
        final AlertDialog dialog = builder.create();

        final View view = View.inflate(getApplicationContext(), R.layout.dialog_set_style, null);
        //在设置对话框时，设置左上右下的内边界距都为0
        dialog.setView(view, 0, 0, 0, 0);
        //将对话框显示出来
        dialog.show();

        final RadioButton normal_style = (RadioButton) view.findViewById(R.id.normal_style);
        RadioButton circle_style = (RadioButton) view.findViewById(R.id.circle_style);

        //回显
        int lockStyle = SpUtil.getInt(getApplicationContext(), ConstantClass.LOCK_STYLE, 0);
        if (lockStyle == 0) {
            normal_style.setChecked(true);
        } else {
            circle_style.setChecked(true);
        }

        Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (normal_style.isChecked()) {
                    //如果默认样式被勾选，则保存默认样式
                    SpUtil.putInt(getApplicationContext(), ConstantClass.LOCK_STYLE, 0);
                } else {
                    //如果默认样式不被勾选，则保存圆形样式
                    SpUtil.putInt(getApplicationContext(), ConstantClass.LOCK_STYLE, 1);
                }
                dialog.dismiss();
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
