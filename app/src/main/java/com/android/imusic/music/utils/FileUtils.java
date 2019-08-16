package com.android.imusic.music.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/15
 */

public class FileUtils {

    private static volatile FileUtils mInstance;

    public static FileUtils getInstance() {
        if(null==mInstance){
            synchronized (FileUtils.class) {
                if (null == mInstance) {
                    mInstance = new FileUtils();
                }
            }
        }
        return mInstance;
    }


    /**
     * 根据格式化URL的文件名
     * @param filePath 带有后缀的绝对路径地址
     * @return 文件名
     */
    public String getFileName(String filePath) {
        if (filePath.isEmpty()) {
            return filePath;
        }
        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    /**
     * 格式化字节
     * @param size 源字节大小
     * @return 格式化后的大小字符串
     */
    public String formatSizeToString(long size) {
        //获取到的size为：1705230
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize;
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB";
        } else {
            resultSize = size + "B";
        }
        return resultSize;
    }

    /**
     * 将内容写入文件
     * @param filePath eg:/mnt/sdcard/demo.txt
     * @param content  内容
     * @param isAppend 是否追加
     */
    public static void writeFile(String filePath, String content, boolean isAppend) {
        try {
            FileOutputStream fout = new FileOutputStream(filePath, isAppend);
            byte[] bytes = content.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除文件
     * @param file 文件对象
     * @return 是否成功执行删除动作
     */
    public boolean deleteFile(File file) {
        if (file != null && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除指定文件，如果是文件夹，则递归删除
     * @param file 文件、文件夹
     * @return 是否成功执行删除动作
     */
    public static boolean deleteFileOrDirectory(File file) {
        if(null==file||!file.exists()) return false;
        try {
            if (file != null && file.isFile()) {
                return file.delete();
            }
            if (file != null && file.isDirectory()) {
                File[] childFiles = file.listFiles();
                // 删除空文件夹
                if (childFiles == null || childFiles.length == 0) {
                    return file.delete();
                }
                // 递归删除文件夹下的子文件
                for (int i = 0; i < childFiles.length; i++) {
                    deleteFileOrDirectory(childFiles[i]);
                }
                return file.delete();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取文件夹大小
     * @param dir 文件夹相对目录
     * @return 大小，字节
     */
    public static long getFolderSize(String dir) {
        File file = new File(dir);
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i].getAbsolutePath());
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /***
     * 获取文件扩展名
     * @param filename 文件名
     * @return .开头的后缀名
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }
}