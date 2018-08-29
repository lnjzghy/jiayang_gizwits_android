package com.lnjzghy.jiayang_gizwits_android.ui.DevicesControlActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.lnjzghy.jiayang_gizwits_android.R;

import java.util.concurrent.ConcurrentHashMap;


public class DevivePetAvtivity extends BaseDevicesControlActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private Switch mMSwitchScram;
    private Switch mMSwitchInterchange;
    private Switch mMSwitchOpenAll;
    private Switch mMSwitchCloseAll;
    private TextView mTvResult_Screen_open;
    private SeekBar mMSeek_Screen;
    private TextView mTvResult_Curtain_open;
    private SeekBar mMSeek_Curtain;
    private TextView mTv_Motor;
    private SeekBar mMSeek_Motor;
    private TextView mTvResult_Motor;

    //全局变量不可改变
    private static final String KEY_CURRENTPOSITION="CurrentPosition";
    private static final String KEY_LHOOK1POSITION="LHook1Position";
    private static final String KEY_RHOOK1POSITION="RHook1Position";
    private static final String KEY_MOTOR="Motor";

    private static final String KEY_CurtainOnOff="CurtainOnOff";
    private static final String KEY_GauzeOnOff="GauzeOnOff";
    private static final String KEY_TrackLength="TrackLength";



    private static final String KEY_SCRAM="Scram";
    private static final String KEY_INTERCHANGE="Interchange";
    private static final String KEY_OPENALL="OpenAll";
    private static final String KEY_CLOSALL="CloseAll";


    //全局的临时变量
    private int tempCurrentPosition=0;
    private int tempMotor=0;
    private boolean tempScram=false;
    private boolean tempInterchange=false;
    private boolean tempOpenAll=false;
    private boolean tempCloseAll=false;
    private int tempLHook1Position=0;
    private int tempRHook1Position=0;
    private int tempGauzeOnOff=0;
    private int tempCurtainOnOff=0;

    @SuppressLint("HandlerLeak")
    private Handler mHanfler =new Handler(){

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);

          if (msg.what==108) {
              updataUI();
          }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_pet);
        initView();
    }

    private void initView() {
        mTopBar =findViewById(R.id.topBar);
        //同步这个设备的名字（以别名的方式）
        String tempTitle = mDevice.getAlias().isEmpty()?mDevice.getProductName():mDevice.getAlias();
        mTopBar.setTitle(tempTitle);
        mTopBar.addLeftImageButton(R.mipmap.ic_back,R.id.topbar_left_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        bindViews();
    }




    private void bindViews() {


        mMSwitchScram = (Switch) findViewById(R.id.mSwitchScram);
        mMSwitchInterchange = (Switch) findViewById(R.id.mSwitchInterchange);
        mMSwitchOpenAll = (Switch) findViewById(R.id.mSwitchOpenAll);
        mMSwitchCloseAll = (Switch) findViewById(R.id.mSwitchCloseAll);
        mTvResult_Screen_open = (TextView) findViewById(R.id.tvResult_Screen_open);//文本框不需要点击事件
        mMSeek_Screen = (SeekBar) findViewById(R.id.mSeek_Screen);
        mTvResult_Curtain_open = (TextView) findViewById(R.id.tvResult_Curtain_open);
        mMSeek_Curtain = (SeekBar) findViewById(R.id.mSeek_Curtain);
        mTv_Motor = (TextView) findViewById(R.id.tv_Motor);
        mMSeek_Motor = (SeekBar) findViewById(R.id.mSeek_Motor);
        mTvResult_Motor = (TextView) findViewById(R.id.tvResult_Motor);


        //ui触摸点击事件初始化
        mMSwitchScram.setOnClickListener(this);
        mMSwitchInterchange.setOnClickListener(this);
        mMSwitchOpenAll.setOnClickListener(this);
        mMSwitchCloseAll.setOnClickListener(this);
        mMSeek_Screen.setOnSeekBarChangeListener(this);
        mMSeek_Screen.setMax(80);

        mMSeek_Curtain.setOnSeekBarChangeListener(this);
        mMSeek_Curtain.setMax(80);

       // mMSeek_Motor.setOnSeekBarChangeListener(this);
       // mMSeek_Motor.setMax(10);//-4 -3 -2 -1 0 1 2 3 4 5
    }




    @Override
    protected void receiveCloundDate(GizWifiErrorCode result, ConcurrentHashMap<String, Object> dataMap) {
        super.receiveCloundDate(result, dataMap);
        Log.e("jiayangYss","DevivePetAvtivity控制界面的下发数据"+dataMap);

        if (result== GizWifiErrorCode.GIZ_SDK_SUCCESS){
            //如果返回结果为成功，并且下发数据不为null的话进行剖析
            if (dataMap!=null){
                parseReceiveData(dataMap);

            }
        }

    }

    private void updataUI() {
        mTvResult_Motor.setText(tempCurrentPosition+" ");
        mMSwitchScram.setChecked(tempScram);
        mMSwitchInterchange.setChecked(tempInterchange);
        mMSwitchOpenAll.setChecked(tempOpenAll);
        mMSwitchCloseAll.setChecked(tempCloseAll);
        mTvResult_Screen_open.setText(tempRHook1Position+" ");
        mTvResult_Curtain_open.setText(tempLHook1Position+" ");
       // mMSeek_Screen.setProgress(()(tempLHook1Position/KEY_TrackLength)*100);
       // mMSeek_Curtain.setProgress(tempRHook1Position);
    }


    private void parseReceiveData(ConcurrentHashMap<String, Object> dataMap) {


        if (dataMap.get("data")!=null){

            ConcurrentHashMap<String, Object> temperDataMap = (ConcurrentHashMap<String, Object>) dataMap.get("data");

            for (String dataKey:temperDataMap.keySet()){

                //通过我们在云端定义的数据点的标志点，获取动力车体位置
                if (dataKey.equals(KEY_CURRENTPOSITION)){
                    tempCurrentPosition = (int) temperDataMap.get(KEY_CURRENTPOSITION);
                }
                //紧急停车
                if (dataKey.equals(KEY_SCRAM)){
                    tempScram = (boolean) temperDataMap.get(KEY_SCRAM);
                }
                //帘纱互换
                if (dataKey.equals(KEY_INTERCHANGE)){
                    tempInterchange = (boolean) temperDataMap.get(KEY_INTERCHANGE);
                }
                //全部打开
                if (dataKey.equals(KEY_OPENALL)){
                    tempOpenAll = (boolean) temperDataMap.get(KEY_OPENALL);
                }
                //全部关闭
                if (dataKey.equals(KEY_CLOSALL)){
                    tempCloseAll = (boolean) temperDataMap.get(KEY_CLOSALL);
                }

                //窗纱开合
                if (dataKey.equals(KEY_LHOOK1POSITION)){
                     tempLHook1Position= (int) temperDataMap.get(KEY_LHOOK1POSITION);
                }
                //窗帘开合
                if (dataKey.equals(KEY_RHOOK1POSITION)){
                    tempRHook1Position = (int) temperDataMap.get(KEY_RHOOK1POSITION);
                }
                //动力车体速度
                //if (dataKey.equals(KEY_CLOSALL)){
                //    tempCloseAll = (boolean) temperDataMap.get(KEY_CLOSALL);
               // }

            }

            mHanfler.sendEmptyMessage(108);
        }

    }


    //按钮的点击事件回调
    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.mSwitchScram){
            sendCommand(KEY_SCRAM,mMSwitchScram.isChecked());
        }
        if (v.getId()==R.id.mSwitchInterchange){
            sendCommand(KEY_INTERCHANGE,mMSwitchInterchange.isChecked());
        }
        if (v.getId()==R.id.mSwitchOpenAll){
            sendCommand(KEY_OPENALL,mMSwitchOpenAll.isChecked());

        }
       if (v.getId()==R.id.mSwitchCloseAll){
           sendCommand(KEY_CLOSALL,mMSwitchCloseAll.isChecked());
           

       }
    }

    //拖动条触摸拖动停止时的点击事件回调
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //通过获取SeekBAR的ID来识别是那个拖动条
        switch (seekBar.getId()){
            case R.id.mSeek_Screen:
                sendCommand(KEY_GauzeOnOff,seekBar.getProgress()+10);
                break;
            case R.id.mSeek_Curtain:
                sendCommand(KEY_CurtainOnOff,seekBar.getProgress()+10);
                break;
            case R.id.mSeek_Motor:
                break;
            default:
                    break;
        }



    }





















































    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }


}
