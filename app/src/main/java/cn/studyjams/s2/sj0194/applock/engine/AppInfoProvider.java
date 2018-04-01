package cn.studyjams.s2.sj0194.applock.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import cn.studyjams.s2.sj0194.applock.db.domain.AppInfo;


/**
 * 获取App信息集合
 * <p>
 * Created by pan on 17-5-7.
 */

public class AppInfoProvider {

    private static PackageManager packageManager;

    /**
     * 获取APP信息集合
     *
     * @param context 上下文环境
     * @return 返回系统中已经安装的应用的集合
     */
    public static List<AppInfo> getAppInfoList(Context context) {
        //1.获取包管理对象
        packageManager = context.getPackageManager();
        //2.获取安装的手机应用的集合,参数为0即代表获取基本信息
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);
        //3.创建APP集合
        List<AppInfo> appInfoList = new ArrayList<>();

        //4.循环遍历获取应用信息集合
        for (PackageInfo packageInfo : packages) {
            AppInfo appInfo = new AppInfo();
            //5.应用名称
            appInfo.setPackageName(packageInfo.packageName);
            //6.获取应用包名
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            appInfo.setAppName(applicationInfo.loadLabel(packageManager).toString());
            //7.应用的图标
            appInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
            //8.判断应用为手机应用或者系统应用
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                appInfo.setSystemApp(true);
            } else {
                appInfo.setSystemApp(false);
            }
            //9.把应用添加到List集合中
            appInfoList.add(appInfo);
        }
        return appInfoList;
    }
}
