package com.lnjzghy.jiayang_gizwits_android.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.icu.text.DateIntervalFormat;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiConfigureMode;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.enumration.GizWifiGAgentType;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.larksmart7618.sdk.communication.wifi.WifiAdmin;
import com.lnjzghy.jiayang_gizwits_android.R;
import com.lnjzghy.jiayang_gizwits_android.Utils.SharePreUtil;
import com.lnjzghy.jiayang_gizwits_android.Utils.WifiAdminUtils;
import com.qmuiteam.qmui.widget.QMUITopBar;

import java.util.ArrayList;
import java.util.List;

import static com.gizwits.gizwifisdk.enumration.GizWifiGAgentType.GizGAgentESP;

public class NetConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEtAPPaw;
    private CheckBox mCbpaw;

    private TextView mTvAPSsid;
    private Button mBtnAdd;

    private WifiAdminUtils adminUtils;
    //进度弹窗
    private ProgressDialog dialog;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what==105){
                dialog.setMessage("配网成功");
                //有结果回调的话
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
            }else if (msg.what==106){
                dialog.setMessage("配网失败");
                //有结果回调的话
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
            }

        }
    };






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_config);
        initView();


        //String uid=SharePreUtil.getString(NetConfigActivity.this,"_uid",null);
        //String token=SharePreUtil.getString(NetConfigActivity.this,"_token",null);
       // Log.e("jiayangYss","NetConfigActivity uid:"+ uid);
       // Log.e("jiayangYss","NetConfigActivity token:"+ token);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //拿到我们手机当前链接的WIFI名称
        String ssid = adminUtils.getWifiConnectedSsid();
        //判断是否拿到了WIFI名称
        if (ssid !=null){
            mTvAPSsid.setText(ssid);
        }else {
            mTvAPSsid.setText(""); //拿不到WIFI名称则显示空文本
        }
        //如果拿不到当前连接的WIFI的名字,就把搜索按钮不可点击
        boolean isEmptyAPSSID = TextUtils.isEmpty(ssid);
        if (isEmptyAPSSID){
            mBtnAdd.setEnabled(false);
            mEtAPPaw.setEnabled(false);
        }
    }

    private void initView() {

        adminUtils = new WifiAdminUtils(this);
        QMUITopBar topBar =findViewById(R.id.topBar);
        topBar.setTitle("添加设备");
        topBar.addLeftImageButton(R.mipmap.ic_back,R.id.topbar_left_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mEtAPPaw =findViewById(R.id.etApPassword);

        mEtAPPaw.addTextChangedListener(new TextWatcher() {

            //编辑框之前的数据
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            //正在编辑的数据回调
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()){
                    mCbpaw.setVisibility(View.VISIBLE);
                }else {
                    mCbpaw.setVisibility(View.GONE);

                }
            }
            //编辑完成之后的数据回调
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mCbpaw=findViewById(R.id.cbPaw);
        mCbpaw.setVisibility(View.GONE);//默认为隐藏的
        mCbpaw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){
                    mEtAPPaw.setInputType(99);  //0x90是特殊字符，表示要显示密码
                }else{
                    mEtAPPaw.setInputType(0x81);  //0x81是特殊字符，表示要显示密码
                }

            }
        });

        mTvAPSsid =findViewById(R.id.tvAPSsid);
        mBtnAdd = findViewById(R.id.btnAdd);
        mBtnAdd.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if(view.getId()==R.id.btnAdd){

            String tvSSID=mTvAPSsid.getText().toString().intern();//WIFI 名字
            String tvPas = mEtAPPaw.getText().toString().intern();//wifi密码

            if(!tvSSID.isEmpty()){
                dialog = new ProgressDialog(NetConfigActivity.this);
                dialog.setMessage("努力配网中。。。");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);//屏幕外不可点击
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();

                    }
                });
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        finish();//点击确认就摧毁当前页面
                    }
                });
                dialog.show();
                //没有回调结果的话隐藏两个按钮
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
                //开始配网
                startAirlink(tvSSID,tvPas);
            }


        }

    }

    private void startAirlink(String ssid ,String pas){

        GizWifiSDK.sharedInstance().setListener(listener);
        List<GizWifiGAgentType> types = new ArrayList<>();
        types.add(GizGAgentESP);//只使用8266配网方式

        //GizWifiSDK.sharedInstance().setDeviceOnboardingDeploy();这个是新版本的配网方式,为了兼容旧设备，不采用这种方法
        GizWifiSDK.sharedInstance().setDeviceOnboarding(ssid,pas,GizWifiConfigureMode.GizWifiAirLink,null,60,types);

    }

    private GizWifiSDKListener listener = new GizWifiSDKListener() {
        @Override
        public void didSetDeviceOnboarding(GizWifiErrorCode result, GizWifiDevice device) {
            super.didSetDeviceOnboarding(result, device);
            if (result==GizWifiErrorCode.GIZ_SDK_SUCCESS){
                mHandler.sendEmptyMessage(105);
            }else {
                mHandler.sendEmptyMessage(106);
            }


        }
    };
}
