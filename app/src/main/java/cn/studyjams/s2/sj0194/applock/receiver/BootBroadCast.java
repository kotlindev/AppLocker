package cn.studyjams.s2.sj0194.applock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.studyjams.s2.sj0194.applock.service.AppLockService;
import cn.studyjams.s2.sj0194.applock.util.ConstantClass;
import cn.studyjams.s2.sj0194.applock.util.SpUtil;


/**
 * 开机启动服务
 * Created by pan on 17-5-14.
 */

public class BootBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //开机启动服务
        if (SpUtil.getBoolean(context, ConstantClass.IS_SERVICE, false)) {
            //如果用户开启了应用锁功能，即打开应用锁服务
            context.startService(new Intent(context, AppLockService.class));
        }
    }
}
