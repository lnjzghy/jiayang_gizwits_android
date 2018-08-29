package com.lnjzghy.jiayang_gizwits_android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.lnjzghy.jiayang_gizwits_android.R;

import java.util.List;

/**
 * Created by lnjzghy on 2018/8/4.
 */

public class LVDevicesAdapter extends BaseAdapter{
    private Context mContext;

    private List<GizWifiDevice> gizWifiDeviceList;

    private LayoutInflater mLayoutInflater;


    public LVDevicesAdapter(Context mContext, List<GizWifiDevice> gizWifiDeviceList) {
        this.mContext = mContext;
        this.gizWifiDeviceList = gizWifiDeviceList;
        mLayoutInflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }





    @Override
    public int getCount() {
        return gizWifiDeviceList.size();
    }

    @Override
    public Object getItem(int i) {
        return gizWifiDeviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView (int i,View view,ViewGroup viewGroup){

        ViewHondlerListView viewHondlerListView =null;
        View view1;
        GizWifiDevice device =gizWifiDeviceList.get(i);
        if (view==null){
            view1=mLayoutInflater.inflate(R.layout.item_list_view_devices,null);
            viewHondlerListView=new ViewHondlerListView();
            //绑定控件
            viewHondlerListView.mTvName =view1.findViewById(R.id.tvDeviceName);
            viewHondlerListView.mTVStatus=view1.findViewById(R.id.tvStatus);
            viewHondlerListView.mIvDeviceIcon=view1.findViewById(R.id.ivDeviceIcon);
            viewHondlerListView.mIvNext=view1.findViewById(R.id.iVnext);

            view1.setTag(viewHondlerListView);

        }else{
            view1=view;
            viewHondlerListView= (ViewHondlerListView) view1.getTag();
        }

        //设置名字如果用户已经设置该设备的别名，就有限显示别名
        if (device.getAlias().isEmpty()){
            viewHondlerListView.mTvName.setText(device.getProductName());
        }else {
            viewHondlerListView.mTvName.setText(device.getAlias());
        }

        //设置设备状态
        if (device.getNetStatus()== GizWifiDeviceNetStatus.GizDeviceOffline){
            viewHondlerListView.mTVStatus.setText("离线");
            viewHondlerListView.mTVStatus.setTextColor(mContext.getResources().getColor(R.color.bar_divider));
            viewHondlerListView.mIvNext.setVisibility(View.INVISIBLE);//离线后箭头隐藏
            viewHondlerListView.mTvName.setTextColor(mContext.getResources().getColor(R.color.bar_divider));
        }else {
            //设备不为离线状态的时候，进一步判断，结果为局域网在线或广域网在线
            if (device.isLAN()){
                viewHondlerListView.mTVStatus.setText("局域网在线");
            }else {
                viewHondlerListView.mTVStatus.setText("远程在线");
            }
            viewHondlerListView.mTvName.setTextColor(mContext.getResources().getColor(R.color.black));
            viewHondlerListView.mTVStatus.setTextColor(mContext.getResources().getColor(R.color.black));
            viewHondlerListView.mIvNext.setVisibility(View.VISIBLE);//在线后箭头显示
        }

        return view1;
    }



    private class ViewHondlerListView{
        //设备图标 ,箭头
        ImageView mIvDeviceIcon , mIvNext;

        //设备名字 ，设备在线状态
        TextView mTvName , mTVStatus;


    }
}
