package cn.studyjams.s2.sj0194.applock.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.db.dao.AppLockDao;
import cn.studyjams.s2.sj0194.applock.db.domain.AppInfo;


/**
 * Created by pan on 17-5-5.
 */

public class LockedAppListAdapter extends RecyclerView.Adapter {
    private static final String TAG = "LockedAppListAdapter";
    private final OnAppItemDeletedListener listener;
    private List<AppInfo> mAppInfoList;
    private ViewHolder mViewHolder;
    private Context mContext;
    private AppLockDao appLockDao;

    /**
     * AppInfoAdapter构造函数，需要传入AppInfo对象的集合
     *
     * @param appInfoList
     */
    public LockedAppListAdapter(List<AppInfo> appInfoList, OnAppItemDeletedListener onAppItemDeletedListener) {
        this.mAppInfoList = appInfoList;
        this.listener = onAppItemDeletedListener;
    }


    // 重写的自定义ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon, delete;
        TextView appName, appType;

        /**
         * ViewHolder的构造函数
         *
         * @param itemView
         */
        public ViewHolder(View itemView) {
            super(itemView);
            /*
            * 获取CardView条目中的的各个控件
            * */
            //cardView = (CardView) itemView;
            appIcon = (ImageView) itemView.findViewById(R.id.iv_app_icon);
            appName = (TextView) itemView.findViewById(R.id.tv_app_name);
            appType = (TextView) itemView.findViewById(R.id.tv_app_type);
            delete = (ImageView) itemView.findViewById(R.id.iv_delete);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        //获取上下文环境
        mContext = parent.getContext();
        // 给ViewHolder设置布局文件
        View view = LayoutInflater.from(mContext).inflate(R.layout.app_home_item, parent, false);

        return new ViewHolder(view);
    }

    /**
     * 回调此方法，使用holder对象在指定的条目索引显示CardView条目
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // 给ViewHolder设置元素
        mViewHolder = (ViewHolder) holder;

        Log.i(TAG, "onBindViewHolder被执行");
        final AppInfo appInfo = mAppInfoList.get(position);

        Log.i(TAG, "应用包名:" + appInfo.getPackageName());
        Log.i(TAG, "应用名:" + appInfo.getAppName());
        Log.i(TAG, "是否时系统应用:" + appInfo.isSystemApp());


        mViewHolder.appName.setText(appInfo.getAppName());//设置标题
        mViewHolder.appIcon.setImageDrawable(appInfo.getAppIcon());

        //设置应用类型
        if (appInfo.isSystemApp()) {
            mViewHolder.appType.setText(R.string.app_item_system_app);
        } else {
            mViewHolder.appType.setText(R.string.app_item_user_app);
        }
        //注册点击事件
        mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示提示框
                showDialog(appInfo);
            }
        });
    }

    /**
     * 弹出对话框，提示用户数否删除当前条目对应的数据库中的已加锁应用
     *
     * @param appInfo 被点击条目对应的app信息
     */
    private void showDialog(final AppInfo appInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.locked_app_hint);
        builder.setIcon(appInfo.getAppIcon());
        builder.setMessage(R.string.locked_app_hint_des);
        //积极按钮，确定
        builder.setPositiveButton(R.string.locked_app_hint_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //获取数据库操作对象
                if (appLockDao == null) {
                    appLockDao = AppLockDao.getInstance(mContext);
                }
                //删除数据库条目
                appLockDao.delete(appInfo.getPackageName());
                //删除本地加锁应用集合的当前条目，并且回调onAppItemDeleted方法
                mAppInfoList.remove(appInfo);
                //回调
                listener.onAppItemDeleted(mAppInfoList);
            }
        });
        //消极按钮
        builder.setNegativeButton(R.string.locked_app_hint_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //显示对话框
        builder.show();
    }

    @Override
    public int getItemCount() {
        return mAppInfoList.size();
    }

    /**
     * 定义监听app条目被删除的接口
     */
    public interface OnAppItemDeletedListener {
        /**
         * 当app条目被删除时，执行此方法
         *
         * @param mAppInfoList 删除一个app条目后剩下的app信息集合，此时可以通知构造器更新数据
         */
        void onAppItemDeleted(List<AppInfo> mAppInfoList);
    }
}
