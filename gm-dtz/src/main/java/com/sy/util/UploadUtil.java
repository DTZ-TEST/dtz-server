package com.sy.util;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

public class UploadUtil {

    // 产生32位UUID
    public static String getUUID32() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    // 获得服务器存储文件名,格式为： 32位UUID+"_"+ fileName
    public static String getSaveFileName(String fileName) {

        if (fileName != null && fileName.length() > 0){
        	
        	return getUUID32() + "_" + fileName;
        }
        return "";

    }

    // 获得上传保存路径  -----这里是模拟的
    public static File getSavePath(HttpServletRequest request) {

        String date = formatDate(new Timestamp(System.currentTimeMillis()),
                "yyyyMMdd");// 当前日期
        String moduls = "upload";// 模块名（这里假设模块名为：upload）
        String username = "A12346579";// 用户名(这里模拟用户名为：A12346579)

        String relativePath = "D:/pic";
       // String absolutePath =  "D:/pic";
        String absolutePath = PropUtil.getString("absolutePath", "", Constant.H5PAY_FILE);
       // String absolutePath = "/data/manorage/pic";//测试服
        File dir = new File(absolutePath);
        if (!dir.exists())// 如果文件家不存在，就创建
            dir.mkdirs();

        return dir;
    }

    // 格式化时间
    public static String formatDate(Timestamp timestamp, String format) {

        return new SimpleDateFormat(format).format(timestamp);
    }

    //这里为了简单，没有引入处理json的包，这是模拟json数据
    public static String getResponseResult(int status,String message){

        return  "{status: "+status+",message: '"+message+"'}";
    }

}

