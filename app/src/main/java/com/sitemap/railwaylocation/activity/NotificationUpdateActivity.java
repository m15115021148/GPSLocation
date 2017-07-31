package com.sitemap.railwaylocation.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.sitemap.railwaylocation.R;
import com.sitemap.railwaylocation.application.MyApplication;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 **
 * @author chenmeng 更新类 create at 2015年12月30日 下午4:49:01
 */
@ContentView(R.layout.update)
public class NotificationUpdateActivity extends BaseActivity implements OnClickListener {
	@ViewInject(R.id.cancel)
	private Button btn_cancel;// 关闭按钮
	@ViewInject(R.id.currentPos)
	private TextView tv_progress;// 正在下载
	@ViewInject(R.id.download_version)
	private TextView download_version;// 版本号
	@ViewInject(R.id.download_size)
	private TextView download_size;// apk大小
	@ViewInject(R.id.progressbar1)
	private ProgressBar mProgressBar;// 进度条
	private boolean isDestroy = true;// 是否销毁
	// 获取到下载url后，直接复制给MapApp,里面的全局变量
	private MyApplication app;// application对象
	private DownloadService.DownloadBinder binder;// 下载服务
	private boolean isBinded;// 是否开启

	/**
	 * 初始化数据
	 */
	@Override
	protected void initData(){
		btn_cancel.setOnClickListener(this);
		app = (MyApplication) getApplication();
		download_version.setText(MyApplication.versionName);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isDestroy && app.isDownload()) {
			Intent it = new Intent(NotificationUpdateActivity.this,
					DownloadService.class);
			startService(it);
			bindService(it, conn, Context.BIND_AUTO_CREATE);
		}
		Log.w("result"," notification  onresume");
	}

	/**
	 * 服务链接
	 */
	ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			isBinded = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (DownloadService.DownloadBinder) service;
			Log.w("result","服务启动!!!");
			// 开始下载
			isBinded = true;
			binder.addCallback(callback);
			binder.start();

		}
	};

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (isDestroy && app.isDownload()) {
			Log.i("result", "移动了");
			Intent it = new Intent(NotificationUpdateActivity.this,
					DownloadService.class);
			startService(it);
			bindService(it, conn, Context.BIND_AUTO_CREATE);
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		isDestroy = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isBinded) {
			unbindService(conn);
		}
		if (binder != null && binder.isCanceled()) {
			Log.w("result"," onDestroy  stopservice");
			Intent it = new Intent(this, DownloadService.class);
			stopService(it);
		}
	}

	/**
	 * 返回更新进度条
	 */
	private ICallbackResult callback = new ICallbackResult() {

		@Override
		public void OnBackResult(Object result) {
			if ("finish".equals(result)) {
				Log.i("result", "下载完成了！");
				SplashActivity.mContext.finish();
				finish();
				return;
			}

			int i = (Integer) result;
			mProgressBar.setProgress(i);
			mHandler.sendEmptyMessage(i);
		}

	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			tv_progress.setText("当前进度 ： " + msg.what + "%");
			java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");
			download_size.setText(df.format(MyApplication.length/1024) + "MB");
		};
	};

	@Override
	public void onClick(View v) {
		if (v == btn_cancel){
			SplashActivity.mContext.finish();
			finish();
		}
	}

	public interface ICallbackResult {
		void OnBackResult(Object result);
	}
	
	/**
	 * 返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
