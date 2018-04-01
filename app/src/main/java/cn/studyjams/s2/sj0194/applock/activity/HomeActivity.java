package cn.studyjams.s2.sj0194.applock.activity;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.adapter.HomeFragmentAdapter;
import cn.studyjams.s2.sj0194.applock.fragment.BaseFragment;
import cn.studyjams.s2.sj0194.applock.fragment.HomeFragment;
import cn.studyjams.s2.sj0194.applock.fragment.SettingFragment;
import cn.studyjams.s2.sj0194.applock.service.AppLockService;
import cn.studyjams.s2.sj0194.applock.util.ConstantClass;
import cn.studyjams.s2.sj0194.applock.util.SpUtil;

/**
 * Created by pan on 17-5-6.
 */

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int WELCOME = 101;//发送欢迎消息的状态码
    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 101;//请求获app栈顶应用权限的请求码
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private List<BaseFragment> fragmentList;
    private AlertDialog dialog;//对话框
    private String welcomeStr;
    private InnerReceiver innerReceiver;
    /*
    * FireBase实例
    * */
    //static FirebaseDatabase database = FirebaseDatabase.getInstance();
    //showDialog相关组件
    private TextView tv_show;
    private TextView tv_title;
    private ImageView iv_icon;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WELCOME:
                    //如果从数据库中获取欢迎和本地已经保存的欢迎信息不一样，则显示出消息
                    if (!welcomeStr.equals(SpUtil.getString(getApplicationContext(), ConstantClass.STRING_WELCOME_MESSAGE, null))) {
                        showNotification();
                        Log.d(TAG, "开始发送通知");
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //为了能监听到按钮，在setContentView之前添加,未添加的话home键监听无效，设置窗体属性
        this.getWindow().setFlags(0x80000000, 0x80000000);

        setContentView(R.layout.activity_home);

        //初始化UI
        initUI();

        //初始化数据
        initData();

        //初始化构造器
        initAdapter();

        //初始化权限获取应用消息的权限
        initPermission();

        //打开应用，如果用户设置了应用锁，立即开启应用锁
        if (SpUtil.getBoolean(getApplicationContext(), ConstantClass.IS_SERVICE, false)) {
            //如果应用锁功能已经开启，即打开应用锁服务
            startService(new Intent(getApplicationContext(), AppLockService.class));
        }

        //如果用户第一次打开应用，则弹出欢迎对话框
        if (SpUtil.getBoolean(getApplicationContext(), ConstantClass.IS_FIRST_TIME_OPEN, true)) {
            //弹出欢迎对话框
            showWelcomeDialog();
            //弹出信息之后，保存非第一次打开应用的标记信息
            SpUtil.putBoolean(getApplicationContext(), ConstantClass.IS_FIRST_TIME_OPEN, false);
        }

        //加载数据库欢迎信息，信息用于发送通知
        //20170604暂时取消通知功能
        //initWelcomeNotification();

        //初始化广播接收者，用于重写home键的响应
        initReceiver();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS) {
            if (!hasPermission()) {
                //若用户未开启获取应用运行状态的权限，则引导用户开启“Apps with usage access”权限
                startActivityForResult(
                        new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
            }
        }
    }

    /**
     * 初始化权限
     */
    private void initPermission() {
        boolean hasPermission = hasPermission();
        if (!hasPermission) {
            //若用户未开启获取应用运行状态权限，则引导用户开启“Apps with usage access”权限
            startActivityForResult(
                    new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                    MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
            Toast.makeText(getApplicationContext(), R.string.home_activity_toast_content, Toast.LENGTH_LONG).show();
        }
    }

    //检测用户是否对本app开启了“Apps with usage access”权限
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        /*
        * 当Android版本为4.4以上，判断是否开启了获取应用运行状态的权限
        * */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }


    /**
     * 加载数据库欢迎信息
     */
    private void initWelcomeNotification() {
        /*
        * 获取数据库中通知的数据
        * */
        //DatabaseReference content = database.getReference("notification_content");

        // Read from the database
        /*content.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                welcomeStr = dataSnapshot.getValue(String.class);
                //提示显示窗口通知
                Message message = Message.obtain();
                message.what = WELCOME;
                mHandler.sendMessage(message);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });*/
    }

    /**
     * 发送欢迎的通知栏信息
     */
    public void showNotification() {
        //获取通知服务对象
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //.Build是构建者模式
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        //===============通知窗口的属性设置===============
        builder.setSmallIcon(R.drawable.ic_small_notification);//设置通知的图标
        builder.setTicker("1");//在状态栏跳出时显示的内容
        builder.setContentTitle("软件锁");//在下拉条里显示的通知的标题
        builder.setContentText(welcomeStr);//标题下的内容摘要
        builder.setDefaults(Notification.DEFAULT_SOUND);//设置通知接收时，系统对应的提醒方式，震动，声音等
        //把所有设置联合起来，返回一个新的notification
        Notification build = builder.build();
        notificationManager.notify(1, build);

        //数据库发送一次欢迎消息之后，就保存来自服务器的信息，如果下次服务器信息有改变，则在此接收到消息
        SpUtil.putString(getApplicationContext(), ConstantClass.STRING_WELCOME_MESSAGE, welcomeStr);
    }


    /**
     * 弹出欢迎对话框
     */
    private void showWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //因为需要自定义对话框的样式，所以调用builder的create()方法和setView()方法
        dialog = builder.create();

        final View view = View.inflate(getApplicationContext(), R.layout.dialog_show_my_message, null);
        tv_title = (TextView) view.findViewById(R.id.tv_title);

        iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
        tv_show = (TextView) view.findViewById(R.id.tv_show);

        //在设置对话框时，设置左上右下的内边界距都为0
        dialog.setView(view, 0, 0, 0, 0);
        Button bt_submit = (Button) view.findViewById(R.id.bt_submit);

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //使对话框不能取消
        dialog.setCancelable(false);

        //将对话框显示出来
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.home_activity_share_content));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initAdapter() {
        //适配构造器
        viewPager.setAdapter(new HomeFragmentAdapter(getSupportFragmentManager(), fragmentList));
        //使TabLayout与ViewPager结合起来
        tabLayout.setupWithViewPager(viewPager);
        //设置标题模式：滚动模式
        //tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    private void initData() {
        //创建List
        fragmentList = new ArrayList<>();

        //创建两个Fragment对象
        HomeFragment homeFragment = new HomeFragment(getString(R.string.home_activity_viewpager_title1));
        SettingFragment settingFragment = new SettingFragment(getString(R.string.home_activity_viewpager_title2));

        fragmentList.add(homeFragment);
        fragmentList.add(settingFragment);
    }

    private void initUI() {
        //设置沉浸效果
        ConstantClass.setActivityImmersive(this);

        /*
        * 获取TabLayout和ViewPager的对象
        * */
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        //.初始化ToolBar
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_small_toolbar);
        }
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