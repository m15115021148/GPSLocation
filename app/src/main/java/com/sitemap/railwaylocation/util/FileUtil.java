package com.sitemap.railwaylocation.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

/*
 * 文件的操作需要的权限
 *  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 * /
/**
 * com.sitemap.stm.file
 * 
 * @author chenemng
 * @Description 文件的操作 创建 写入 读取 删除 压缩
 * @date create at 2016年8月24日 下午3:06:02
 */
public class FileUtil {
    /**
     * sd卡路径
     */
    private static String SDPATH = Environment.getExternalStorageDirectory().getPath() + "/";

    /**
     * 判断sd卡是否存在
     *
     * @return
     */
    public static boolean isExitSD() {
        String state = Environment.getExternalStorageState();
        if (state != null && state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 创建文件夹 在sd卡目录下
     *
     * @param dirName 文件夹的名称
     * @return
     */
    public static File createSDDir(String dirName) {
        File dir = new File(SDPATH + dirName);
        dir.mkdir();
        return dir;
    }

    /**
     * 创建根目录 自动区分是否在sd卡 还是内部储存
     * @param fileName 根目录 名称
     * @return
     */
    public static File createRootDirectory(String fileName){
        String filePath =  Environment.getExternalStorageDirectory().toString() + "/";
        File file = new File(filePath +  fileName);
        file.mkdir();
        return file;
    }

    /**
     * 创建文件夹
     * @param filePath
     * @return
     */
    public static File createFolder(String filePath){
        File file = new File(filePath);
        if (file.exists()){
            return file;
        }else{
            file.mkdir();
            return file;
        }
    }

    /**
     * 创建文件夹 默认sd卡中
     *
     * @param context
     * @param folderName return 返回文件夹的路径
     */
    public static File createFolder(Context context, String folderName) {
//        context.getExternalFilesDir("");
        File dir = null;
        if (isExitSD()) {
            dir = createSDDir(folderName);
        } else {
//            内部存储
            String FILE_PAHT = context.getFilesDir().getPath() + "/";

//            String FILE_PAHT =  Environment .getExternalStorageDirectory().toString() + "/";

            File file = new File(FILE_PAHT + folderName);
            file.mkdir();
            dir = file;
        }
        Log.e("result", "文件夹的目录:" + dir.getPath());
        return dir;
    }

    /**
     * 创建单个文件夹
     *
     * @param parentFile
     * @param folderName
     * @return
     */
    public static File createOneFolder(File parentFile, String folderName) {
        File dir = new File(parentFile, folderName);
        if (dir.exists()) {
            return dir;
        } else {
            dir.mkdir();
            return dir;
        }
    }

    /**
     * 创建单个文件夹
     *
     * @param parentPath
     * @return
     */
    public static File createOneFolder(String parentPath) {
        File dir = new File(parentPath);
        if (dir.exists()) {
            return dir;
        } else {
            dir.mkdir();
            return dir;
        }
    }

    /**
     * 创建内部文件
     *
     * @throws IOException
     */
    public static File createInternalFile(Context context, String fileName)
            throws IOException {
        String FILE_PAHT = context.getFilesDir().getPath() + "/";
        File file = new File(FILE_PAHT + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 在sd卡中创建文件
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static File createSDFile(String fileName) throws IOException {
        File file = new File(SDPATH + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 删除文件(SD卡中)
     *
     * @param fileName
     * @return
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(SDPATH + fileName);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        file.delete();
        return true;
    }

    /**
     * 删除文件(SD卡中)
     *
     * @param filePath
     * @return
     */
    public static boolean deleteFilePath(String filePath) {
        File file = new File(filePath);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        file.delete();
        return true;
    }


    /**
     * 删除文件夹 (SD卡中)
     * 卸载时候 删除
     *
     * @param name
     * @return
     */
    public static boolean deleteDir(String name) {
        File file = new File(SDPATH + name);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        deleteFolder(file);
        return true;
    }

    /**
     * 删除文件夹
     *
     * @param filePath
     * @return
     */
    public static boolean deleteFolder(String filePath) {
        File file = new File(filePath);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        deleteFolder(file);
        return true;
    }

    /**
     * 递归删除该文件夹下的所有文件
     *
     * @param folder
     */
    public static void deleteFolder(File folder) {
        File[] fileArray = folder.listFiles();
        if (fileArray==null){
            return;
        }
        if (fileArray.length == 0) {
            // 空文件夹则直接删除
            folder.delete();
        } else {
            // 遍历该目录
            for (File currentFile : fileArray) {
                if (currentFile.exists() && currentFile.isFile()) {
                    // 文件则直接删除
                    currentFile.delete();
                } else {
                    // 递归删除
                    deleteFolder(currentFile);
                }
            }
            folder.delete();
        }
    }

    /**
     * 创建不同编号同名的文件
     *
     * @param path
     * @param name
     * @return
     * @throws IOException
     */
    public boolean createFile(String path, String name) throws IOException {
        File file = new File(path, name);
        //判断文件是否存在
        if (!file.exists()) {
            file.createNewFile();
            return true;
        } else {
            int i = 1;
            while (true) {
                //创建不同编号的同名
                String Tempname = name + "(" + i + ")";
                File fileTemp = new File(path, Tempname);
                if (fileTemp.exists()) {
                    i++;
                } else {
                    return createSameNameFile(path, name);
                }
            }

        }
    }

    /**
     * 创建不同编号同名的文件
     *
     * @param path
     * @param name
     * @return
     * @throws IOException
     */
    public boolean createSameNameFile(String path, String name) throws IOException {
        int i = 1;
        while (true) {
            //创建不同编号的同名
            String Tempname = name + "(" + i + ")";
            File fileTemp = new File(path, Tempname);
            if (fileTemp.exists()) {
                i++;
            } else {
                fileTemp.createNewFile();
                return true;
            }
        }
    }

    /**
     * 创建不同编号同名的文件夹
     *
     * @param path
     * @param name
     * @return
     * @throws IOException
     */
    public static boolean createFolder(String path, String name) {
        File file = new File(path, name);
        //判断文件是否存在
        if (!file.exists()) {
            file.mkdir();
            return true;
        } else {
            int i = 1;
            while (true) {
                //创建不同编号的同名
                String tempName = name + "(" + i + ")";
                File fileTemp = new File(path, tempName);
                if (fileTemp.exists()) {
                    i++;
                } else {
                    fileTemp.mkdir();
                    return true;
                }
            }
        }
    }

    /**
     * 创建不同编号同名的文件夹
     *
     * @param parentFile
     * @param name
     * @return
     * @throws IOException
     */
    public static File createFolder(File parentFile, String name) {
        File file = new File(parentFile, name);
        //判断文件是否存在
        if (!file.exists()) {
            file.mkdir();
            return file;
        } else {
            int i = 1;
            while (true) {
                //创建不同编号的同名
                String tempName = name + "(" + i + ")";
                file = new File(parentFile, tempName);
                if (file.exists()) {
                    i++;
                } else {
                    file.mkdir();
                    return file;
                }
            }
        }
    }


    /**
     * 文件的重命名
     *
     * @param oldFileName
     * @param newFileName
     * @return
     */
    public boolean renameSDFile(String oldFileName, String newFileName) {
        File oleFile = new File(SDPATH + oldFileName);
        File newFile = new File(SDPATH + newFileName);
        return oleFile.renameTo(newFile);
    }

    /**
     * 文件夹 的重命名
     *
     * @param oldFilePath
     * @param newFilePath
     * @return
     */
    public static boolean renameFile(String oldFilePath, String newFilePath) {
        File oleFile = new File(oldFilePath);
        File newFile = new File(newFilePath);
        return oleFile.renameTo(newFile);
    }

    /**
     * 拷贝一个文件,srcFile源文件，destFile目标文件
     *
     * @param srcFile
     * @param destFile
     * @throws IOException
     */
    public static boolean copyFileTo(File srcFile, File destFile) throws IOException {
        if (srcFile.isDirectory() || destFile.isDirectory()){
            return false;// 判断是否是文件
        }else {
            FileInputStream fis = new FileInputStream(srcFile);
            FileOutputStream fos = new FileOutputStream(destFile);
            int readLen = 0;
            byte[] buf = new byte[1024];
            while ((readLen = fis.read(buf)) >0) {
                fos.write(buf, 0, readLen);
            }
            fos.flush();
            fos.close();
            fis.close();
            return true;
        }

    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static boolean copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
                return true;
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 保存写入的文件到sd卡目录下
     *
     * @param fileName
     * @param txt
     */
    public static void saveFilePath(File parentFile, String fileName, String txt, boolean save) {
        try {
            File file = new File(parentFile, fileName);
            Log.e("result", "创建的文件的路径:" + file.getPath());
            FileOutputStream fos = new FileOutputStream(file, save);//false每次写入都会替换内容
            byte[] b = txt.getBytes();
            fos.write(b);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件的内容
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @return
     */
    public static String readFilePath(String filePath, String fileName) {
        try {
            File file = new File(filePath, fileName);
            if (!file.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len = bis.read(b);//表示每一次读取1024个字节放到byte数组里面
            while (len != -1) {
                len = bis.read(b);
                arrayOutputStream.write(b, 0, b.length);
            }
            arrayOutputStream.close();
            String content = new String(arrayOutputStream.toByteArray());
            Log.e("result", "读出来的内容:" + content);
            return content;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 文件的保存
     *
     * @param context  类
     * @param fileName 文件的名称
     * @param txt      内容
     */
    public static void saveFile(Context context, String fileName, String txt) {
        try {
            /*
             * 第一个参数，代表文件名称，注意这里的文件名称不能包括任何的/或者/这种分隔符，只能是文件名
             *          该文件会被保存在/data/data/应用名称/files/sss.txt
             * 第二个参数，代表文件的操作模式
             *          MODE_PRIVATE 私有（只能创建它的应用访问） 重复写入时会文件覆盖
             *          MODE_APPEND  私有   重复写入时会在文件的末尾进行追加，而不是覆盖掉原来的文件
             *          MODE_WORLD_READABLE 公用  可读
             *          MODE_WORLD_WRITEABLE 公用 可读写
             *  */
            FileOutputStream outputStream = context.openFileOutput(fileName, Activity.MODE_PRIVATE);
            outputStream.write(txt.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件的读取
     *
     * @param context  类
     * @param fileName 文件的名称
     * @return
     */
    public static String readFile(Context context, String fileName) {
        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            while (inputStream.read(bytes) != -1) {
                arrayOutputStream.write(bytes, 0, bytes.length);
            }
            inputStream.close();
            arrayOutputStream.close();
            String content = new String(arrayOutputStream.toByteArray());
            Log.e("result", "读出来的内容:" + content);
            return content;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "暂无内容";
    }


    /**
     * 遍历文件夹下所以的文件
     *
     * @param file 文件夹
     * @return 返回文件的路径
     */
    public static List<String> getListFiles(File file) {
        List<String> list = new ArrayList<String>();
        File[] fileArray = file.listFiles();
        if (fileArray==null){
            return null;
        }
        if (fileArray.length == 0) {
            return null;
        } else {
            // 遍历该目录
            for (File currentFile : fileArray) {
                if (currentFile.isFile()) {
                    // 文件则
                    list.add(currentFile.getPath());
                } else {
                    // 递归
                    getListFiles(currentFile);
                }
            }
        }
        return list;
    }

    /**
     * 遍历文件夹下所以的文件夹
     *
     * @param path 文件路径
     * @return 返回文件
     */
    public static List<File> getListFiles(String path) {
        List<File> list = new ArrayList<File>();
        File file = new File(path);
        File[] fileArray = file.listFiles();
        if (fileArray==null){
            return list;
        }
        if (fileArray.length == 0) {
            return list;
        } else {
            // 遍历该目录
            for (File currentFile : fileArray) {
                if (currentFile.isDirectory()) {
                    // 文件则
                    list.add(currentFile);
                } else {
                    // 递归
                    getListFiles(currentFile);
                }
            }
        }
        return list;
    }

}
