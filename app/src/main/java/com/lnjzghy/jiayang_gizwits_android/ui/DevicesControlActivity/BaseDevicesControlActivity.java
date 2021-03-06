package com.lnjzghy.jiayang_gizwits_android.ui.DevicesControlActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.lnjzghy.jiayang_gizwits_android.MainActivity;
import com.lnjzghy.jiayang_gizwits_android.Utils.WifiAdminUtils;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.concurrent.ConcurrentHashMap;


public abstract class BaseDevicesControlActivity extends AppCompatActivity{


    private QMUITipDialog mTipDialog;


    protected GizWifiDevice mDevice;

    protected QMUITopBar mTopBar;
    private NetWorkChangeReciver netWorkChangeReciver;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDevice();
        initNetBroadReciever();
    }



    private void initNetBroadReciever() {


        netWorkChangeReciver = new NetWorkChangeReciver();
        //此处表示拦截我们安卓系统的网络状态改变的广播
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangeReciver,intentFilter);

    }

    protected  void initDevice(){
        //我们拿到上个界面传来的一个设备对象
        mDevice=getIntent().getParcelableExtra("_device");
        //设置设备的云端回调结果监听状态
        mDevice.setListener(mListener);

        mTipDialog=new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在云端获取数据")
                .create();
        mTipDialog.show();


        //主动获取最新状态
        getStatus();
        Log.e("jiayangYss","上个界面传来的一个设备对象"+mDevice);


    }

    /**
     * 下发控制封装- 发送上传命令
     * @param key 标志名
     * @param value 数值
     */

    protected void sendCommand(String key,Object value){

        if (value==null)
           return;
        ConcurrentHashMap<String, Object> dataMap=new ConcurrentHashMap<>() ;
        dataMap.put(key,value);
        mDevice.write(dataMap,5);

    }

    private void getStatus() {
        //如果当前的设备可以控制的话，那么我们就获取最新状态
        if (mDevice.getNetStatus()==GizWifiDeviceNetStatus.GizDeviceControlled){
            mDevice.getNetStatus();
            mTipDialog.dismiss();
        }
    }


    protected  void receiveCloundDate(GizWifiErrorCode result,ConcurrentHashMap<String, Object> dataMap){
        if (result==GizWifiErrorCode.GIZ_SDK_SUCCESS){
            mTipDialog.dismiss();
        }

    }


    private void updateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus){
        if (netStatus==GizWifiDeviceNetStatus.GizDeviceOffline){
            if (mTipDialog.isShowing()){
                Toast.makeText(this,"设备无法同步",Toast.LENGTH_SHORT).show();
                mTipDialog.dismiss();
                finish();//退出界面

            }
        }

    }



    private GizWifiDeviceListener mListener = new GizWifiDeviceListener(){

        //设备状态回调
        @Override
        public void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
            super.didReceiveData(result, device, dataMap, sn);
            receiveCloundDate(result,dataMap);
            Log.e("jiayangYss","控制界面的下发数据"+dataMap);
        }

        //设备状态回调:包括离线、在线回调
        //该回调主动上报设备的网络状态变化，当设备重上电、断电或可控时会触发该回调
        @Override
        public void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
            super.didUpdateNetStatus(device, netStatus);
            updateNetStatus(device,netStatus);
            Log.e("jiayangYss","控制界面设备状态回调"+netStatus);
        }


    };






    //内部类，获取手机网络状态 发生改变的截取
    private class NetWorkChangeReciver extends BroadcastReceiver{

        //广播接收的内蓉
        @Override
        public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            //获取到了手机已经处于断开网络的状态
            if (info ==null ||!info.isConnected()){
            Log.e("jiayangIUU","断网状态触发：");
            finish();
            }

            if (info ==null)
                return;

            //切换到我们的移动网络
            if (info.getType()==ConnectivityManager.TYPE_MOBILE){
                Log.e("jiayangIUU","切换到我们的移动网络");
            }

            //切换到我们的WIFI网络
            if (info.getType()==ConnectivityManager.TYPE_WIFI){
                Log.e("jiayangIUU","切换到我们的WIFI网络");
            }

        }
    }

    //广播接收的内容
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁广播
        unregisterReceiver(netWorkChangeReciver);
        //取消订阅云端消息
        mDevice.setListener(null);
        mDevice.setSubscribe("f2cdcc113542414ea10a3cdd813ffd44",false);
    }
}
