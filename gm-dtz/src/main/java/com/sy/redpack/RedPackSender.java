package com.sy.redpack;

import com.sy.mainland.util.PropertiesFileLoader;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class RedPackSender {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RedPackSender.class);

    private static final org.slf4j.Logger monitor = LoggerFactory.getLogger("MONITOR");

    /**
     * 发送现金红包给玩家
     * @param openId
     * @param amont
     * @return
     */
    public static WechatRedPackResponse sendRedBag(String openId, float amont) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            WechatRedPackRequest request = new WechatRedPackRequest();
            request.setNonce_str(UUID.randomUUID().toString().replace("-", ""));
            request.setMch_id(WeixinRedbagConfig.getMchId());// 商户订单号（每个订单号必须唯一） 组成： mch_id+yyyymmdd+10位一天内不能重复的数字
            String mchBillno = WeixinRedbagConfig.getMchId() + sdf.format(new Date()) + "0" + System.currentTimeMillis() % 1000000000;
            request.setMch_billno(mchBillno);
            request.setWxappid(WeixinRedbagConfig.getAppId());
            request.setNick_name(WeixinRedbagConfig.getGameName());
            request.setSend_name(WeixinRedbagConfig.getGameName());
            request.setRe_openid(openId); // 接收者的微信公众号的openId;
            int money = (int)(amont * 100);
            logger.info("money:" + money);// 以分为单位
            request.setTotal_amount(money);
            request.setMin_value(money);
            request.setMax_value(money);
            request.setTotal_num(1);
            request.setWishing(WeixinRedbagConfig.getWishContent());
            request.setClient_ip("1.1.1.1");
            request.setAct_name(WeixinRedbagConfig.getActivityName());
            request.setRemark(WeixinRedbagConfig.getActivityName());
            String sign = WechatUtils.getSign(request);
            request.setSign(sign);
            FileInputStream instream = new FileInputStream(new File(PropertiesFileLoader.getClassPath() + WeixinRedbagConfig.getP12KeyPath()));
            String resp = HttpUtil1.wechatPost(WeixinRedbagConfig.getApiUrl(),
                    WechatUtils.convertObjectToXml(request, WechatRedPackRequest.class), instream);
            WechatRedPackResponse response = WechatUtils.convertXmlToResponse(resp);
            logger.info(resp);
            return response;
        } catch (Exception e) {
            logger.error("发送现金红包接口异常:" + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        float game= 1.6199999f;
        DecimalFormat fnum = new DecimalFormat("#0.00");
        float sendAccRedBagNum = Float.parseFloat(fnum.format(game));
        int ddd = (int)(sendAccRedBagNum * 100);

        FileInputStream instream = new FileInputStream(new File(PropertiesFileLoader.getClassPath() + WeixinRedbagConfig.getP12KeyPath()));
        System.out.println(ddd);
    }

}
