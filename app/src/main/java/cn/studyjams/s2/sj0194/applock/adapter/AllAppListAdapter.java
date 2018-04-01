package cn.studyjams.s2.sj0194.applock.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.studyjams.s2.sj0194.applock.R;
import cn.studyjams.s2.sj0194.applock.db.dao.AppLockDao;
import cn.studyjams.s2.sj0194.applock.db.domain.AppInfo;


/**
 * Created by pan on 17-5-7.
 */

public class AllAppListAdapter extends BaseAdapter {
    private final List<AppInfo> appInfoList;
    private final Context mContext;
    private final List<String> mLockListPackages;
    int lockNum = 0, unLockNum = 0;

    /**
     * @param mLockListPackages  在界面上勾选的应用，即将保存到加锁数据库，
     *                           ListView滚动时，它将根据数据库是否存在当前条目应用的包名进行是否回显，
     *                           而用户点击后的条目的应用包名暂时保存在mLockListPackages中，
     *                           所以在此需要判断：
     *                           如果mLockListPackages集合中也包含ListView中的条目的包名，则也让它显示勾选状态
     * @param appInfoList        应用信息集合，用于填充ListView条目
     * @param applicationContext 上下文环境
     */
    public AllAppListAdapter(List<String> mLockListPackages, List<AppInfo> appInfoList, Context applicationContext) {
        this.appInfoList = appInfoList;
        this.mLockListPackages = mLockListPackages;
        this.mContext = applicationContext;
    }

    @Override
    public int getCount() {
        return appInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return appInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*
        * 填充布局
        * */
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.app_list_item, null);

            holder = new ViewHolder();
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
            holder.tv_app_name = (TextView) convertView.findViewById(R.id.tv_app_name);
            holder.tv_app_type = (TextView) convertView.findViewById(R.id.tv_app_type);
            holder.cb_box = (CheckBox) convertView.findViewById(R.id.cb_lock_state);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //使用数据填充控件
        holder.iv_icon.setImageDrawable(appInfoList.get(position).getAppIcon());
        holder.tv_app_name.setText(appInfoList.get(position).getAppName());
        if (appInfoList.get(position).isSystemApp()) {
            holder.tv_app_type.setText(R.string.app_item_system_app);
        } else {
            holder.tv_app_type.setText(R.string.app_item_user_app);
        }

        //对应用是否已经加锁状态进行回显，如果mLockListPackages包含当前条目包名，则尽显回显
        List<String> allLockApps = AppLockDao.getInstance(mContext).findAll();
        if (mLockListPackages.contains(appInfoList.get(position).getPackageName())) {
            //如果此条目的应用的包名有记录在加锁应用数据库中，则把它设置为勾选状态
            holder.cb_box.setChecked(true);
        } else {
            // 如果数据库中没有保存，则设置为未勾选状态
            holder.cb_box.setChecked(false);
        }


        return convertView;
    }

    public class ViewHolder {
        ImageView iv_icon;
        TextView tv_app_name, tv_app_type;
        CheckBox cb_box;
    }
}
