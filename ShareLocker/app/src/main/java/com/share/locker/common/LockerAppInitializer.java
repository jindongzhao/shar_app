package com.share.locker.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.share.locker.MainActivity;
import com.share.locker.http.HttpCallback;
import com.share.locker.http.LockerHttpUtil;
import com.share.locker.util.JsonUtil;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Jordan on 16/01/2018.
 */

public class LockerAppInitializer {
    private MainActivity mainActivity;
    private final String URL_GET_APP_CONFIG = Constants.URL_BASE + "getAppConfig.json";

    public LockerAppInitializer(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public void init(){
        //申请权限
        if(ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(mainActivity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        if(ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(mainActivity,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }

        //检查系统配置信息是否存在
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.SHARED_REF_NAME, Context.MODE_PRIVATE);
        String config = sharedPreferences.getString(Constants.SHARED_REF_KEY_APP_CONFIG,null);
        if(config == null){
            //没有配置信息，从接口获取
            LockerHttpUtil.getJson(URL_GET_APP_CONFIG,
                new HttpCallback(){
                    @Override
                    public void processSuccess(String successData){
                        //保存到sharedPref
                        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.SHARED_REF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.SHARED_REF_KEY_APP_CONFIG,successData);
                        editor.apply();

                        //初始化配置对象
                        AppConfig.init(successData);

                        //系统初始化完成后，执行界面操作
                        mainActivity.runOnUiThread(
                            new Runnable() {
                               @Override
                               public void run() {
                                   mainActivity.excuseAfterInitApp();
                               }
                            });
                    }
                    @Override
                    public void processFail(String failData){
                        //TODO 弹框提示系统异常
                    }
                });
        }else{
            //初始化配置对象
            AppConfig.init(config);

            //系统初始化完成后，执行界面操作
            mainActivity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.excuseAfterInitApp();
                        }
                    });
        }

    }
}
