package cn.studyjams.s2.sj0194.applock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.studyjams.s2.sj0194.applock.service.AppLockService;
import cn.studyjams.s2.sj0194.applock.util.ConstantClass;
import cn.studyjams.s2.sj0194.applock.util.SpUtil;


/**
 * Created by pan on 17-5-14.
 */

public class ScreenBroadcast extends BroadcastReceiver {
    private static final String TAG = "ScreenBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "唤醒设置广播接收者收到广播" + action);
        //当用户开启手机后开启应用锁
        if (SpUtil.getBoolean(context, ConstantClass.IS_SERVICE, false)) {
            //如果用户开启服务功能，则开启应用锁服务
            context.startService(new Intent(context, AppLockService.class));
        }
    }
}
