package com.sitemap.railwaylocation.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.sitemap.railwaylocation.R;
import com.sitemap.railwaylocation.application.MyApplication;
import com.sitemap.railwaylocation.config.RequestCode;
import com.sitemap.railwaylocation.config.WebUrlConfig;
import com.sitemap.railwaylocation.db.LocationDataDao;
import com.sitemap.railwaylocation.gps.Point;
import com.sitemap.railwaylocation.gps.SmoothTrack;
import com.sitemap.railwaylocation.http.HttpUtil;
import com.sitemap.railwaylocation.model.GpsModel;
import com.sitemap.railwaylocation.model.ResultModel;
import com.sitemap.railwaylocation.util.DateUtil;
import com.sitemap.railwaylocation.util.FileNames;
import com.sitemap.railwaylocation.util.GpsUtil;
import com.sitemap.railwaylocation.util.ImageUtil;
import com.sitemap.railwaylocation.util.MapUtil;
import com.sitemap.railwaylocation.util.ParserUtil;
import com.sitemap.railwaylocation.util.PreferencesUtil;
import com.sitemap.railwaylocation.util.StepCountCheckUtil;
import com.sitemap.railwaylocation.util.SystemFunUtil;
import com.sitemap.railwaylocation.util.ToastUtil;
import com.sitemap.railwaylocation.view.RoundProgressDialog;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 首页
 * created by chenmeng on 2017/6/6
 */
@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private MainActivity mContext;//本类
    private long exitTime = 0;//退出的时间
    private HttpUtil http;//网络请求
    private RoundProgressDialog progressDialog;//加载条
    @ViewInject(R.id.bmapView)
    private MapView mMapView;//地图视图
    private LatLng latLng = null;//点
    private MapUtil mapUtil;//地图工具
    private List<GpsModel> mGpsList = new ArrayList<>();//采集数据
    private int count = 0;//次数
    private double point1, point2, point3, point4;//经纬度数据
    private PopupWindow pop;//popupwindow
    private int width = 0;//item的宽度
    private int height = 0;//屏幕的高度
    @ViewInject(R.id.userName)
    private TextView mUserName;//用户名
    @ViewInject(R.id.date)
    private TextView mDate;//日期
    @ViewInject(R.id.time)
    private TextView mTime;//时间
    private Callback.Cancelable cancelable;//回调
    @ViewInject(R.id.punchCard)
    private TextView mPunchCard;//打卡
    @ViewInject(R.id.stepNum)
    private TextView mStepNum;//步数
    private File uploadFile = null;//上传的文件
    private SystemFunUtil imgUtil;//工具栏
    private String imgPath;//图片的路径
    private boolean isBind = false;//记步服务是否绑定
    private boolean isBindLocation = false;//定位服务是否绑定
    private TimerTask timerTask;//定时任务
    private Timer timer;//定时器
    private int firstNum = 0;//第几次进入范围中
    private int leaveNum = 0;//第几次离开范围中
    public static NetBroadcastReceiver.NetEvevt evevt;//广播 监测网络变化
    private LocationDataDao mDbLocation;//数据库
    private int page = 0;//第一条
    private int uploadNum = 100;//每次上传的数据
    private List<LatLng> mHistoryList = new ArrayList<>();//轨迹点数据
    private List<Point> pointList = new ArrayList<>();//轨迹点纠偏数据;

    @Override
    protected void initData() {
        mContext = this;
        mPunchCard.setOnClickListener(this);
        if (http == null) {
            http = new HttpUtil(handler);
        }
        imgUtil = new SystemFunUtil(mContext);
        uploadFile = imgUtil.createRootDirectory("upload");
        mDbLocation = new LocationDataDao(mContext);
        progressDialog = RoundProgressDialog.createDialog(mContext);
        getPersimmions(this);
        mapUtil = new MapUtil(mContext, mMapView);
        // 隐藏缩放控件
        mapUtil.hidezoomView();

        mUserName.setText(MyApplication.userModel.getUserName());
        String[] split = MyApplication.userModel.getLastTime().split(" ");
        String[] str = split[0].split("-");
        mDate.setText(str[0] + "年" + str[1] + "月" + str[2] + "日");
        mTime.setText(split[1].substring(0, 5));

        String latLng1 = MyApplication.userModel.getRange().split(";")[0];
        String latLng2 = MyApplication.userModel.getRange().split(";")[1];
        point1 = Double.parseDouble(latLng1.split(",")[1]);
        point2 = Double.parseDouble(latLng1.split(",")[0]);
        point3 = Double.parseDouble(latLng2.split(",")[1]);
        point4 = Double.parseDouble(latLng2.split(",")[0]);

        LatLng top = new LatLng(point1, point2);
        LatLng left = new LatLng(point1, point4);
        LatLng right = new LatLng(point3, point2);
        LatLng bottom = new LatLng(point3, point4);

//        mapUtil.drawRectangle(top,left,right,bottom);

        /**
         * 这里判断当前设备是否支持计步
         */
        if (StepCountCheckUtil.isSupportStepCountSensor(this)) {
            setupService();
        } else {
            mStepNum.setText("0");
        }

        if (!GpsUtil.isGPSEnable(mContext)) {
            GpsUtil.openGPSSettings(mContext);
        }
        mDbLocation.deleteCurData(DateUtil.getCurrentTime());//删除以前数据
        startLoctionService();
//        uploadData();
//        drawTrace();

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (!cancelable.isCancelled()) {
                    cancelable.cancel();
                }
                ImageUtil.deleteFolder(uploadFile);
            }
        });

    }

    /**
     * 开启计步服务
     */
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            /**
             * 设置定时器，每个三秒钟去更新一次运动步数
             */
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, RequestCode.MSG_FROM_CLIENT);
                        msg.replyTo = new Messenger(handler);
                        messenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            timer = new Timer();
            timer.schedule(timerTask, 0, 3000);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferencesUtil.getDestroy(mContext, "isDestroy")) {
            uploadInfo(MyApplication.userModel.getUserID(), "3", PreferencesUtil.getStringData(mContext, "DestroyTime"));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBind) {
            this.unbindService(conn);
        }
        if (isBindLocation){
            this.unbindService(connLocation);
        }
        mapUtil.clear();
        mHistoryList.clear();
        pointList.clear();
        timerTask.cancel();
        timer.cancel();
        PreferencesUtil.isDestroy(mContext, "isDestroy", true);
        PreferencesUtil.setStringData(mContext, "DestroyTime", DateUtil.getSWAHDate());
    }

    /**
     * 启动定位服务
     */
    private void startLoctionService(){
        // 启动服务
        Intent locationIntent = new Intent(this, LocationService.class);
        isBindLocation = bindService(locationIntent, connLocation, Context.BIND_AUTO_CREATE);
        startService(locationIntent);
    }

    ServiceConnection connLocation = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, RequestCode.GPS_FROM_CLIENT);
                        msg.replyTo = new Messenger(handler);
                        messenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            timer = new Timer();
            timer.schedule(timerTask, 0, MyApplication.gpsTime);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 每隔一分钟上传数据
     */
    private void uploadData(){
        List<GpsModel> list = mDbLocation.getCurrPageData(page, uploadNum,DateUtil.getCurrentTime());
        //上传服务器数据
        mGpsList.clear();
        if (list.size()>0){
            for (GpsModel m: list){
                GpsModel model = new GpsModel();
                if (m.getIsUpload().equals("2")){//没有上传到服务器
                    model.setLat(m.getLat());
                    model.setLng(m.getLng());
                    model.setTime(m.getTime());
                    model.setNote(m.getNote());
                    mGpsList.add(model);
                }
            }
            if (mGpsList.size()>0){
                uploadData(
                        MyApplication.userModel.getUserID(), JSON.toJSONString(mGpsList)
                );
            }else{
                page = page + 100;
                uploadNum = uploadNum + 100;
            }
        }else{
            page = 0;
            uploadNum = 100;
        }
    }

    /**
     * 画轨迹
     */
    private void drawTrace(){
        try {
            List<GpsModel> hList = mDbLocation.getHistoryData(DateUtil.getCurrentAgeTime(1),DateUtil.getSWAHDate());
            mHistoryList.clear();
            pointList.clear();
            Log.e("result","hlist;"+hList.size());
            if (hList.size()>0){
                for (GpsModel m:hList){
                    if (m.getNote().equals("")){
                        if (!mapUtil.isZeroPoint(Double.parseDouble(m.getLat()),Double.parseDouble(m.getLng()))){
                            Point p = new Point();
                            p.setLat(Double.parseDouble(m.getLat()));
                            p.setLng(Double.parseDouble(m.getLng()));
                            p.setTime(m.getTime());
                            p.setUnixTime(DateUtil.parseDate(m.getTime()).getTime()/1000);
                            pointList.add(p);
                        }
                    }
                }
                List<Point> points = SmoothTrack.doSmooth(pointList, 0.1, 1);
                List<Point> points1 = SmoothTrack.correcteZShape(points);
                List<Point> points2 = SmoothTrack.doSmooth(points1, 0.1, 1);
                for (Point p: points2){
                    LatLng latLng = new LatLng(p.getLat(),p.getLng());
                    mHistoryList.add(latLng);
                }
                Log.e("result","point:"+ mHistoryList.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (mHistoryList.size()>0){
                mapUtil.drawHistoryTrack(mHistoryList);
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HttpUtil.SUCCESS:
                    if (msg.arg1 == RequestCode.PUNCHCARD){
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();// 关闭进度条
                        }
                        ImageUtil.deleteFolder(uploadFile);
                        ResultModel model = (ResultModel) ParserUtil.jsonToObject(msg.obj.toString(),ResultModel.class);
                        if ("1".equals(model.getResult())){
                            ToastUtil.showBottomShort(mContext,"打卡成功");
                        }else{
                            ToastUtil.showBottomShort(mContext,model.getErrorMsg());
                        }
                    }
                    if (msg.arg1 == RequestCode.UPLOADINFO){
                        ResultModel model = (ResultModel) ParserUtil.jsonToObject(msg.obj.toString(),ResultModel.class);
                        if ("1".equals(model.getResult())){
                            if(PreferencesUtil.getDestroy(mContext,"isDestroy")){
                                PreferencesUtil.isDestroy(mContext,"isDestroy",false);
                                PreferencesUtil.setStringData(mContext,"DestroyTime","");
                            }
                        }
                    }
                    if (msg.arg1 == RequestCode.REPORTDATA){
                        ResultModel model = (ResultModel) ParserUtil.jsonToObject(msg.obj.toString(),ResultModel.class);
                        if ("1".equals(model.getResult())){
                            for (GpsModel m : mGpsList){
                                m.setIsUpload("1");
                                mDbLocation.updateCurData(m);
                            }
                            page = page + mGpsList.size()-1;
                            uploadNum = uploadNum + mGpsList.size()-1;
                        }else{
                        }
                    }
                    break;
                case HttpUtil.EMPTY:
                    break;
                case HttpUtil.FAILURE:
                    ToastUtil.showBottomShort(mContext, RequestCode.ERRORINFO);
                    break;
                case HttpUtil.LOADING:
                    if (msg.arg1 == RequestCode.PUNCHCARD) {
                        progressDialog.setMessage(msg.arg2 + "%");
                    }
                    break;
                case RequestCode.MSG_FROM_SERVER:
                    //记录运动步数
                    int steps = msg.getData().getInt("steps");
                    //设置的步数
                    mStepNum.setText(String.valueOf(steps));
                    break;
                case RequestCode.GPS_FROM_SERVER:
                    double lat = msg.getData().getDouble("lat");
                    double lng = msg.getData().getDouble("lng");
                    LatLng gpsLatlng = new LatLng(lat,lng);
                    LatLng latLng = mapUtil.changeBaiduByGPS(gpsLatlng);
                    uploadResult(latLng);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 上报
     *
     */
    private void uploadData(String userID,String data) {
        Log.e("jack","data:"+data);
        if (MyApplication.getNetObject().isNetConnected()) {
            RequestParams params = http.getParams(WebUrlConfig.reportData());
            params.addBodyParameter("userID",userID);
            params.addBodyParameter("data",data);
            http.sendPost(RequestCode.REPORTDATA, params);
        } else {
//            ToastUtil.showBottomShort(mContext, RequestCode.NOLOGIN);
        }
    }

    /**
     * 打卡
     */
    private void punchCard(String userID,String lat,String lng,String imgPath) {
//        lat = "41.853754";
//        lng = "123.476113";
        if (MyApplication.getNetObject().isNetConnected()) {
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.setMessage("加载中...");
                progressDialog.show();
            }
            RequestParams params = http.getParams(WebUrlConfig.punchCard());
            params.addBodyParameter("userID",userID);
            params.addBodyParameter("lat",lat);
            params.addBodyParameter("lng",lng);
            if (!TextUtils.isEmpty(imgPath)){
                params.addBodyParameter("img", new File(imgPath));
            }
            cancelable = http.uploadFile(RequestCode.PUNCHCARD, params);
        } else {
            ToastUtil.showBottomShort(mContext, RequestCode.NOLOGIN);
        }
    }

    /**
     * 上报各种信息
     *
     */
    private void uploadInfo(String userID,String type,String time) {
        if (MyApplication.getNetObject().isNetConnected()) {
            http.sendGet(RequestCode.UPLOADINFO, WebUrlConfig.uploadInfo(userID, type,time));
        } else {
            ToastUtil.showBottomShort(mContext, RequestCode.NOLOGIN);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mPunchCard) {
//            imgUtil.openCamera(SystemFunUtil.SYSTEM_IMAGE_PREPOSITION,101);
            Intent intent = new Intent(mContext,PictureActivity.class);
            startActivityForResult(intent,111);
        }
    }

    /**
     * 初始化pop数据
     */
    private void initPopupWindow() {
        pop = new PopupWindow(this);
        View customView = getLayoutInflater().inflate(R.layout.pop_layout, null);
        TextView name = (TextView) customView.findViewById(R.id.name);
        TextView time = (TextView) customView.findViewById(R.id.time);
        name.setText(MyApplication.userModel.getUserName());
        time.setText(MyApplication.userModel.getLastTime());
        pop.setContentView(customView);
        pop.setWidth(width / 3);
        pop.setHeight(height/4);
        pop.setFocusable(true);
        pop.setBackgroundDrawable(new BitmapDrawable());
        backgroundAlpha(1f);
        Runnable r = new Runnable() {//默认弹出

            @Override
            public void run() {
                if(null != mContext.getWindow().getDecorView().getWindowToken()) {
//                    pop.showAsDropDown(mPopLayout);
                    handler.removeCallbacks(this);
                }else {
                    handler.postDelayed(this, 5);
                }
            }
        };
        handler.post(r);
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

    /**
     *	退出activity
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序!",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                //退出所有的activity
                Intent intent = new Intent();
                intent.setAction(BaseActivity.TAG_ESC_ACTIVITY);
                sendBroadcast(intent);
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @TargetApi(23)
    private void getPersimmions(Activity context) {
        int permission = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, 1);
        } else {//已经获取了权限
        }
    }

    /**
     * 上传结果
     * @param location
     */
    private void uploadResult(LatLng location){
        GpsModel model = new GpsModel();
        if (!mapUtil.isZeroPoint(location.latitude,location.longitude)){
            if (location.latitude>=point1&&location.longitude<=point4){//范围内上传信息
                model.setLat(String.valueOf(location.latitude));
                model.setLng(String.valueOf(location.longitude));
                model.setNote("");
                firstNum+=1;
            }else{
                model.setNote("不在范围内");
                leaveNum+=1;
            }
        }else{
            model.setNote("无法定位到自己位置");
        }
        model.setTime(DateUtil.getSWAHDate());
        model.setIsUpload("2");
        mDbLocation.addNewData(model);//保存

        if (firstNum==1){
            uploadInfo(MyApplication.userModel.getUserID(),"1",DateUtil.getSWAHDate());
        }
        if (leaveNum == 1){
            uploadInfo(MyApplication.userModel.getUserID(),"2",DateUtil.getSWAHDate());
        }
        if (count==0){
            if (!mapUtil.isZeroPoint(location.latitude,location.longitude)){
                latLng = new LatLng(location.latitude, location.longitude);
                mapUtil.updateStatus(latLng,true);
            }
        }
        count+=1;
        if (count==Integer.parseInt(MyApplication.userModel.getUploadRate())/Integer.parseInt(MyApplication.userModel.getGpsRate())) {
            drawTrace();//每个一分钟更新轨迹
            uploadData();//每隔一分钟上传数据
            count=0;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 111){
            Log.e("result","path:"+data.getStringExtra("path"));
            try{
                String path = data.getStringExtra("path");

                Bitmap bt = ImageUtil.getSmallBitmap(path);//压缩上传
                FileNames names = new FileNames();
                imgPath = ImageUtil.saveBitmap(uploadFile.getPath(),bt,names.getImageName());
                Log.e("jack","path:"+ imgPath);
                //删除原有图片
                ImageUtil.deleteFilePath(path);

                punchCard(MyApplication.userModel.getUserID(),
                        String.valueOf( MyApplication.lat),
                        String.valueOf( MyApplication.lng),
                        imgPath);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 101){//相机
                try{
                    File imgFile = imgUtil.getImgFile();

                    Bitmap bt = ImageUtil.getSmallBitmap(imgFile.getPath());//压缩上传
                    FileNames names = new FileNames();
                    imgPath = ImageUtil.saveBitmap(uploadFile.getPath(),bt,names.getImageName());
                    Log.e("jack","path:"+ imgPath);
                    //删除原有图片
                    ImageUtil.deleteFilePath(imgFile.getPath());

                    punchCard(MyApplication.userModel.getUserID(),
                            String.valueOf( MyApplication.lat),
                            String.valueOf( MyApplication.lng),
                            imgPath);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}
