package cn.studyjams.s2.sj0194.applock.db.domain;

import android.graphics.drawable.Drawable;

/**
 * Created by pan on 17-5-5.
 */

public class AppInfo {
    /*

        * 定义锁屏app应用的信息
        * */
    private String appName, packageName;
    private Drawable appIcon;
    private int imageId;
    boolean isSystemApp, isLuck;//是否为系统应用



    public void setLuck(boolean luck) {
        isLuck = luck;
    }

    public boolean isLuck() {
        return isLuck;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public int getImageId() {
        return imageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

}
