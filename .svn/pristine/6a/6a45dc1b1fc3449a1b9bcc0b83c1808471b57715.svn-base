package com.sitemap.railwaylocation.activity;


import android.content.Intent;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.view.SurfaceView;
import android.view.View;

import com.sitemap.railwaylocation.R;
import com.sitemap.railwaylocation.util.FileNames;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

@ContentView(R.layout.activity_picture)
public class PictureActivity extends BaseActivity implements View.OnClickListener,SurfaceHolder.Callback  {
    private PictureActivity mContext;//本类
    @ViewInject(R.id.take_photo)
    private ImageView mTakePhoto;
    @ViewInject(R.id.my_surfaceView)
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private String mImgPath;
    @ViewInject(R.id.cancel)
    private ImageView mCancel;//取消
    @ViewInject(R.id.sure)
    private ImageView mSure;//确定
    private byte[] newData;//数据
    @ViewInject(R.id.layout)
    private RelativeLayout mLayout;//布局

    /* 图像数据处理还未完成时的回调函数 */
    private Camera.ShutterCallback mShutter = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // 一般显示进度条
        }
    };

    /* 图像数据处理完成后的回调函数 */
    private Camera.PictureCallback mJpeg = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                mTakePhoto.setVisibility(View.GONE);
                mCancel.setVisibility(View.VISIBLE);
                mSure.setVisibility(View.VISIBLE);
                newData = data;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void initData() {
        mContext = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//影藏系统状态栏
        getWindow().addFlags(View.SYSTEM_UI_LAYOUT_FLAGS);//导航键

        mTakePhoto.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mSure.setOnClickListener(this);
        mLayout.setOnClickListener(this);

        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setKeepScreenOn(true);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this); // 回调接口
    }

    @Override
    public void onClick(View v) {
        if (v == mTakePhoto){
            mCamera.takePicture(mShutter, null, mJpeg);
        }
        if (v == mCancel){
            mCancel.setVisibility(View.GONE);
            mSure.setVisibility(View.GONE);
            mTakePhoto.setVisibility(View.VISIBLE);

            mCamera.stopPreview();
            mCamera.startPreview();//数据处理完后继续开始预览
        }
        if (v == mSure){
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(newData, 0, newData.length);
                //自定义文件保存路径  以拍摄时间区分命名
                FileNames name = new FileNames();
                File appFile = createRootDirectory("SystemResources");
                File file = new File(appFile.getPath(),name.getImageName());
                mImgPath = file.getPath();
                Matrix matrix = new Matrix();
                matrix.postScale(1, -1); //镜像水平翻转
                matrix.postRotate(-90);
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩的流里面
                bos.flush();// 刷新此缓冲区的输出流
                bos.close();// 关闭此输出流并释放与此流有关的所有系统资源
                bitmap.recycle();//回收bitmap空间
                newBitmap.recycle();

                Intent intent = new Intent();
                intent.putExtra("path", mImgPath);
                setResult(111,intent);
                mContext.finish();
            }catch (Exception e){
                e.printStackTrace();
            } finally {
                // 释放相机
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
            }
        }
        if (v == mLayout){
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //当surfaceview创建时开启相机
        if(mCamera == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                mCamera = Camera.open(1);
                // i=0 表示后置相机
            } else
                mCamera = Camera.open();
            try {
                mCamera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                mCamera.setDisplayOrientation(90);   //by me rotate 90
                mCamera.startPreview();//开始预览
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // SurfaceView销毁时，取消Camera预览
        if (mCamera != null) {
            //当surfaceview关闭时，关闭预览并释放资源
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mSurfaceView = null;
        }
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        // 释放相机
//        if (mCamera != null) {
//            mCamera.release();
//            mCamera = null;
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放相机
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 创建根目录 自动区分是否在sd卡 还是内部储存
     * @param fileName 根目录 名称
     * @return File
     */
    public File createRootDirectory(String fileName){
        String filePath =  Environment.getExternalStorageDirectory().toString() + "/";
        File file = new File(filePath +  fileName);
        file.mkdir();
        return file;
    }
}