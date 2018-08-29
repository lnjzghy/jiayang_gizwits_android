package com.lnjzghy.jiayang_gizwits_android.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by lnjzghy on 2018/8/2.
 * /sharedPerferencrs处理，存储的处理uid和token
 */

public class SharePreUtil {

    private static final String SP_NAME="config";//全局变量


    /**
     *存储String封装
     * @param mContext 上下文
     * @param key   键
     * @param value 数值
     */
    public static void putString(Context mContext,String key , String value){

        //拿到本地的SharedPreferences一个对象，设置为本地引用才能读取
        SharedPreferences sp = mContext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        //拿到SharedPreferences的操作对象
        SharedPreferences.Editor editor = sp.edit();
        //存储
        editor.putString(key,value);
        //应用一下
        editor.apply();
    }

    /**
     * 取出String封装
     * @param mContext 上下文
     * @param key 键
     * @param defValue  数值  默认为defValue
     * @return 返回值
     */
    public static String getString(Context mContext,String key,String defValue){

        //拿到本地的SharedPreferences一个对象，设置为本地引用才能读取
        SharedPreferences sp = mContext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        //取出来,如果该键值为null，就会默认为defValue
        return sp.getString(key,defValue);
    }

}
