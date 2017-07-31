package com.sitemap.railwaylocation.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sitemap.railwaylocation.model.GpsModel;
import com.sitemap.railwaylocation.model.StepEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Date : 2017/3/24
 * To do : 定位数据保存
 */

public class LocationDataDao {
    private DBOpenHelper locationHelper;
    private SQLiteDatabase locationDb;
    private String TABLE = "location";

    public LocationDataDao(Context context) {
        locationHelper = new DBOpenHelper(context);
    }

    /**
     * 添加一条新记录
     *
     * @param model
     */
    public void addNewData(GpsModel model) {
        locationDb = locationHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put("lat", model.getLat());
        values.put("lng",  model.getLng());
        values.put("time",  model.getTime());
        values.put("note",  model.getNote());
        values.put("isUpload",  model.getIsUpload());

        locationDb.insert(TABLE, null, values);
        locationDb.close();
    }

    /**
     * 查询所有的记录 按时间排序
     *
     * @return
     */
    public List<GpsModel> getAllDatas() {
        List<GpsModel> dataList = new ArrayList<>();
        locationDb = locationHelper.getReadableDatabase();
        Cursor cursor = locationDb.rawQuery("select * from location order by time asc", null);

        while (cursor.moveToNext()) {
            String lat = cursor.getString(cursor.getColumnIndex("lat"));
            String lng = cursor.getString(cursor.getColumnIndex("lng"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String note = cursor.getString(cursor.getColumnIndex("note"));
            String isUpload = cursor.getString(cursor.getColumnIndex("isUpload"));
            GpsModel entity = new GpsModel();
            entity.setLat(lat);
            entity.setLng(lng);
            entity.setTime(time);
            entity.setNote(note);
            entity.setIsUpload(isUpload);
            dataList.add(entity);
        }
        cursor.close();
        //关闭数据库
        locationDb.close();
        return dataList;
    }

    /**
     * 更新数据  是否上传 是否上传到服务器  1是  2否
     * @param model
     */
    public void updateCurData(GpsModel model) {
        locationDb = locationHelper.getReadableDatabase();
        String sql = "update location set isUpload ="+model.getIsUpload();

        ContentValues values = new ContentValues();
        values.put("lat", model.getLat());
        values.put("lng",  model.getLng());
        values.put("time",  model.getTime());
        values.put("note",  model.getNote());
        values.put("isUpload",  model.getIsUpload());

//        locationDb.update(TABLE, values, "isUpload=?", new String[]{model.getIsUpload()});
        if (locationDb.isOpen())
            locationDb.execSQL(sql);
        locationDb.close();
    }


    /**
     * 删除指定日期的记录
     *
     * @param curDate
     */
    public void deleteCurData(String curDate) {
        locationDb = locationHelper.getReadableDatabase();
        if (locationDb.isOpen())
            locationDb.delete(TABLE, "time<?", new String[]{curDate});
        Log.e("result","delete:"+"删除成功");
        locationDb.close();
    }

    /**
     * 总记录
     * @return
     */
    public int getTotalPage(){
        int sum = 0;
        locationDb = locationHelper.getReadableDatabase();
        String sql = "select count(*) as totalRecord from location";
        Cursor cursor = locationDb.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String totalRecord = cursor.getString(cursor.getColumnIndex("totalRecord"));
            sum = Integer.parseInt(totalRecord);
        }
        cursor.close();
        //关闭数据库
        locationDb.close();
        return sum;
    }

    /**
     * 查询指定的记录 分页查询
     * @param first
     * @param end
     * @return
     */
    public List<GpsModel> getCurrPageData(int first,int end,String currTime){
        List<GpsModel> list = new ArrayList<>();
        String sql = "select * from location where time>=? order by time asc , _id limit ?,?";
        locationDb = locationHelper.getReadableDatabase();
        Cursor cursor = locationDb.rawQuery(sql, new String[]{currTime,String.valueOf(first), String.valueOf(end)} );
        while (cursor.moveToNext()) {
            String lat = cursor.getString(cursor.getColumnIndex("lat"));
            String lng = cursor.getString(cursor.getColumnIndex("lng"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String note = cursor.getString(cursor.getColumnIndex("note"));
            String isUpload = cursor.getString(cursor.getColumnIndex("isUpload"));
            GpsModel entity = new GpsModel();
            entity.setLat(lat);
            entity.setLng(lng);
            entity.setTime(time);
            entity.setNote(note);
            entity.setIsUpload(isUpload);
            list.add(entity);
        }
        cursor.close();
        //关闭数据库
        locationDb.close();
        return list;
    }

    /**
     * 查询指定的记录
     * @return
     */
    public List<GpsModel> getHistoryData(String data,String currTime){
        List<GpsModel> list = new ArrayList<>();
        String sql = "select * from location where time>=? and time <=? order by time asc";
        locationDb = locationHelper.getReadableDatabase();
        Cursor cursor = locationDb.rawQuery(sql, new String[]{data,currTime} );
        while (cursor.moveToNext()) {
            String lat = cursor.getString(cursor.getColumnIndex("lat"));
            String lng = cursor.getString(cursor.getColumnIndex("lng"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String note = cursor.getString(cursor.getColumnIndex("note"));
            GpsModel entity = new GpsModel();
            entity.setLat(lat);
            entity.setLng(lng);
            entity.setTime(time);
            entity.setNote(note);
            list.add(entity);
        }
        cursor.close();
        //关闭数据库
        locationDb.close();
        return list;
    }

}
