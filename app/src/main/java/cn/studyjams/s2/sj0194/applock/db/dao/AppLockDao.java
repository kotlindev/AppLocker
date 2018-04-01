package cn.studyjams.s2.sj0194.applock.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.studyjams.s2.sj0194.applock.db.AppLockOpenHelper;


/**
 * Created by pan on 17-4-09.
 */

public class AppLockDao {

    private static final String TAG = "AppLockDao";
    private static AppLockOpenHelper appLockOpenHelper;
    private Context context;
    private Cursor cursor;
    private SQLiteDatabase db;

    /**
     * 获取锁屏应用数据库操作对象，并构造数据库
     *
     * @param context 使用上下文环境构造数据库对象
     */
    //BlackNumberDao单立模式
    //1.私有化构造方法
    public AppLockDao(Context context) {
        this.context = context;
        //创建数据库以及其表结构
        appLockOpenHelper = new AppLockOpenHelper(context);
    }

    //2.声明一个当前类的对象
    public static AppLockDao appLockDao = null;

    /**
     * 如果已经在创建数据库实例，则通过此方法获取数据库实例对象，对已有的数据库进行操作
     *
     * @param context
     * @return
     */
    //3.提供一个共有静态一个方法,创建一个实例
    public static synchronized AppLockDao getInstance(Context context) {
        if (appLockDao == null) {
            appLockDao = new AppLockDao(context);
        }
        return appLockDao;
    }

    public static synchronized AppLockOpenHelper getDbInstance(Context context) {
        if (appLockOpenHelper == null)
            appLockOpenHelper = new AppLockOpenHelper(context);

        return appLockOpenHelper;
    }

    //插入方法
    public void insert(String packageName) {
        /*SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();*/

        if (appLockOpenHelper == null) {
            appLockOpenHelper = getDbInstance(context);
        }
        db = appLockOpenHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("packagename", packageName);

        //容错处理
        try {
            if (db.isOpen()) {
                db.insert("applock", null, contentValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();

        //数据增加时,通过内容管理者发通知，在服务里内容观察者捕获
        context.getContentResolver().notifyChange(Uri.parse("content://applock/change"), null);
    }

    //删除方法
    public void delete(String packageName) {
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("packagename", packageName);

        try {
            Log.i(TAG, "正在尝试删除" + packageName);
            if (db.isOpen()) {
                int deleteNum = db.delete("applock", "packagename = ?", new String[]{packageName});
                Log.i(TAG, "成功删除" + deleteNum + "条数据");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();

        //删除过程数据发生改变后，通过内容管理者发通知，在AppLockService服务里的内容观察者捕获，并且作出相关处理
        context.getContentResolver().notifyChange(Uri.parse("content://applock/change"), null);
    }

    //查询所有
    public List<String> findAll() {
        List<String> lockPackageList = new ArrayList<>();
        if (appLockOpenHelper == null) {
            appLockOpenHelper = getDbInstance(context);
        }
        db = appLockOpenHelper.getWritableDatabase();
        //if做容错处理
        try {
            if (db.isOpen()) {
                cursor = db.query("applock", new String[]{"packagename"}, null, null, null, null, null);

                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        //do operation
                        lockPackageList.add(cursor.getString(0));
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


       /* while (cursor.moveToNext()) {
            lockPackageList.add(cursor.getString(0));
        }*/
        //容错判断处理
      /*  if (db.isOpen() && cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                //do operation
                lockPackageList.add(cursor.getString(0));
            }
        }*/

        cursor.close();
        db.close();

        return lockPackageList;
    }

}
