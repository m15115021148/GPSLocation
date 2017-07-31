package com.sitemap.railwaylocation.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.sitemap.railwaylocation.R;
import com.sitemap.railwaylocation.application.MyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc 地图处理 工具类
 * Created by chenmeng on 2017/3/23.
 */

public class MapUtil {
    private Context mContext;
    private MapView mMapView;
    private BaiduMap mBaiduMap;//百度地图对象
    private BaiduMap.OnMarkerClickListener markClick;// 地图标注点击事件
    private float level = 20;//地图显示等级
    /**
     * 路线覆盖物
     */
    public Overlay polylineOverlay = null;
    private MapStatus mapStatus = null;
    private Marker mMoveMarker = null;
    public LatLng lastPoint = null;

    public MapUtil(Context context,MapView mMapView){
        this.mContext = context;
        this.mMapView = mMapView;
        this.mBaiduMap = mMapView.getMap();
    }

    /**
     * 隐藏图标
     */
    public void hidezoomView() {
        // 隐藏logo
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }
        //地图上比例尺
        mMapView.showScaleControl(false);
        // 隐藏缩放控件
        mMapView.showZoomControls(false);
    }

    public void updateStatus(LatLng currentPoint, boolean showMarker) {
        if (null == mBaiduMap || null == currentPoint) {
            return;
        }

        if (null != mBaiduMap.getProjection()) {
            Point screenPoint = mBaiduMap.getProjection().toScreenLocation(currentPoint);
            // 点在屏幕上的坐标超过限制范围，则重新聚焦底图
            if (screenPoint.y < 200 || screenPoint.y > MyApplication.screenHeight - 500
                    || screenPoint.x < 200 || screenPoint.x > MyApplication.screenWidth - 200
                    || null == mapStatus) {
                animateMapStatus(currentPoint, level);
            }
        } else if (null == mapStatus) {
            // 第一次定位时，聚焦底图
            setMapStatus(currentPoint, level);
        }

        if (showMarker) {
            addMarker(currentPoint);
        }

    }

    public void clear(){
        if (null != mMoveMarker) {
            mMoveMarker.remove();
        }
        if (null != polylineOverlay) {
            polylineOverlay.remove();
        }
        if (null != mBaiduMap) {
            mBaiduMap.clear();
        }
    }

    public void setMapStatus(LatLng point, float zoom) {
        MapStatus.Builder builder = new MapStatus.Builder();
        mapStatus = builder.target(point).zoom(zoom).build();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }

    /**
     * 添加地图覆盖物
     */
    public void addMarker(LatLng currentPoint) {
        if (null == mMoveMarker) {
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.icon_point);
            mMoveMarker = addOverlay(currentPoint, bitmap, null);
            return;
        }

        if (null != lastPoint) {
            moveLooper(currentPoint);
        } else {
            lastPoint = currentPoint;
            mMoveMarker.setPosition(currentPoint);
        }
    }

    /**
     * 移动逻辑
     */
    public void moveLooper(LatLng endPoint) {

        mMoveMarker.setPosition(lastPoint);
        mMoveMarker.setRotate((float)getAngle(lastPoint, endPoint));

        double slope = getSlope(lastPoint, endPoint);
        // 是不是正向的标示（向上设为正向）
        boolean isReverse = (lastPoint.latitude > endPoint.latitude);
        double intercept = getInterception(slope, lastPoint);
        double xMoveDistance = isReverse ? getXMoveDistance(slope) : -1 * getXMoveDistance(slope);

        for (double latitude = lastPoint.latitude; latitude > endPoint.latitude == isReverse; latitude =
                latitude - xMoveDistance) {
            LatLng latLng;
            if (slope != Double.MAX_VALUE) {
                latLng = new LatLng(latitude, (latitude - intercept) / slope);
            } else {
                latLng = new LatLng(latitude, lastPoint.longitude);
            }
            mMoveMarker.setPosition(latLng);
        }
    }

    public Marker addOverlay(LatLng currentPoint, BitmapDescriptor icon, Bundle bundle) {
        OverlayOptions overlayOptions = new MarkerOptions().position(currentPoint)
                .icon(icon).zIndex(9).draggable(true);
        Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);
        if (null != bundle) {
            marker.setExtraInfo(bundle);
        }
        return marker;
    }

    /**
     * 画矩形
     * @param top       左上角
     * @param left    左下角
     * @param right  右上角
     * @param bottom  右下角
     */
    public void drawRectangle(LatLng top,LatLng left,LatLng right,LatLng bottom){
        //矩形的点
        List<LatLng> pts = new ArrayList<>();
        pts.add(top);
        pts.add(right);
        pts.add(bottom);
        pts.add(left);

        //构建用户绘制多边形的Option对象
        OverlayOptions polygonOption = new PolygonOptions()
                .points(pts)
                .stroke(new Stroke(6, 0xAA00FF00))
                .fillColor(0xAAFFFF00);
        //在地图上添加多边形Option，用于显示
        mBaiduMap.addOverlay(polygonOption);
    }

    public boolean isZeroPoint(double lat,double lng){
        if (lat!=4.9E-324 && lng!=4.9E-324 && lat!=0 && lng!=0 && lat!=-3.067393572659021E-8 && lng!=2.890871144776878E-9){
            return false;
        }
        return true;
    }

    /**
     * 别的地图转百度坐标
     *
     * @param ll
     * @return
     */
    public LatLng changeBaidu(LatLng ll) {
        // 将google地图、soso地图、aliyun地图、mapabc地图和amap地图// 所用坐标转换成百度坐标
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.COMMON);
        // sourceLatLng待转换坐标
        converter.coord(ll);
        LatLng desLatLng = converter.convert();
        Log.i("TAG", "latitude:" + desLatLng.latitude + "longitude:" + desLatLng.longitude);
        return desLatLng;
    }

    /**
     * 原始GPS转百度地图
     * @param ll
     * @return
     */
    public LatLng changeBaiduByGPS(LatLng ll) {
        // 将GPS设备采集的原始GPS坐标转换成百度坐标
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标
        converter.coord(ll);
        LatLng desLatLng = converter.convert();
        Log.i("TAG", "latitude:" + desLatLng.latitude + " longitude:" + desLatLng.longitude);
        return desLatLng;
    }

    /**
     * 绘制历史轨迹
     */
    public void drawHistoryTrack(List<LatLng> points) {
        // 绘制新覆盖物前，清空之前的覆盖物
        mBaiduMap.clear();
        if (points == null || points.size() == 0) {
            if (null != polylineOverlay) {
                polylineOverlay.remove();
                polylineOverlay = null;
            }
            return;
        }
        // 构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.icon_point);

        if (points.size() == 1) {
            OverlayOptions startOptions = new MarkerOptions().position(points.get(0)).icon(bitmap)
                    .zIndex(9).draggable(true);
            mBaiduMap.addOverlay(startOptions);
            animateMapStatus(points.get(0), level);
            return;
        }

        LatLng startPoint;
        LatLng endPoint;

        startPoint = points.get(0);
        endPoint = points.get(points.size() - 1);

        // 构建Marker图标
        BitmapDescriptor startBt = BitmapDescriptorFactory.fromResource(R.mipmap.icon_start);
        BitmapDescriptor endBt = BitmapDescriptorFactory.fromResource(R.mipmap.icon_end);

        // 添加起点图标
        OverlayOptions startOptions = new MarkerOptions()
                .position(startPoint).icon(startBt)
                .zIndex(9).draggable(true);
        // 添加终点图标
        OverlayOptions endOptions = new MarkerOptions().position(endPoint)
                .icon(endBt).zIndex(9).draggable(true);

        // 添加路线（轨迹）
        OverlayOptions polylineOptions = new PolylineOptions().width(10)
                .color(Color.BLUE).points(points);

        mBaiduMap.addOverlay(startOptions);
        mBaiduMap.addOverlay(endOptions);
        polylineOverlay = mBaiduMap.addOverlay(polylineOptions);

        OverlayOptions markerOptions =
                new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(bitmap)
                        .position(points.get(points.size() - 1))
                        .rotate((float) getAngle(points.get(0), points.get(1)));
        mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);

        animateMapStatus(points);
    }

    public void animateMapStatus(List<LatLng> points) {
        if (null == points || points.isEmpty()) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        MapStatusUpdate msUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build());
        mBaiduMap.animateMapStatus(msUpdate);
    }

    public void animateMapStatus(LatLng point, float zoom) {
        MapStatus.Builder builder = new MapStatus.Builder();
        mapStatus = builder.target(point).zoom(zoom).build();
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        return 180 * (radio / Math.PI) + deltAngle - 90;
    }

    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        return (toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude);
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {
        return point.latitude - slope * point.longitude;
    }

    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return 0.0001;
        }
        return Math.abs((0.0001 * slope) / Math.sqrt(1 + slope * slope));
    }

}
