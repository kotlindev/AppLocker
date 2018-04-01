package cn.studyjams.s2.sj0194.applock.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import cn.studyjams.s2.sj0194.applock.R;


/**
 * SharedPreferences数据键名常量类
 * <p>
 * Created by pan on 17-5-8.
 */

public class ConstantClass {

    /*
    *以下常量为保存的SharedPreference节点
    * */

    /**
     * 是否开启应用锁服务
     */
    public static final String IS_SERVICE = "is_service";
    /**
     * 应用锁密码
     */
    public static final String LOCKED_PASSWORD = "locked_password";
    /**
     * 加锁样式
     */
    public static final String LOCK_STYLE = "lock_style";
    /**
     * 显示欢迎信息
     */
    public static final String STRING_WELCOME_MESSAGE = "show_welcome_message";
    /**
     * 标记应用打开加锁界面时是否为本身打开
     */
    public static final String IS_SELF = "is_self";
    /**
     * 用户是否为第一次打开应用
     */
    public static final String IS_FIRST_TIME_OPEN = "is_first_time_open";

    /**
     * 为了兼容Android4.4到5.0的显示效果，设置Activity的沉浸标题
     *
     * @param activity 当前Activity
     */
    public static void setActivityImmersive(Activity activity) {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT <= 21) {
            Window window = activity.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            // 设置透明状态栏
            if ((params.flags & bits) == 0) {
                params.flags |= bits;
                window.setAttributes(params);
            }
            // 设置状态栏颜色
            ViewGroup contentLayout = (ViewGroup) window.findViewById(android.R.id.content);

            View statusBarView = new View(activity);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
            contentLayout.addView(statusBarView, lp);
            statusBarView.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimaryDark));

            // 设置Activity layout的fitsSystemWindows
            View contentChild = contentLayout.getChildAt(0);
            contentChild.setFitsSystemWindows(true);
        }
    }

    /**
     * 获得状态栏高度
     */
    private static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }
}
