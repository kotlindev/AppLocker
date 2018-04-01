package cn.studyjams.s2.sj0194.applock.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import cn.studyjams.s2.sj0194.applock.activity.EnterPasswordActivity;
import cn.studyjams.s2.sj0194.applock.db.dao.AppLockDao;


/**
 * Created by pan on 17-5-12.
 */

public class AppLockService extends Service {
    private static final String TAG = "AppLockService";
    private boolean isWatch;
    private AppLockDao appLockDao;
    private List<String> mAppLockList;
    private MyContentObserver mContentObserver;
    private MyReceiver mBroadcastReceiver;
    private static String mUnLockPackageName;//已经解锁的包，由解锁的Activity发送过来
    private String runningPackageName;//正在运行的app

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind正在执行");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand正在执行");
        //使用START_STICKY粘性机制，自动拉活
        return Service.START_STICKY;
    }


    /**
     * 首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或 onBind() 之前）。如果服务已在运行，则不会调用此方法。
     */
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate正在执行");
        appLockDao = AppLockDao.getInstance(getApplicationContext());

        //1.开启循环循环监听模式
        isWatch = true;


        watch();

        //2.
        /*
        *定义数据库的内容观察者，用于接收AppLockDao对象对加锁数据库操作产生的数据变化的消息，
        * 一旦数据库发生变化，就重新加载一次数据库信息
         */
        mContentObserver = new MyContentObserver(new Handler());
        //注册内容观察者,第二个参数为true,代表不匹配，“change”之后的uri,即uri需要一模一样才能接收到消息
        getContentResolver().registerContentObserver(Uri.parse("content://applock/change"), true, mContentObserver);

        //3.定义应用解锁成功的广播接收者对象，一旦某个应用解锁成功，则在解锁界面Activity发送解锁成功的应用包名，在此处接收，使监服务不再监听已经解锁成功的应用
        //（1）过滤器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SKIP");//过滤内容需要和广播发送者设置的过滤器内容一致
        //(2)接收者
        mBroadcastReceiver = new MyReceiver();
        //(3)注册广播接收者
        registerReceiver(mBroadcastReceiver, intentFilter);


        /* 4.注册机器锁屏时的广播 */
        IntentFilter mScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        AppLockService.this.registerReceiver(mScreenOReceiver, mScreenOffFilter);

        super.onCreate();

        Log.i(TAG, "注册应用锁服务");
    }

    /**
     * 监听加锁应用
     */
    private void watch() {

        getTopApp(getApplicationContext());
        //开启子线程不断的监听手机中Activity的变化
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.获取加锁应用集合
                if (appLockDao == null) {
                    appLockDao = AppLockDao.getInstance(getApplicationContext());
                }
                mAppLockList = appLockDao.findAll();
                showLockAppInfo("在watch中获取数据库加锁应用");
                //检测正在开启的应用，任务栈
                Log.i(TAG, "开始监听加锁应用");


                //判断系统版本，如果是Android5.0以上，则使用新的API
                while (isWatch) {


                    //谷歌在Android5.0后禁用了以下API,所以获取不了栈顶应用
                    //2.获取包管理者对象
                   /* ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    //3.获取正在运行的任务栈信息集合
                    List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);//参数为：获取任务栈的最大数目
                    //4.当前任务栈信息集合中指定条目的信息，集合中只有一条数据
                    ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);*/


                    //5.获取栈顶应用的包名
                    runningPackageName = getTopApp(getApplicationContext());
                    Log.i(TAG, "获取应用为：" + runningPackageName);

                    //如果应用在此回到主界面或者其他应用，则代表不再解锁，即当用户在此尝试打开时就要作出拦截
                    if (!runningPackageName.equals(mUnLockPackageName) && !runningPackageName.equals(getPackageName())) {
                        mUnLockPackageName = "";//用户推出之后，使加锁应用变为空
                    }
                    //6.使用此包名和加锁应用集合中的包名做对比
                    if (mAppLockList.contains(runningPackageName)) {
                        Log.i(TAG, "已经监听到" + runningPackageName);
                        Log.i(TAG, "此时传过来的已经解锁包名" + mUnLockPackageName);
                        //如果应用已经解锁，则不再拦截.
                        if (!runningPackageName.equals(mUnLockPackageName)) {
                            Log.i(TAG, "加锁应用正在尝试解锁" + runningPackageName);
                            //7.弹出拦截界面
                         /*
                        * 开启Activity的四种模式
                        * 1.stander标准
                        * 2.singleTop
                        * 3.singleTask
                        * 4.singleInstance
                        *
                        * 在AndroidManifest中把输入密码的Activity开启任务栈的方式声明为singleInstance
                        * */

                        /*
                        以下为FLAG_ACTIVITY_NEW_TASK的官方文档解释
                        * When using this flag, if a task is already running for the activity
                        * you are now starting, then a new activity will not be started; instead,
                        * the current task will simply be brought to the front of the screen with
                        * the state it was last in.
                        * */
                            Intent intent = new Intent(getApplicationContext(), EnterPasswordActivity.class);
                            //添加Flags为FLAG_ACTIVITY_NEW_TASK,以此种方式启动Activity,可以打到的效果为：如果此Activity已经存在，则不能在开启同一个Activity
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("package_name", runningPackageName);
                            startActivity(intent);
                        }

                    }
                    //使当前线程睡眠一会
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 获取并打印栈顶app
     *
     * @param context
     */
    private String getTopApp(Context context) {
        String topActivity = "";
        //高版本获取应用信息
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager m = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (m != null) {
                long now = System.currentTimeMillis();
                //获取10秒之内的应用数据
                List<UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 10 * 1000, now);
                Log.i(TAG, "Running app number in last 60 seconds : " + stats.size());


                //取得最近运行的一个app，即当前运行的app
                if ((stats != null) && (!stats.isEmpty())) {
                    int j = 0;
                    for (int i = 0; i < stats.size(); i++) {
                        if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                            j = i;
                        }
                    }
                    topActivity = stats.get(j).getPackageName();
                }
                Log.i(TAG, "top running app is : " + topActivity);
            }
        } else {
            //低版本获取应用信息
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> infos = am.getRunningTasks(1);
            topActivity = infos.get(0).topActivity.getPackageName();
        }
        return topActivity;
    }


    /**
     * 当服务不再使用且将被销毁时，系统将调用此方法。服务应该实现此方法来清理所有资源，如线程、注册的侦听器、接收器等。 这是服务接收的最后一个调用。
     */
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy正在执行");
        //注销
        isWatch = false;
        if (mContentObserver != null) {
            //通过内容处理者对象注销内容观察者对象
            getContentResolver().unregisterContentObserver(mContentObserver);
        }
        if (mBroadcastReceiver != null) {
            //注销广播接收者
            unregisterReceiver(mBroadcastReceiver);
        }
        if (mScreenOReceiver != null) {
            //注销锁屏接收者
            unregisterReceiver(mScreenOReceiver);
        }
        super.onDestroy();
        Log.i(TAG, "已经注销应用锁服务");
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //当监听到消息时回调此方法
            mUnLockPackageName = (String) intent.getExtras().get("package_name");
            Log.i(TAG, "接收到已经解锁成功的广播，解锁包名:" + mUnLockPackageName);
        }
    }


    /**
     * 内容观察者，用于观察加锁应用数据库的变化
     */
    private class MyContentObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 当数据库内容发生变化时调用此方法
         *
         * @param selfChange
         */
        @Override
        public void onChange(boolean selfChange) {
            Log.i(TAG, "数据库发生改变，重新加载数据");
            //尝试访问数据库，必须使用同一线程
            if (appLockDao == null) {
                appLockDao = AppLockDao.getInstance(getApplicationContext());
            }
            mAppLockList = appLockDao.findAll();
            showLockAppInfo("数据库发生改变后再一次获取加锁应用");
            super.onChange(selfChange);
        }
    }

    /**
     * 打印本地（即从数据库获取的数据）加锁应用列表
     *
     * @param des 描述内容
     */
    private void showLockAppInfo(String des) {
        Log.i(TAG, des);
        for (int i = 0; i < mAppLockList.size(); ++i) {
            Log.i(TAG, "第" + i + "条数据：" + mAppLockList.get(i));
        }
    }

    private BroadcastReceiver mScreenOReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i(TAG, "ScreenService屏幕已经锁上");
                //当用户关闭屏幕时，注销应用锁服务
                stopService(new Intent(getApplicationContext(), AppLockService.class));
            }
        }

    };
}
