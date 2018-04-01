package cn.studyjams.s2.sj0194.applock.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Date;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.util.ConstantClass;


/**
 * Created by pan on 17-5-15.
 */

public class FeedBackActivity extends AppCompatActivity {

    private EditText content;
    private InnerReceiver innerReceiver;
    //实时数据库实例
    //private DatabaseReference mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //为了能监听到按钮，在setContentView之前添加,未添加的话home键监听无效，设置窗体属性
        this.getWindow().setFlags(0x80000000, 0x80000000);
        //初始化布局
        setContentView(R.layout.activity_feedback);
        //初始化控件
        initUI();
        //初始化广播接收者
        initReceiver();
        //初始化数据
        initData();
    }

    private void initData() {
        //mDatabase = FirebaseDatabase.getInstance().getReference("feedback");
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

        //获取控件
        //内容
        content = (EditText) findViewById(R.id.et_feedback_content);


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
