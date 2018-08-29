package com.lnjzghy.jiayang_gizwits_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizEventType;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.lnjzghy.jiayang_gizwits_android.Utils.SharePreUtil;
import com.lnjzghy.jiayang_gizwits_android.adapter.LVDevicesAdapter;
import com.lnjzghy.jiayang_gizwits_android.ui.DevicesControlActivity.DevivePetAvtivity;
import com.lnjzghy.jiayang_gizwits_android.ui.NetConfigActivity;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private LVDevicesAdapter adapter;
    private List<GizWifiDevice> gizWifiDeviceList;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //刷新的弹窗
    private QMUITipDialog refleshTipDialog;
    private QMUITipDialog mTipDialog;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==105)
                adapter.notifyDataSetChanged();
        }
    };
    private String uid;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSDK();
        initView();
    }
    private void initView() {
        QMUITopBar topBar = findViewById(R.id.topBar);
        topBar.setTitle("嘉阳智能电动窗帘");
        //右边加号添加设备
        topBar.addRightImageButton(R.mipmap.ic_add, R.id.topbar_right_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,NetConfigActivity.class));
            }
        });

        gizWifiDeviceList=new ArrayList<>();
        listView=findViewById(R.id.listview);
        adapter=new LVDevicesAdapter(this,gizWifiDeviceList);
        listView.setAdapter(adapter);
        //轻触的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startControl(gizWifiDeviceList.get(position));
            }
        });
        //长按三秒的点击事件
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showLongDialogonClick(gizWifiDeviceList.get(position));
                return true;
            }
        });
        getBonundDevices();

        mSwipeRefreshLayout=findViewById(R.id.swipeRefreshLayout);
        //设置下拉的颜色
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.white);//初始化为白色
        mSwipeRefreshLayout.setColorSchemeResources(R.color.app_color_theme_sx10
                ,R.color.app_color_theme_sx11
                ,R.color.app_color_theme_sx12
                ,R.color.app_color_theme_sx13
                ,R.color.app_color_theme_sx14);
        //手动调用系统通知
        mSwipeRefreshLayout.measure(0,0);
        //默认是打开就是下拉刷新状态
        mSwipeRefreshLayout.setRefreshing(true);
        //设置手动下来的监听事件
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refleshTipDialog=new QMUITipDialog.Builder(MainActivity.this)
                        .setTipWord("正在刷新中")
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .create();
                refleshTipDialog.show();

                //这里面储存的是可以在主线程调用的
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //拿到SDK里面的设备
                       if (GizWifiSDK.sharedInstance().getDeviceList().size()!=0){
                           gizWifiDeviceList.clear();
                           gizWifiDeviceList.addAll(GizWifiSDK.sharedInstance().getDeviceList());
                           adapter.notifyDataSetChanged();
                       }
                        refleshTipDialog.dismiss();
                        mSwipeRefreshLayout.setRefreshing(false);

                        ConnectivityManager connectivityManager= (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

                        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                        //获取到了手机已经处于断开网络的状态
                        if (info ==null ||!info.isConnected()){
                            mTipDialog=new QMUITipDialog.Builder(MainActivity.this)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .setTipWord("无法获取设备 请检查网络")
                                    .create();
                            mTipDialog.show();
                            listView.setVisibility(View.GONE);
                        }else{
                            listView.setVisibility(View.VISIBLE);
                        //显示另外一个弹窗,如果获取到的设备为空的话
                        if (gizWifiDeviceList.size()==0){
                            mTipDialog=new QMUITipDialog.Builder(MainActivity.this)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_NOTHING)
                                    .setTipWord("无法获取设备")
                                    .create();
                            mTipDialog.show();
                        }else{
                            mTipDialog=new QMUITipDialog.Builder(MainActivity.this)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                    .setTipWord("获取设备成功")
                                    .create();
                            mTipDialog.show();
                        }
                        }
                        mSwipeRefreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTipDialog.dismiss();
                            }
                        },1500);
                    }
                },3000);
            }
        });
        //三秒之后自动收回来
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        },3000);
    }
    //跳转控制
    private void startControl(GizWifiDevice gizWifiDevice) {
        if (gizWifiDevice.getNetStatus()== GizWifiDeviceNetStatus.GizDeviceOffline)
            return;

        gizWifiDevice.setListener(mWifiDeviceListener);

        gizWifiDevice.setSubscribe("f2cdcc113542414ea10a3cdd813ffd44",true);





    }

    private void showLongDialogonClick(final GizWifiDevice device) {

        //显示弹窗
        String[] items=new String[]{"重命名","解绑设备"};
        new QMUIDialog.MenuDialogBuilder(this).addItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        showReNameDialog(device);
                        break;
                    case 1:
                        showDelateDialog(device);
                        break;
                }
                dialogInterface.dismiss();
            }

        }).show();

    }
    //解绑远程设备操作
    private void showDelateDialog(final GizWifiDevice device) {
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("无法解绑局域网设备").setMessage("确定要解绑远程设备？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("删除", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        GizWifiSDK.sharedInstance().unbindDevice(uid,token,device.getDid());
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //重命名操作
    private void showReNameDialog(final GizWifiDevice device) {
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("设备重命名").setInputType(InputType.TYPE_CLASS_TEXT)
                .setPlaceholder("输入新名称")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.dismiss();
            }
        })
                .addAction("确认", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        String newName = builder.getEditText().getText().toString().trim();
                        //判断输入名称是否为空
                        if (newName.isEmpty()){
                            Toast.makeText(MainActivity.this,"不可设为空名称",Toast.LENGTH_SHORT).show();
                        }else{
                            device.setListener(mWifiDeviceListener);
                            device.setCustomInfo(null,newName);
                        }
                        dialog.dismiss();

                    }
                })
                .show();
    }

    private void getBonundDevices() {

        uid = SharePreUtil.getString(this,"_uid",null);
        token = SharePreUtil.getString(this,"_token",null);

        if (uid !=null&& token !=null)
        GizWifiSDK.sharedInstance().getBoundDevices(uid, token);
    }


    private void initSDK(){
        // 设置 SDK 监听
        GizWifiSDK.sharedInstance().setListener(mListener);
        // 设置 AppInfo
        ConcurrentHashMap<String, String> appInfo = new ConcurrentHashMap<>();
        appInfo.put("appId", "e3cd8a543e5d41ed86241e768bd2def0");
        appInfo.put("appSecret", "78553ea02f9a4643823f7b526a2610ec");
        // 设置要过滤的设备 productKey 列表。不过滤则直接传 null
        List<ConcurrentHashMap<String, String>> productInfo = new ArrayList<>();
        ConcurrentHashMap<String, String> product = new ConcurrentHashMap<>();
        product.put("productKey", "a2f9efb89a8e4ac3a8bcc40d44c7ec81");
        product.put("productSecret", "f2cdcc113542414ea10a3cdd813ffd44");
        productInfo.add(product);

        GizWifiSDK.sharedInstance().startWithAppInfo(this, appInfo, productInfo, null, false);
    }
    private GizWifiSDKListener mListener =new GizWifiSDKListener() {
        @Override
        public void didUnbindDevice(GizWifiErrorCode result, String did) {
           // super.didUnbindDevice(result, did);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 解绑成功
                Toast.makeText(MainActivity.this,"解绑完成",Toast.LENGTH_SHORT).show();
            } else {
                // 解绑失败
                Toast.makeText(MainActivity.this,"解绑失败"+result,Toast.LENGTH_SHORT).show();
            }
        }

        /**
        @Override
        public void didBindDevice(GizWifiErrorCode result, String did) {
            super.didBindDevice(result, did);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 绑定成功
                Toast.makeText(MainActivity.this,"绑定成功！",Toast.LENGTH_SHORT).show();
            } else {
                // 绑定失败
                Toast.makeText(MainActivity.this,"绑定失败！",Toast.LENGTH_SHORT).show();
            }

        }*/

        @Override
        public void didNotifyEvent(GizEventType eventType, Object eventSource, GizWifiErrorCode eventID, String eventMessage) {
            super.didNotifyEvent(eventType, eventSource, eventID, eventMessage);

            Log.e("==w","didNotifyEvent:"+eventType);
            //如果我们的SDK 是初始化成功 就匿名登陆
            //匿名登录。匿名方式登录，不需要注册用户账号。
            if(eventType==GizEventType.GizEventSDK){
                GizWifiSDK.sharedInstance().userLoginAnonymous();
            }

        }

        @Override
        public void didUserLogin(GizWifiErrorCode result, String uid, String token) {
            super.didUserLogin(result, uid, token);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {

             // 登录成功
                Log.e("jiayangYSS","登录成功");
                //Log.e("jiayangYSS","登录成功uid:"+uid);
                //Log.e("jiayangYSS","登录成功token"+token);

                SharePreUtil.putString(MainActivity.this,"_uid",uid);
                SharePreUtil.putString(MainActivity.this,"_token",token);
            } else {
             // 登录失败
                Log.e("jiayangYSS","登录失败");
            }

        }

        /**
         *
         * @param result
         * @param deviceList 已经在局域网发现的设备，包括我们一个未绑定的设备
         */
        @Override
        public void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {
            super.didDiscovered(result, deviceList);
            Log.e("m","didDiscovered："+deviceList);
            //每次拿到数据都要清空设备集合
            gizWifiDeviceList.clear();
            gizWifiDeviceList.addAll(deviceList);
            for (int i = 0; i < deviceList.size(); i++) {
                //如果判断设备是否已经绑定
                if (!deviceList.get(i).isBind()){
                    startBindDevice(deviceList.get(i));
                }

            }
            mHandler.sendEmptyMessage(105);

        }
    };

    private void startBindDevice(GizWifiDevice device) {
        if(uid!=null&&token!=null)
            GizWifiSDK.sharedInstance().bindRemoteDevice(uid,token,device.getMacAddress()
                    ,"a2f9efb89a8e4ac3a8bcc40d44c7ec81"
                    ,"f2cdcc113542414ea10a3cdd813ffd44");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //保证了每次打开页面能正常回调SDK监听
        GizWifiSDK.sharedInstance().setListener(mListener);
    }

    private GizWifiDeviceListener mWifiDeviceListener= new GizWifiDeviceListener(){


        @Override
        public void didSetSubscribe(GizWifiErrorCode result, GizWifiDevice device, boolean isSubscribed) {
            super.didSetSubscribe(result, device, isSubscribed);
            Log.e("jiayang","订阅结果："+result);
            Log.e("jiayang","订阅设备："+device);
            //如果为成功的订阅了回调，则可以跳转
            if (result==GizWifiErrorCode.GIZ_SDK_SUCCESS){

                Intent intent =new Intent(MainActivity.this, DevivePetAvtivity.class);
                intent.putExtra("_device",device);
                startActivity(intent);
            }
        }

        @Override
        public void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
            super.didSetCustomInfo(result, device);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
            // 修改成功
                if (GizWifiSDK.sharedInstance().getDeviceList().size()!=0){
                    gizWifiDeviceList.clear();
                    gizWifiDeviceList.addAll(GizWifiSDK.sharedInstance().getDeviceList());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this,"修改完成！",Toast.LENGTH_SHORT).show();
                }
            } else {
            // 修改失败
                Toast.makeText(MainActivity.this,"修改失败！",Toast.LENGTH_SHORT).show();
            }
        }
    };
}
