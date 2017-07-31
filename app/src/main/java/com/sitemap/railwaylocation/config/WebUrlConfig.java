package com.sitemap.railwaylocation.config;

/**
 * @author zhangfan
 * @ClassName: WebUrlConfig.java
 * @Description: 网络url（接口）配置文件
 * @Date 2017-1-11
 */

public class WebUrlConfig {
    private static final String HOST_NAME = WebHostConfig.getHostName();
    private static final String LOGIN = HOST_NAME + "dbAction_login.do?";//登录
    private static final String REPORTDATA = HOST_NAME + "dbAction_reportData.do?";//上传
    private static final String PUNCHCARD = HOST_NAME + "dbAction_clock.do?";//打卡
    private static final String UPDATEVERSION = HOST_NAME + "dbAction_updateVersion.do?";//更新接口
    private static final String UPLOADINFO = HOST_NAME + "dbAction_uploadInfo.do?";//上传各种信息

    /**
     * 登录
     * @return
     */
    public static String login(){
        return LOGIN;
    }

    /**
     * 上传
     * @return
     */
    public static String reportData(){
        return REPORTDATA ;
    }

    /**
     * 打卡
     * @return
     */
    public static String punchCard(){
        return PUNCHCARD;
    }

    /**
     * 版本更新
     * @param versionCode
     * @return
     */
    public static String updateVersion(String versionCode){
        return UPDATEVERSION + "versionCode="+versionCode;
    }

    /**
     * 上传各种信息
     * @param userID
     * @param type
     * @param time
     * @return
     */
    public static String uploadInfo(String userID,String type,String time){
        return UPLOADINFO + "userID="+userID+"&type="+type+"&time="+time;
    }

}

	