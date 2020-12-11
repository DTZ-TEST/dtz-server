package test;

import com.alibaba.fastjson.JSON;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.HttpsUtil;
import com.sy.mainland.util.SHAUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by pc on 2017/4/18.
 */
public class Test1 {
    public static void main(String[] args) {
        StringBuilder stringBuilder=new StringBuilder();

        String appId="1400029121";
        String appKey="d2fe9cb93128078f54237e73d51a3352";//sdkappid对应的appkey，需要业务方高度保密

        String msg="您的注册验证码是：{}，有效期5分钟，切勿告知他人。祝您游戏愉快！";
        msg=msg.replace("{}", "a5h68");

        String strMobile = "18507312202"; //tel的mobile字段的内容
        long strRand = System.currentTimeMillis(); //url中的random字段的值
        long strTime = strRand/1000; //unix时间戳

        stringBuilder.append("appkey=").append(appKey);
        stringBuilder.append("&random=").append(strRand);
        stringBuilder.append("&time=").append(strTime);
        stringBuilder.append("&mobile=").append(strMobile);

        String sig = SHAUtil.sha256(stringBuilder.toString());

        String url=new StringBuilder("https://yun.tim.qq.com/v5/tlssmssvr/sendsms?sdkappid=")
                .append(appId).append("&random=").append(strRand).toString();

        Map<String,Object> dataMap = new LinkedHashMap<>();
        Map<String,Object> data1Map = new LinkedHashMap<>();
        data1Map.put("nationcode","86");
        data1Map.put("mobile",strMobile);

        dataMap.put("tel",data1Map);
        dataMap.put("type",0);
        dataMap.put("msg", msg);
        dataMap.put("sig",sig);
        dataMap.put("time",strTime);
        dataMap.put("extend","");
        dataMap.put("ext","");

//        System.out.println(JSON.toJSONString(dataMap));

        Map<String,String> params=new HashMap<>();
        params.put("$",JSON.toJSONString(dataMap));
        String result= HttpsUtil.getUrlReturnValue(url,"UTF-8","POST",params);

        System.out.println(result);
    }
}
