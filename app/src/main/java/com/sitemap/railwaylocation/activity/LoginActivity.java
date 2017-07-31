package com.sitemap.railwaylocation.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sitemap.railwaylocation.R;
import com.sitemap.railwaylocation.application.MyApplication;
import com.sitemap.railwaylocation.config.RequestCode;
import com.sitemap.railwaylocation.config.WebUrlConfig;
import com.sitemap.railwaylocation.http.HttpUtil;
import com.sitemap.railwaylocation.model.UserModel;
import com.sitemap.railwaylocation.util.ParserUtil;
import com.sitemap.railwaylocation.util.PreferencesUtil;
import com.sitemap.railwaylocation.util.ToastUtil;
import com.sitemap.railwaylocation.view.RoundProgressDialog;

import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;

/**
 * 登录页面
 * created by chenmeng on 2017/6/6
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity implements View.OnClickListener{
    private LoginActivity mContext;//  本类
    @ViewInject(R.id.back)
    private LinearLayout mBack;//返回上一层
    @ViewInject(R.id.title)
    private TextView mTitle;//标题
    @ViewInject(R.id.userName)
    private EditText mUserName;//用户名
    @ViewInject(R.id.password)
    private EditText mPsw;//密码
    @ViewInject(R.id.login)
    private TextView mLogin;//登录
    private HttpUtil http;//网络请求
    private RoundProgressDialog progressDialog;//加载条
    private boolean isPhone =false;//账号是否输入
    private boolean isPsw =false;//密码是否输入规范

    @Override
    protected void initData() {
        mContext = this;
        mBack.setVisibility(View.GONE);
        mTitle.setText("登录");
        mLogin.setOnClickListener(this);
        if (http == null){
            http = new HttpUtil(handler);
        }
        getPermission();
        mUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length()>0){
                    isPhone =true;
                    if (isPsw){
                        mLogin.setSelected(true);
                    }else {
                        mLogin.setSelected(false);
                    }
                }else {
                    isPhone =false;
                    mLogin.setSelected(false);
                }
            }
        });
        mPsw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length()>0){
                    isPsw =true;
                    if (isPhone){
                        mLogin.setSelected(true);
                    }else {
                        mLogin.setSelected(false);
                    }
                }else {
                    isPsw =false;
                    mLogin.setSelected(false);
                }
            }
        });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();// 关闭进度条
            }
            switch (msg.what) {
                case HttpUtil.SUCCESS:
                    if (msg.arg1 == RequestCode.LOGIN) {
                        MyApplication.userModel = (UserModel) ParserUtil.jsonToObject(msg.obj.toString(), UserModel.class);
                        if ("1".equals(MyApplication.userModel.getResult())){
                            ToastUtil.showBottomLong(mContext,"登录成功");
                            int time = Integer.parseInt(MyApplication.userModel.getGpsRate())*1000;
                            MyApplication.gpsTime = time;
//                            MyApplication.locationService = new LocationService(getApplicationContext());
                            PreferencesUtil.isFristLogin(mContext,"first",false);
                            PreferencesUtil.setStringData(mContext,"userName",mUserName.getText().toString());
                            PreferencesUtil.setStringData(mContext,"psw",mPsw.getText().toString());

                            Intent intent = new Intent(mContext,MainActivity.class);
                            startActivity(intent);
                            mContext.finish();
                        }else{
                            ToastUtil.showBottomLong(mContext,MyApplication.userModel.getErrorMsg());
                        }
                    }
                    break;
                case HttpUtil.EMPTY:
                    break;
                case HttpUtil.FAILURE:
                    ToastUtil.showBottomShort(mContext, RequestCode.ERRORINFO);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 获取登录信息
     *
     */
    private void getLogin(String userName,String psw,String mac) {
        if (MyApplication.getNetObject().isNetConnected()) {
            progressDialog = RoundProgressDialog.createDialog(mContext);
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.setMessage("登陆中...");
                progressDialog.show();
            }
            RequestParams params = http.getParams(WebUrlConfig.login());
            params.addBodyParameter("userName",userName);
            params.addBodyParameter("userPwd",psw);
            params.addBodyParameter("mac",mac);
            http.sendPost(RequestCode.LOGIN,params);
        } else {
            ToastUtil.showBottomShort(mContext, RequestCode.NOLOGIN);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mLogin){
            if (TextUtils.isEmpty(mUserName.getText().toString().trim())){
                ToastUtil.showBottomShort(mContext,"用户名不能空");
                return;
            }
            if (TextUtils.isEmpty(mPsw.getText().toString().trim())){
                ToastUtil.showBottomShort(mContext,"密码不能为空");
                return;
            }
            MyApplication.mac = MyApplication.getDeviceID(mContext);
            getLogin(mUserName.getText().toString(),mPsw.getText().toString(),MyApplication.mac);
        }
    }

    @TargetApi(23)
    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }

            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 127);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("result","g:"+ JSON.toJSONString(grantResults));
        if (requestCode == 127) {
            int sum = 0;
            for (int i:grantResults){
                sum = sum +i;
            }
            if (sum == PackageManager.PERMISSION_GRANTED) {
                MyApplication.mac = MyApplication.getDeviceID(mContext);
            } else {
                //请求失败则提醒用户
                ToastUtil.showBottomShort(mContext,"请求权限失败！");
                mContext.finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        //退出所有的activity
        Intent intent = new Intent();
        intent.setAction(BaseActivity.TAG_ESC_ACTIVITY);
        sendBroadcast(intent);
        finish();
        super.onBackPressed();
    }
}
