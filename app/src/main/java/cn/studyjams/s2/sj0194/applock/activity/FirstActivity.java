package cn.studyjams.s2.sj0194.applock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class FirstActivity extends AppCompatActivity {
    private Lock9View lock_9_view;
    private TextView tv_des;//描述文本
    String firstPs, secondPs;//两次输入的密码
    private TextView tv_title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        initUI();
        initData();

        if (!(SpUtil.getString(getApplicationContext(), ConstantClass.LOCKED_PASSWORD, null) == null)) {
            //如果密码已经保存，则进入程序主界面
            Intent intent = new Intent(getApplicationContext(), EnterPasswordActivity.class);
            intent.putExtra(ConstantClass.IS_SELF, true);
            startActivity(intent);
            finish();
        }
    }

    private void initData() {
        /*
        * 回调九宫个密码设置
        * */
        lock_9_view.setCallBack(new Lock9View.CallBack() {
            @Override
            public void onFinish(String password) {

                if (password.length() < 4) {
                    Toast.makeText(getApplicationContext(), R.string.first_activity_short_hint, Toast.LENGTH_SHORT).show();
                } else {
                    if (firstPs == null) {
                        //第一次输入
                        firstPs = password;
                        tv_des.setText(R.string.first_activity_confirm_hint);
                        Toast.makeText(getApplicationContext(),R.string.first_activity_confirm_hint, Toast.LENGTH_SHORT).show();
                    } else {
                        //第二次输入
                        secondPs = password;

                        //确定密码
                        if (!firstPs.equals(secondPs)) {
                            firstPs = null;
                            firstPs = null;
                            Toast.makeText(getApplicationContext(), R.string.first_activity_different_hint, Toast.LENGTH_SHORT).show();
                            tv_des.setText(R.string.first_activity_set_password_hint);
                        } else {
                            //保存密码
                            SpUtil.putString(getApplicationContext(), ConstantClass.LOCKED_PASSWORD, Md5Util.encoder(password));
                            Toast.makeText(getApplicationContext(), R.string.first_activity_saved_password_hint, Toast.LENGTH_SHORT).show();
                            //设置好密码后，进入主界面
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));

                            //第一次设置好密码之后，就设置开启应用锁
                            SpUtil.putBoolean(getApplicationContext(), ConstantClass.IS_SERVICE, true);

                            finish();
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
            supportActionBar.setDisplayShowTitleEnabled(false);
        }

        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(R.string.first_activity_set_fisrt_password_hint);

        tv_des = (TextView) findViewById(R.id.tv_des);

        lock_9_view = (Lock9View) findViewById(R.id.lock_9_view);
    }

}
