package com.sitemap.railwaylocation.model;

/**
 * 登录实体类
 * Created by chenMeng on 2017/6/6.
 */

public class UserModel {
    private String result;
    private String errorMsg;
    private String userID;
    private String gpsRate;
    private String uploadRate;
    private String range;
    private String lastTime;//登录时间
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public String getGpsRate() {
        return gpsRate;
    }

    public void setGpsRate(String gpsRate) {
        this.gpsRate = gpsRate;
    }

    public String getUploadRate() {
        return uploadRate;
    }

    public void setUploadRate(String uploadRate) {
        this.uploadRate = uploadRate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
