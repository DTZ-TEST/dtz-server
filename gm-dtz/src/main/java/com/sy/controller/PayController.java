package com.sy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.entity.pojo.*;
import com.sy.general.GeneralHelper;
import com.sy.mainland.util.*;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.MD5Util;
import com.sy.mainland.util.cache.CacheEntity;
import com.sy.mainland.util.cache.CacheEntityUtil;
import com.sy.mainland.util.cache.TimeUnitSeconds;
import com.sy.mainland.util.db.annotation.SelectDataBase;
import com.sy.util.*;
import com.sy.util.weixin.PayUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pay/*"})
public class PayController extends BaseController {
    private static Object lock = new Object();
    private static final Logger logger = LoggerFactory.getLogger(PayController.class);

//    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/cash"})
//    public void cash(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        Map<String, String> params = UrlParamUtil.getParameters(request);
//        logger.info("params:{}", params);
//
//        int count = NumberUtils.toInt(params.get("count"), 0);
//
//        if (count <= 0) {
//            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("pay_count_error"))
//                    , request, response, null, false);
//            return;
//        }
//
//        RoomCard roomCard=loadRoomCard(request);
//
//        if (StringUtils.isBlank(roomCard.getOpenid())){
//            OutputUtil.output(1004,PropUtil.getString("wx_auth_fail"),request,response,false);
//            return;
//        }else{
////<xml>
////<mch_appid>wxe062425f740c30d8</mch_appid>
////<mchid>10000098</mchid>
////<nonce_str>3PG2J4ILTKCH16CQ2502SI8ZNMTM67VS</nonce_str>
////<partner_trade_no>100000982014120919616</partner_trade_no>
////<openid>ohO4Gt7wVPxIT1A9GjFaMYMiZY1s</openid>
////<check_name>FORCE_CHECK</check_name>
////<re_user_name>张三</re_user_name>
////<amount>100</amount>//分
////<desc>节日快乐!</desc>
////<spbill_create_ip>10.2.3.10</spbill_create_ip>
////<sign>C97BDBACF37622775366F38B629F45E3</sign>
////</xml>
//            String temp=CommonUtil.getSerializableDigit();
//            Map<String,String> map=new LinkedHashMap<>();
//            map.put("mch_appid",PropUtil.getString("appid"));
//            map.put("mchid",PropUtil.getString("mchid"));
//            map.put("nonce_str", MD5Util.getMD5String(UUID.randomUUID().toString()));
//            map.put("partner_trade_no",temp);
//            map.put("openid",roomCard.getOpenid());
//            map.put("check_name","FORCE_CHECK");
//            map.put("re_user_name",roomCard.getUserName());
//            map.put("amount",String.valueOf(count*100));
//            map.put("desc","牛魔王代理返佣");
//            map.put("spbill_create_ip",PropUtil.getString("localip"));
//
//            String[] keys=map.keySet().toArray(new String[0]);
//            Arrays.sort(keys);
//
//            StringBuilder strBuilder=new StringBuilder();
//            for (String key:keys){
//                String value=map.get(key);
//                if (StringUtils.isNotBlank(value)){
//                    strBuilder.append(key).append("=").append(value).append("&");
//                }
//            }
//
//            strBuilder.append("key=").append(PropUtil.getString("paykey"));
//
//            map.put("sign",MD5Util.getMD5String(strBuilder));
//
//            StringBuilder paramBuilder=new StringBuilder();
//            paramBuilder.append("<xml>");
//            for (Map.Entry<String,String> kv:map.entrySet()){
//                paramBuilder.append("<").append(kv.getKey()).append(">");
//                paramBuilder.append(kv.getValue());
//                paramBuilder.append("</").append(kv.getKey()).append(">");
//            }
//            paramBuilder.append("</xml>");
//
//            String postContent=paramBuilder.toString();
//            String result= PayUtil.post(PropUtil.getString("mchid"),PayUtil.PAY_URL,postContent);
//
//            logger.info("transfer: agencyId={},content={},result={}",roomCard.getAgencyId(),postContent,result);
//        }
//    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/player"})
    public void payForPlayer(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String playerId = params.get("playerId[]");

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        Integer isFree = Integer.parseInt(StringUtils.isBlank(params.get("isFree"))?"0":params.get("isFree"));
        int count = NumberUtils.toInt(params.get("count"), 0);

        if (count <= 0) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("pay_count_error"))
                    , request, response, null, false);
            return;
        }
        RoomCard roomCards =(RoomCard)request.getSession().getAttribute("roomCard");
        UserInfo userInfo;
        if (!CommonUtil.isPureNumber(playerId)) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("player_not_exists"))
                    , request, response, null, false);
            return;
        } else {
            userInfo = new UserInfo();
            userInfo.setUserId(Long.parseLong(playerId));
            userInfo = commonManager.findOne(dataBase,userInfo);

            if (userInfo == null) {
                OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("player_not_exists"))
                        , request, response, null, false);
                return;
            }
        }

        String rest = null;

        synchronized (lock) {
            RoomCard roomCard = new RoomCard();
            roomCard.setUserId(loadRoomCard(request).getUserId());
            roomCard = commonManager.findOne(roomCard);

            int ret1, ret2 = 0;

            //if (roomCard.getPartAdmin() == null || roomCard.getPartAdmin() == 0) {
                if (roomCard.getCommonCard() == null || roomCard.getCommonCard() < count) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("rest_lack"))
                            , request, response, null, false);
                    return;
                }
                roomCard.setCommonCard(roomCard.getCommonCard() - count);
                roomCard.setUpdateTime(new Date());

                setSessionValue(request, "roomCard", roomCard);

                ret1 = commonManager.saveOrUpdate( SqlHelperUtil.getString("cards_agency", Constant.ROOMCARD_FILE)+" and commonCard>=?",
                        new Object[]{-count, CommonUtil.dateTimeToString(), roomCard.getAgencyLevel(), roomCard.getUserId(),count});
            /*} else {
                ret1 = 1;
                rest = "all";
            }*/

            if (ret1 > 0)
                ret2 = commonManager.saveOrUpdate(dataBase, SqlHelperUtil.getString("cards_user", Constant.USER_INFO_FILE),
                        new Object[]{count, playerId});

            if (ret2 > 0) {
                RoomCardOrder rco = new RoomCardOrder();
                rco.setRoleId(userInfo.getUserId());
                rco = commonManager.findOne( dataBase,rco);

                RoomCardOrder roomCardOrder = new RoomCardOrder();
                roomCardOrder.setCommonCards(count);
                roomCardOrder.setCreateTime(new Date());
                roomCardOrder.setFreeCards(0);
                roomCardOrder.setIsDirectRecharge(1);
                roomCardOrder.setIsFirstPayBindId(0);
                if(isFree != null && isFree.intValue() == 1){
                	roomCardOrder.setRemark("活动赠送");
                }
                roomCardOrder.setIsFirstPayAmount(rco == null ? count : 0);
                roomCardOrder.setOrderId(CommonUtil.getSerializableDigit());
                roomCardOrder.setOrderStatus(1);
                roomCardOrder.setRechargeAgencyId(roomCard.getAgencyId());
                roomCardOrder.setRechargeBindAgencyId(userInfo.getPayBindId() == null ? 0 : userInfo.getPayBindId());
                roomCardOrder.setRechargeWay("");
                roomCardOrder.setRegisterBindAgencyId(userInfo.getRegBindId() == null ? 0 : userInfo.getRegBindId());
                roomCardOrder.setRoleId(userInfo.getUserId());

                commonManager.save(dataBase, roomCardOrder);

                if (userInfo.getEnterServer()!=null&&userInfo.getEnterServer().intValue()>0){
                    ServerConfig serverConfig=new ServerConfig();
                    serverConfig.setId(userInfo.getEnterServer());
                    serverConfig=commonManager.findOne(dataBase,serverConfig);

                    if (serverConfig!=null){
                        String url=serverConfig.getIntranet();
                        if (StringUtils.isBlank(url)){
                            url=serverConfig.getHost();
                        }

                        if (StringUtils.isNotBlank(url)){
                            int idx=url.indexOf(".");
                            if (idx>0){
                                idx=url.indexOf("/",idx);
                                if (idx>0){
                                    url=url.substring(0,idx);
                                }
                                long now=System.currentTimeMillis();
                                url+="/online/notice.do?type=playerCards&userId="+userInfo.getUserId()+"&message="+count;
                                String noticeRet = HttpUtil.getUrlReturnValue(url,5);
                                logger.info("notice result:url={},ret={},time={}",url,noticeRet,System.currentTimeMillis()-now);
                            }
                        }
                    }

                }
            }

            logger.info("user pay result:admin={},agencyId={},agencyRest={},playerId={},playerAddCards={},ret1={},ret2={}"
                    , roomCard.getPartAdmin(), roomCard.getAgencyId(), roomCard.getCommonCard(), playerId, count, ret1, ret2);
            if (!"all".equals(rest))
                rest = roomCard.getCommonCard() == null ? "" : roomCard.getCommonCard().toString();
        }

        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, LanguageUtil.getString("pay_success"))
                        .builder("rest", rest)
                , request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency"})
    public void payForAgency(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        String agencyId = params.get("agencyId");
        int count = NumberUtils.toInt(params.get("count"), 0);
        Integer isFree = Integer.parseInt(StringUtils.isBlank(params.get("isFree"))?"0":params.get("isFree"));
        if (count <= 0) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("pay_count_error"))
                    , request, response, null, false);
            return;
        }

        RoomCard rc=loadRoomCard(request);

        if (count <= 0&&(rc.getAgencyLevel()==null||rc.getAgencyLevel().intValue()<99)) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("pay_for_agency_min"))
                    , request, response, null, false);
            return;
        }

        RoomCard roomCard0;
        if (!CommonUtil.isPureNumber(agencyId)) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("agency_not_exists"))
                    , request, response, null, false);
            return;
        } else {

            if (agencyId.equals(loadRoomCard(request).getAgencyId().toString())) {
                OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("agency_me_error"))
                        , request, response, null, false);
                return;
            }

            roomCard0 = new RoomCard();
            roomCard0.setAgencyId(Integer.parseInt(agencyId));
            roomCard0 = commonManager.findOne(roomCard0);

            if (roomCard0 == null) {
                OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("agency_not_exists"))
                        , request, response, null, false);
                return;
            }
        }
        String rest = null;
        synchronized (lock) {
            RoomCard roomCard = new RoomCard();
            roomCard.setUserId(loadRoomCard(request).getUserId());
            roomCard = commonManager.findOne( roomCard);

            int ret1, ret2 = 0;

            //if (roomCard.getPartAdmin() == null || roomCard.getPartAdmin() == 0) {
                if (roomCard.getCommonCard() == null || roomCard.getCommonCard() < count) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("rest_lack"))
                            , request, response, null, false);
                    return;
                }
                roomCard.setCommonCard(roomCard.getCommonCard() - count);
                roomCard.setUpdateTime(new Date());

                setSessionValue(request, "roomCard", roomCard);

                ret1 = commonManager.saveOrUpdate(SqlHelperUtil.getString("cards_agency", Constant.ROOMCARD_FILE)+" and commonCard>=?",
                        new Object[]{-count, CommonUtil.dateTimeToString(), roomCard.getAgencyLevel(), roomCard.getUserId(),count});
           /* } else {
                ret1 = 1;
                rest = "all";
            }*/

            if (ret1 > 0)
                ret2 = commonManager.saveOrUpdate( SqlHelperUtil.getString("cards_agency", Constant.ROOMCARD_FILE),
                        new Object[]{count, CommonUtil.dateTimeToString(), roomCard0.getAgencyLevel(), roomCard0.getUserId()});

            if (ret2 > 0) {

                CacheEntityUtil.setCache("refresh agency"+agencyId,new CacheEntity<>("1", TimeUnitSeconds.CACHE_1_DAY));

                RoomCardRecord roomCardRecord = new RoomCardRecord();
                roomCardRecord.setActiveUserid(roomCard.getAgencyId());
                roomCardRecord.setActiveUserName(roomCard.getUserName());
                roomCardRecord.setCreateTime(new Date());
                roomCardRecord.setReactiveUserId(roomCard0.getAgencyId());
                roomCardRecord.setReactiveUserName(roomCard0.getUserName());
                roomCardRecord.setRechargeOrReturn(1);
                roomCardRecord.setRecordStatus(0);
                roomCardRecord.setRoomCardType(1);
                roomCardRecord.setRoomCardNumber(count);
                if(isFree != null && isFree.intValue() == 1){
                	roomCardRecord.setRemark("活动赠送");
                }
                commonManager.save(roomCardRecord);
            }

            logger.info("agency pay result:admin={},agencyId={},agencyRest={},playerId={},playerAddCards={},ret1={},ret2={}"
                    , roomCard.getPartAdmin(), roomCard.getAgencyId(), roomCard.getCommonCard(), agencyId, count, ret1, ret2);
            if (!"all".equals(rest))
                rest = roomCard.getCommonCard() == null ? "" : roomCard.getCommonCard().toString();
        }

        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, LanguageUtil.getString("pay_success"))
                        .builder("rest", rest)
                , request, response, null, false);
    }

    //h5支付后返回
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/callback"})
    public String callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String page="buy_cards";
        try {
            Map<String,String> params = UrlParamUtil.getParameters(request);

            logger.info("callback params={}",params);

            String out_trade_no = request.getParameter("out_trade_no");
            if (!StringUtils.isBlank(out_trade_no)) {
                OrderInfo order = new OrderInfo();
                order.setOrderId(out_trade_no);
                order = commonManager.findOne( order);
//                request.setAttribute("out_trade_no", out_trade_no);
//                request.setAttribute("total_fee", order.getOrderAmount());
//                request.setAttribute("body", PropUtil.getString("body", "", Constant.H5PAY_FILE));
                if (order.getIsSent() != null && order.getIsSent().intValue() > 0) {
                    loadRoomCard(request);
                    page="index";
                }
            }
            setSessionValue(request,"order_confirm", "confirm"+out_trade_no);
        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
        }
        return page;
    }

    //订单支付状态查询接口
    public HashMap<String, String> queryPayResult(String out_trade_no) throws Exception {
        HashMap<String, String> payResult = new HashMap<String, String>();
        payResult.put("trade_state", "PAYERROR");
        SortedMap<String, String> map = new TreeMap<>();
        CloseableHttpResponse response1 = null;
        CloseableHttpClient client = null;
        String ret = null;
        String res = null;
        String reqUrl = "";
        String reqParamsStr = "";
        try {
            if (StringUtils.isBlank(out_trade_no)) {
                logger.error("订单号为空！");
                return payResult;
            }
            payResult.put("out_trade_no", out_trade_no);
            map.put("out_trade_no", out_trade_no);
            map.put("mch_id", PropUtil.getString("mch_id", "", Constant.H5PAY_FILE));
            map.put("nonce_str", String.valueOf(new Date().getTime()));
            map.put("service", PropUtil.getString("query_service", "", Constant.H5PAY_FILE));
            map.put("version", PropUtil.getString("version", "", Constant.H5PAY_FILE));
            map.put("sign_type", PropUtil.getString("sign_type", "", Constant.H5PAY_FILE));
            Map<String, String> params = SignUtils.paraFilter(map);
            StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
            SignUtils.buildPayParams(buf, params, false);
            String preStr = buf.toString();
            String sign = MD5.sign(preStr, "&key=" + PropUtil.getString("key", "", Constant.H5PAY_FILE), "utf-8");
            map.put("sign", sign);
            reqUrl = PropUtil.getString("req_url", "", Constant.H5PAY_FILE);
            reqParamsStr = XmlUtils.parseXML(map);
            HttpPost httpPost = new HttpPost(reqUrl);
            StringEntity entityParams = new StringEntity(reqParamsStr, "utf-8");
            httpPost.setEntity(entityParams);
            //httpPost.setHeader("Content-Type", "text/xml;charset=ISO-8859-1");
            client = HttpClients.createDefault();
            response1 = client.execute(httpPost);
            if (response1 != null && response1.getEntity() != null) {
                Map<String, String> resultMap = XmlUtils.toMap(EntityUtils.toByteArray(response1.getEntity()), "utf-8");
                res = XmlUtils.toXml(resultMap);
                ret = res;
                logger.info("请求结果：" + res);
                if (resultMap.containsKey("sign")) {
                    if (!SignUtils.checkParam(resultMap, PropUtil.getString("key", "", Constant.H5PAY_FILE))) {
                        res = "验证签名不通过";
                    } else {
                        if ("0".equals(resultMap.get("status")) && "0".equals(resultMap.get("result_code"))) {
                            String trade_state = resultMap.get("trade_state");
                            payResult.put("trade_state", trade_state);
                            payResult.put("msg", LanguageUtil.getString(trade_state, "支付失败"));
                            if (!StringUtils.isBlank(trade_state) && "SUCCESS".equals(trade_state)) {
                                payResult.put("total_fee", resultMap.get("total_fee"));
                                payResult.put("payTime", resultMap.get("time_end"));
                            }

                        } else {
                            payResult.put("msg", resultMap.get("err_msg"));
                        }
                    }
                }
            } else {
                res = "操作失败";
            }
        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
            res = "系统异常";
        } finally {
            logger.info("request url={},param={}", reqUrl, reqParamsStr, ret);
            if (response1 != null) {
                response1.close();
            }
            if (client != null) {
                client.close();
            }
        }
        return payResult;
    }

    //调起代理自助购钻H5微信支付
   /* @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pay", "/create"})
    public void pay(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        Map<String, String> reqParams = UrlParamUtil.getParameters(request);

        logger.info("params:{}", reqParams);

        final int orderAmount = NumberUtils.toInt(reqParams.get("total_fee"), 0);

        String itemsStr = PropUtil.getString("goods_items", "", Constant.H5PAY_FILE);
        String[] items = itemsStr.split(";");
        String[] currentItem = null;
        for (String item : items) {
            if (item.length() > 0) {
                String[] temps = item.split(",");
                if (Integer.parseInt(temps[0]) == orderAmount) {
                    currentItem = temps;
                    break;
                }
            }
        }

        if (currentItem == null) {
//            request.setAttribute("result", "品项不存在");
            OutputUtil.output(1001, "品项不存在", request, response, false);
            return;//"h5Pay";
        }

        RoomCard roomCard = loadRoomCard(request);
        RoomCard roomCard1=null;
        if (reqParams.containsKey("token")){
            CacheEntity<RoomCard> cacheEntity = CacheEntityUtil.getCache(reqParams.get("token"));
            if (cacheEntity != null){
                roomCard1 = cacheEntity.getValue();
            }

            if (roomCard1 == null){
                OutputUtil.output(1001,LanguageUtil.getString("param_error"),request,response,false);
                return;
            }
        }

        logger.info("good item:userId={},agencyId={},item={},delegate={}", roomCard.getUserId(), roomCard.getAgencyId(), currentItem,roomCard1==null?"":roomCard1.getAgencyId());
        String out_trade_no = CommonUtil.getSerializableDigit();
        JSONObject item = new JSONObject();
		item.put("partner_id", PropUtil.getString("partner_id", "", Constant.H5PAY_FILE));
		item.put("app_id", PropUtil.getString("app_id", "", Constant.H5PAY_FILE));
		item.put("wap_type", "1");
		item.put("money", String.valueOf(orderAmount));
		item.put("out_trade_no", out_trade_no);
		item.put("subject", URLEncoder.encode("代理购钻"));
		item.put("imei", roomCard.getUserId());
		item.put("qn", PropUtil.getString("qn", "", Constant.H5PAY_FILE));
		String signString = Util.getUrlData(item);
		item.put("sign", com.sy.util.MD5Util.md5Encode(signString+"&key="+PropUtil.getString("zyf_key", "", Constant.H5PAY_FILE)).toUpperCase());
		String kystr = Util.getUrlData(item);
		String url = PropUtil.getString("zyf_payUrl", "", Constant.H5PAY_FILE)+kystr;
        String res = null;
        try {
        	URL urls =new URL(url);
            HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            Integer code = conn.getResponseCode();
            System.out.println("支付返回的代码值："+code);
    		if(code != null && code==302){
    			Map<String, List<String>> map = conn.getHeaderFields();
    			String str = map.get("Location").get(0).replace("[", "").replace("]", "");
    			System.out.println("支付返回的Location值:"+str);
    			String result = str;
    			"https://pay.swiftpass.cn/pay/qrcode?uuid="+URLEncoder.encode(//);
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setCreateTime(new Date());
                orderInfo.setOrderAmount(String.valueOf(orderAmount));
                orderInfo.setOrderId(out_trade_no);
                orderInfo.setPlatform(reqParams.get("type"));
                orderInfo.setIsSent(0);
                orderInfo.setPayType("微信支付");
                orderInfo.setPayPf("webzyf");
                orderInfo.setSellTime(new Date());
                orderInfo.setFlatId("");
                String pf = "weixindtz";
                orderInfo.setPlatform(pf);
                orderInfo.setItemId(orderAmount);

                orderInfo.setPayMoney(new BigDecimal(orderAmount*1.0/100).setScale(2, BigDecimal.ROUND_HALF_UP));

                if (roomCard1==null){
                    orderInfo.setUserId(roomCard.getUserId().longValue());
                    orderInfo.setServerId(roomCard.getAgencyId()+"Z");

                }else{
                    orderInfo.setUserId(roomCard1.getUserId().longValue());
                    orderInfo.setServerId(roomCard1.getAgencyId()+"Z");
                    orderInfo.setDelegateAgency(roomCard.getAgencyId()+"Z");
                }

                orderInfo.setItemNum(Integer.parseInt(currentItem[1]));

                orderInfo.setFlatId(request.getSession().getId());

                commonManager.save( orderInfo);

                OutputUtil.output(MessageBuilder.newInstance()
                                .builderCodeMessage(1000, "OK").builder("body","代理购钻").builder("pay_info",result)
                                .builder("out_trade_no", orderInfo.getOrderId()).builder("total_fee", orderInfo.getOrderAmount())
                        , request, response, null, false);

                setSessionValue(request,"order_confirm", "confirm"+out_trade_no);
                return;//"h5Pay";
    		}else {
                res = "操作失败";
            }
        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
            res = "系统异常";
        } finally {
            logger.info("request url={},param={}", url, item, res);
        }
        OutputUtil.output(1002, res, request, response, false);
        return;
    }*/
    
//    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pay", "/create"})
//    public void pay2(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    	request.setCharacterEncoding("utf-8");
//        response.setCharacterEncoding("utf-8");
//        Map<String, String> reqParams = UrlParamUtil.getParameters(request);
//        String type = reqParams.get("type");
//        logger.info("params:{}", reqParams);
//
//        final Integer orderAmount = NumberUtils.toInt(reqParams.get("total_fee"), 0);
//
//        String itemsStr = PropUtil.getString("goods_items", "", Constant.H5PAY_FILE);
//        String[] items = itemsStr.split(";");
//        String[] currentItem = null;
//        for (String item : items) {
//            if (item.length() > 0) {
//                String[] temps = item.split(",");
//                if (Integer.parseInt(temps[0]) == orderAmount) {
//                    currentItem = temps;
//                    break;
//                }
//            }
//        }
//
//        if (currentItem == null) {
////            request.setAttribute("result", "品项不存在");
//            OutputUtil.output(1001, "品项不存在", request, response, false);
//            return;//"h5Pay";
//        }
//
//        RoomCard roomCard = loadRoomCard(request);
//        RoomCard roomCard1=null;
//        if (reqParams.containsKey("token")){
//            CacheEntity<RoomCard> cacheEntity = CacheEntityUtil.getCache(reqParams.get("token"));
//            if (cacheEntity != null){
//                roomCard1 = cacheEntity.getValue();
//            }
//
//            if (roomCard1 == null){
//                OutputUtil.output(1001,LanguageUtil.getString("param_error"),request,response,false);
//                return;
//            }
//        }
//        //String url = "https://pay.swiftpass.cn/pay/gateway";
//        logger.info("good item:userId={},agencyId={},item={},delegate={}", roomCard.getUserId(), roomCard.getAgencyId(), currentItem,roomCard1==null?"":roomCard1.getAgencyId());
//        String out_trade_no = CommonUtil.getSerializableDigit();
//        Map<String, Object> payMap = new HashMap<>();
//		payMap.put("service", "pay.weixin.wappay");
//		String merIds = PropUtil.getString("merIds", "", Constant.H5PAY_FILE);
//		if (StringUtils.isBlank(merIds)){
//            merIds = PropUtil.getString("merId", "", Constant.H5PAY_FILE);
//        }
//        String[] mIds = merIds.split(",");
//		payMap.put("mch_id", mIds[new SecureRandom().nextInt(mIds.length)]);
//		payMap.put("out_trade_no", out_trade_no);
//		payMap.put("body", "代理购钻");
//		payMap.put("total_fee", orderAmount.toString());
//		payMap.put("mch_create_ip", request.getRemoteAddr());
//		payMap.put("notify_url", PropUtil.getString("notifyurl", "", Constant.H5PAY_FILE));
//		payMap.put("nonce_str", out_trade_no);
//		payMap.put("attach","附加信息");
//		payMap.put("device_info","AND_WAP");
//		payMap.put("mch_app_name","dtz");
//		payMap.put("mch_app_id","com.tencent.tmgp.sgame");
//		payMap.put("version","2.0");
//		payMap.put("sign_type","MD5");
//		payMap.put("sign", sign(payMap));
//		URL url = new URL("https://pay.swiftpass.cn/pay/gateway");
//        String res = null;
//          try {
//        	String xmlStr = XmlUtil.fromMap(payMap, "xml");
//        	logger.info("支付XML格式数据："+xmlStr);
//        	HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//        	conn.setDoOutput(true);
//        	conn.setRequestMethod("POST");
//        	byte[] xmlbyte = xmlStr.toString().getBytes("UTF-8");
//        	DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
//        	outStream.write(xmlbyte);
//        	outStream.flush();
//        	BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
//        	StringBuffer sb2 = new StringBuffer();
//        	String lines = "";
//        	while(null !=(lines=in.readLine())){
//        		sb2.append(lines);
//        	}
//          	String result =  sb2.toString();;
//			logger.info("create order url:"+url+",result="+result);
//			Map<String, Object> resMap = XmlUtil.toMap(result);
//			String status =resMap.get("status").toString();
//			if (status.equals("0")) {
//				String result_code = resMap.get("result_code").toString();
//				if (result_code.equals("0")) {
//					OrderInfo orderInfo = new OrderInfo();
//	                orderInfo.setCreateTime(new Date());
//	                orderInfo.setOrderAmount(String.valueOf(orderAmount));
//	                orderInfo.setOrderId(out_trade_no);
//	                orderInfo.setPlatform(reqParams.get("type"));
//	                orderInfo.setIsSent(0);
//	                String pf = "weixinwftdtz";
//	                orderInfo.setPlatform(pf);
//	                orderInfo.setSellTime(new Date());
//	                orderInfo.setFlatId("");
//	                orderInfo.setItemId(orderAmount);
//
//	                orderInfo.setPayMoney(new BigDecimal(orderAmount*1.0/100).setScale(2, BigDecimal.ROUND_HALF_UP));
//
//	                if (roomCard1==null){
//	                    orderInfo.setUserId(roomCard.getUserId().longValue());
//	                    orderInfo.setServerId(roomCard.getAgencyId()+"Z");
//
//	                }else{
//	                    orderInfo.setUserId(roomCard1.getUserId().longValue());
//	                    orderInfo.setServerId(roomCard1.getAgencyId()+"Z");
//	                    orderInfo.setDelegateAgency(roomCard.getAgencyId()+"Z");
//	                }
//
//	                orderInfo.setItemNum(Integer.parseInt(currentItem[1]));
//
//	                orderInfo.setFlatId(request.getSession().getId());
//	                String pay_info = resMap.get("pay_info").toString();
//	                commonManager.save( orderInfo);
//	                OutputUtil.output(MessageBuilder.newInstance()
//	                      .builderCodeMessage(1000, "OK").builder("body","代理购钻").builder("pay_info",pay_info).builder("type","微信支付")
//	                      .builder("out_trade_no", orderInfo.getOrderId()).builder("total_fee", orderInfo.getOrderAmount())
//	              , request, response, null, false);
//	                return;
//				}
//			}
//
//        } catch (Exception e) {
//            logger.error("Exception:" + e.getMessage(), e);
//            res = "系统异常";
//        } finally {
//            logger.info("request url={},param={}", url, payMap, res);
//        }
//        OutputUtil.output(1002, res, request, response, false);
//        return;
//    }
    private String sign(Map<String, Object> map) {
		Object[] oArr = map.keySet().toArray();
		Arrays.sort(oArr);
		StringBuffer sb = new StringBuffer();
		for (Object o : oArr) {
			String key = o.toString();
			if (key.equals("sign")) {
				continue;
			}
			Object value = map.get(key);
			if (value == null || StringUtils.isBlank(value.toString())) {
				continue;
			}
			sb.append(key).append("=").append(value).append("&");
		}
		String key = PropUtil.getString("mch_key"+map.get("mch_id"), "", Constant.H5PAY_FILE);

		sb.append("key=").append(StringUtils.isNotBlank(key)?key:PropUtil.getString("APPKEY", "", Constant.H5PAY_FILE));
		return com.sy.util.MD5Util.md5Encode(sb.toString()).toUpperCase();
	}
    //查询代理支付信息
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/query"})
    public void query(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        RoomCard rc = loadRoomCard(req);
        SystemUser user=loadSystemUser(req);
        if (rc==null||user==null) {
            OutputUtil.output(1004, LanguageUtil.getString("no_auth"), req, resp, false);
            return;
        }else if ((rc.getAgencyLevel()!=null&&rc.getAgencyLevel().intValue()==99)||(user.getRoleId()!=null&&user.getRoleId().intValue()>=1)){
        }else{
            OutputUtil.output(1004, LanguageUtil.getString("no_auth"), req, resp, false);
            return;
        }

        Map<String, String> params = UrlParamUtil.getParameters(req);
        logger.info("params:{}", params);

        Integer agencyId = NumberUtils.toInt(params.get("agencyId"), 0);
        if (agencyId <= 0) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("agency_not_exists"))
                    , req, resp, null, false);
            return;
        }

        List<Map<String, Object>> list = commonManager.find(SqlHelperUtil.getString("agency_pay_list", Constant.ORDER_INFO_FILE)
                , new Object[]{agencyId+"Z"});

        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                        .builder("datas", list == null ? new ArrayList<>() : list)
                , req, resp, null, false);
    }
    //查询支付结果
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/payResultQuery"})
    public void payResultQuery(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String respString = "0";
        try {
            String resString = req.getParameter("out_trade_no");
            if (resString != null && !"".equals(resString)) {
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setOrderId(resString);
                orderInfo = commonManager.findOne( orderInfo);
                if (orderInfo.getIsSent() != null && 1 == orderInfo.getIsSent().intValue()) {
                    respString = "1";
                }
            }

        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
        }
        resp.getWriter().write(respString);
        resp.getWriter().flush();
    }

    //处理代理自助购钻H5微信支付结果通知
//    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/notify"})
//    public void payNotify(HttpServletRequest req, HttpServletResponse resp) throws Exception {
//    	String respString = "fail";
//    	try {
//            req.setCharacterEncoding("utf-8");
//            resp.setCharacterEncoding("utf-8");
//            resp.setHeader("Content-type", "text/html;charset=UTF-8");
//            String resString = XmlUtils.parseRequst(req);
//            logger.info("通知内容：" + resString);
//            if (resString != null && !"".equals(resString)) {
//                Map<String, String> map = XmlUtils.toMap(resString.getBytes(), "utf-8");
//                String res = XmlUtils.toXml(map);
//                logger.info("通知内容：" + res);
//                if (map.get("status").equals("0")) {
//                    if (map.get("result_code").equals("0")){
//                    	String out_trade_no = map.get("out_trade_no");
//                         logger.info("订单号:：" + out_trade_no);
//                         //此处可以在添加相关处理业务，校验通知参数中的商户订单号out_trade_no和金额total_fee是否和商户业务系统的单号和金额是否一致，一致后方可更新数据库表中的记录。
//                         OrderInfo orderInfo = new OrderInfo();
//                         orderInfo.setOrderId(out_trade_no);
//                         orderInfo = commonManager.findOne(orderInfo);
//                        if ((orderInfo.getIsSent() == null || orderInfo.getIsSent().intValue() <= 0) && orderInfo.getOrderAmount().equals(map.get("total_fee"))) {
//                          respString = "0";
//                         int ret = commonManager.saveOrUpdate( SqlHelperUtil.getString("update_pay_result", Constant.ORDER_INFO_FILE)
//                                             , new Object[]{out_trade_no});
//                             if (ret > 0) {
//
//                             	ret = commonManager.saveOrUpdate( SqlHelperUtil.getString("add_cards_agency", Constant.ROOMCARD_FILE)
//                                         , new Object[]{orderInfo.getItemNum().intValue(), orderInfo.getUserId()});
//                                 logger.info("pay success:result={},order={}", ret, JSON.toJSONString(orderInfo));
//
//                                 commonManager.saveOrUpdate( SqlHelperUtil.getString("update_pay_sell_time", Constant.ORDER_INFO_FILE)
//                                         , new Object[]{CommonUtil.dateTimeToString(),out_trade_no});
//
//                                 RoomCard roomCard=new RoomCard();
//                                 roomCard.setUserId(orderInfo.getUserId().intValue());
//                                 roomCard=commonManager.findOne(roomCard);
//                                 if (roomCard!=null&&(roomCard.getAgencyLevel() == null || roomCard.getAgencyLevel().intValue() == 0)) {
//                                     int agencyCost = NumberUtils.toInt(PropUtil.getString("agency_cost"), 500);
//
//                                     if (orderInfo.getPayMoney().doubleValue() >= agencyCost || (orderInfo.getPayMoney().doubleValue() + commonManager.count(SqlHelperUtil.getString("agency_cards", Constant.ROOMCARD_ORDER_FILE)
//                                             , new Object[]{roomCard.getAgencyId()+"Z"})) >= agencyCost) {
//                                         ret=commonManager.saveOrUpdate(SqlHelperUtil.getString("update_agency_level", Constant.ROOMCARD_FILE),new Object[]{CommonUtil.dateTimeToString(),"1",roomCard.getUserId()});
//
//                                         logger.info("update agency level:from {} to {},result={}",roomCard.getAgencyLevel(),1,ret);
//                                     }
//                                 }
//
//                                 CacheEntityUtil.setCache("refresh"+orderInfo.getFlatId(),new CacheEntity<>(System.currentTimeMillis(),10*60));
//                             }
//                             }
//                          }
//                         }
//                  }
//
//         } catch (Exception e) {
//             logger.error("Exception:" + e.getMessage(), e);
//         } finally {
//             resp.getWriter().write(respString);
//             resp.getWriter().flush();
//         }
//    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/cash/income"})
    public void cash2(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            RoomCard rc = loadRoomCard(req);
//            SystemUser user=loadSystemUser(req);

            Map<String,String> params=UrlParamUtil.getParameters(req);
            logger.info("params:{}",params);

            String dateMsg=params.get("dateMsg");
            String type=params.get("type");
            SimpleDateFormat sdf=new SimpleDateFormat("M.d");
            if (StringUtils.isBlank(dateMsg)||StringUtils.isBlank(rc.getOpenid())){
                OutputUtil.output(1001,LanguageUtil.getString("param_error"),req,resp,false);
            }else{
                	 String date1=dateMsg;
                     String date2=dateMsg;
                     Date myDate1;
                     Date myDate2;
                     String desc;
                     if(type.equals("1")){
                     	myDate1=GeneralHelper.str2Date(dateMsg.substring(0, 6));
                     	Calendar cal = Calendar.getInstance();
                     	cal.setTime(myDate1);
                     	SimpleDateFormat format = new SimpleDateFormat("yyyyMM01~yyyyMMdd");
                         cal.add(Calendar.MONTH, 1);
                         cal.set(Calendar.DAY_OF_MONTH,0);//设置为1号,当前日期既为本月第一天
                         String lastDay = format.format(cal.getTime());
                         date1=lastDay.split("~")[0];
                         date2=lastDay.split("~")[1];
                         SimpleDateFormat s = new SimpleDateFormat("yyyyMM");

                         desc = "游戏3代理返佣(月末补足)"+s.format(myDate1);
                     }else if(type.equals("2")){
                    	 myDate1=GeneralHelper.str2Date(dateMsg);
                         if (!CashIncomeUtil.canCashIncome(myDate1)) {
                             OutputUtil.output(1008, CashIncomeUtil.msg_cashIncome, req, resp, false);
                             return;
                         }
                    	 desc = "游戏3代理返佣"+sdf.format(myDate1);
                     }else{
                         type = "default";
                    	 String[] dates=dateMsg.split("\\~");
                    	 date1=dates[0].replace("-", "");
                         date2=dates[1].replace("-", "");
                         myDate1=GeneralHelper.str2Date(date1);
                         myDate2=GeneralHelper.str2Date(date2);

                         desc = "游戏3代理返佣"+sdf.format(myDate1)+"~"+sdf.format(myDate2);
                     }

                SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_1");

                AgencyIncome agencyIncome=commonManager.findOne(dataBase,"select * from agency_income where startDate=? and endDate=? and agencyId=?",new Object[]{date1,date2,rc.getAgencyId()},AgencyIncome.class);

                        BigDecimal bigDecimal;

                        if (agencyIncome==null){
                            OutputUtil.output(1001,LanguageUtil.getString("param_error"),req,resp,false);
                            return;
                        }else{
                            bigDecimal=agencyIncome.getTotalIncome();
                            bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
                            logger.info("agency withdraw cash:agencyId={},date={},income={},currentState={}",rc.getAgencyId(),dateMsg,bigDecimal,agencyIncome.getCurrentState());
                        }
                        if (!"0".equals(agencyIncome.getCurrentState())){
                            OutputUtil.output(1002,LanguageUtil.getString("wx_cash_withdraw"),req,resp,false);
                        }else if (agencyIncome.getTotalIncome()==null||agencyIncome.getTotalIncome().intValue()<1){
                            OutputUtil.output(1004,LanguageUtil.getString("wx_cash_min"),req,resp,false);
                        }else{

                            int max = NumberUtils.toInt("cash_max",0);
                            if (max>0&&bigDecimal.intValue()>max){
                                OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1100,LanguageUtil.getString("cash_max_out_tip","单笔金额超过限额，请联系客服")),req,resp,null,false);
                                return;
                            }
                                    synchronized (PayController.class){
                                    int ret=commonManager.saveOrUpdate(dataBase,"update agency_income set currentState=? where keyId=?",new Object[]{"1",agencyIncome.getKeyId()});

                                    String temp0 = new StringBuilder().append(rc.getUserId()).append("|").append(agencyIncome.getAgencyId()).append("|").append(agencyIncome.getKeyId()).append("|").append(agencyIncome.getIncomeStatiscType()).append("|"
                                    ).append(agencyIncome.getStartDate()).append("|").append(agencyIncome.getEndDate()).append("|").append(rc.getUserName()).append("|").append(CommonUtil.dateTimeToString(rc.getCreateTime())).toString();
                                    String temp = MD5Util.getMD5String(temp0);

                                    logger.info("agency={},cash={},result={},keyId={},preState={},tradeNo1={},tradeNo2={}",rc.getAgencyId(),bigDecimal,ret
                                    ,agencyIncome.getKeyId(),agencyIncome.getCurrentState(),temp0,temp);
                                    if (ret>0){
                                        Map<String,String> map=new LinkedHashMap<>();
                                        map.put("mch_appid",PropUtil.getString("appid"));
                                        map.put("mchid",PropUtil.getString("mchid"));
                                        map.put("nonce_str", com.sy.mainland.util.MD5Util.getMD5String(UUID.randomUUID().toString()));
                                        map.put("partner_trade_no",temp);
                                        map.put("openid",rc.getOpenid());
                                        map.put("check_name","FORCE_CHECK");
                                        map.put("re_user_name",rc.getUserName());
                                        map.put("amount",String.valueOf((int)(bigDecimal.doubleValue()*100)));
                                        map.put("desc",desc);
                                        map.put("spbill_create_ip",PropUtil.getString("localip"));

                                        String[] keys=map.keySet().toArray(new String[0]);
                                        Arrays.sort(keys);

                                        StringBuilder strBuilder=new StringBuilder();
                                        for (String key:keys){
                                            String value=map.get(key);
                                            if (StringUtils.isNotBlank(value)){
                                                strBuilder.append(key).append("=").append(value).append("&");
                                            }
                                        }

                                        strBuilder.append("key=").append(PropUtil.getString("paykey"));

                                        map.put("sign", com.sy.mainland.util.MD5Util.getMD5String(strBuilder));

                                        StringBuilder paramBuilder=new StringBuilder();
                                        paramBuilder.append("<xml>");
                                        for (Map.Entry<String,String> kv:map.entrySet()){
                                            paramBuilder.append("<").append(kv.getKey()).append(">");
                                            paramBuilder.append(kv.getValue());
                                            paramBuilder.append("</").append(kv.getKey()).append(">");
                                        }
                                        paramBuilder.append("</xml>");

                                        String postContent=paramBuilder.toString();
                                        try{
                                            String result= PayUtil.post(PropUtil.getString("mchid"),PayUtil.PAY_URL,postContent);

                                            logger.info("transfer: agencyId={},content={},result={}",rc.getAgencyId(),postContent,result);

                                            Map<String, String> retMap = XmlUtils.toMap(result.getBytes("UTF-8"), "UTF-8");
                                            if ("SUCCESS".equals(retMap.get("return_code")) && "SUCCESS".equals(retMap.get("result_code"))) {
                                                OutputUtil.output(1000,LanguageUtil.getString("operate_success"),req,resp,false);

                                                HbExchangeRecord hb=new HbExchangeRecord();
                                                hb.setUserId(rc.getAgencyId().longValue());
                                                hb.setMoney(agencyIncome.getTotalIncome());
                                                hb.setWxname(rc.getUserName());
                                                hb.setPhone(rc.getAgencyPhone());
                                                hb.setCreateTime(new Date());
                                                hb.setState(11);
                                                commonManager.save(dataBase,hb);
                                            }else if ("SYSTEMERROR".equals(retMap.get("err_code"))){
                                                ret=commonManager.saveOrUpdate(dataBase,"update agency_income set currentState=? where keyId=?",new Object[]{"2",agencyIncome.getKeyId()});
                                                logger.warn("orderId={},wx_ret={},update={},content={}",temp,"SYSTEMERROR",ret,postContent);
                                                OutputUtil.output(1000,LanguageUtil.getString("operate_success"),req,resp,false);

                                                HbExchangeRecord hb=new HbExchangeRecord();
                                                hb.setUserId(rc.getAgencyId().longValue());
                                                hb.setMoney(agencyIncome.getTotalIncome());
                                                hb.setWxname(rc.getUserName());
                                                hb.setPhone(rc.getAgencyPhone());
                                                hb.setCreateTime(new Date());
                                                hb.setState(12);
                                                commonManager.save(dataBase,hb);
                                                CashLog log = new CashLog();
                                                log.setAgencyId(rc.getAgencyId());
                                                log.setCashDesc(desc);
                                                log.setCashId(agencyIncome.getKeyId());
                                                log.setCashReport(postContent);
                                                log.setCreateTime(new Date());
                                                log.setState(2);
                                                log.setMoney(agencyIncome.getTotalIncome());
                                                commonManager.save(log);
                                            }else{
                                                ret=commonManager.saveOrUpdate(dataBase,"update agency_income set currentState=? where keyId=?",new Object[]{"0",agencyIncome.getKeyId()});
                                                logger.info("orderId={},retMap={},update={}",temp,retMap,ret);
                                                OutputUtil.output(1007,retMap.get("err_code_des"),req,resp,false);
                                            }
                                        }catch (Exception e){
                                            commonManager.saveOrUpdate(dataBase,"update agency_income set currentState=? where keyId=?",new Object[]{"0",agencyIncome.getKeyId()});
                                            logger.error("Exception:"+e.getMessage(),e);
                                        }
                                    }else{
                                        OutputUtil.output(1006,LanguageUtil.getString("wx_cash_fail"),req,resp,false);
                                    }
                                }
                        }
            }
        } catch (Exception e) {
            logger.error("Exception:"+e.getMessage(),e);
            OutputUtil.output(1003, "系统异常，稍后再试", req, resp, false);
            return;
        }
    }
    /**
     * 每周一十二点之前不能提取上周的返佣
     * @param cal
     * @param dateInt
     * @return
     */
    private static final boolean notCanCash(Calendar cal,int dateInt){
        if (cal.get(Calendar.DAY_OF_WEEK)==Calendar.MONDAY&&cal.get(Calendar.HOUR_OF_DAY)<12){
            if (Integer.parseInt(CommonUtil.dateTimeToString(cal.getTime(),"yyyyMMdd"))-dateInt<=1){
                return true;
            }
        }
        return false;
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/addOrder"})
    public void addOrder(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            RoomCard rc = loadRoomCard(req);
            SystemUser user=loadSystemUser(req);
            if (rc==null||user==null) {
                OutputUtil.output(1004, LanguageUtil.getString("no_auth"), req, resp, false);
                return;
            }else if ((rc.getAgencyLevel()!=null&&rc.getAgencyLevel().intValue()==99)||(user.getRoleId()!=null&&user.getRoleId().intValue()>=1)){
            }else{
                OutputUtil.output(1004, LanguageUtil.getString("no_auth"), req, resp, false);
                return;
            }
            String out_trade_no = req.getParameter("out_trade_no");
            if (!StringUtils.isBlank(out_trade_no)) {
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setOrderId(out_trade_no);
                orderInfo = commonManager.findOne(orderInfo);
                if (null == orderInfo) {
                    OutputUtil.output(1003, "订单不存在", req, resp, false);
                    return;
                }
                if (orderInfo.getIsSent()>0) {
                    OutputUtil.output(1003, "订单支付状态正常，无需补单", req, resp, false);
                } else {
                    HashMap<String, String> map = queryPayResult(out_trade_no);
                    if (null != map && "SUCCESS".equals(map.get("trade_state"))) {
                        int ret = commonManager.saveOrUpdate(SqlHelperUtil.getString("update_addp_pay_result", Constant.ORDER_INFO_FILE)
                                , new Object[]{out_trade_no});
                        if (ret > 0) {
                            RoomCard roomCard = new RoomCard();
                            roomCard.setUserId(orderInfo.getUserId().intValue());
                            roomCard = commonManager.findOne(roomCard);
                            ret = commonManager.saveOrUpdate(SqlHelperUtil.getString("add_cards_agency", Constant.ROOMCARD_FILE)
                                    , new Object[]{orderInfo.getItemNum().intValue(), orderInfo.getUserId()});
                            logger.info("pay success:result={},order={},agency={}", ret, JSON.toJSONString(orderInfo), JSON.toJSONString(roomCard));
                        }
                        OutputUtil.output(1000, "补单成功", req, resp, false);
                    } else {
                        OutputUtil.output(1003, map.get("msg"), req, resp, false);
                    }
                }
            } else {
                OutputUtil.output(1003, "订单号不能为空", req, resp, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            OutputUtil.output(1003, "系统异常，稍后再试", req, resp, false);
            return;
        }
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/offlinePay"})
    public void offlinePay(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            RoomCard rc = loadRoomCard(req);
            SystemUser user=loadSystemUser(req);
            if ( rc== null || rc.getAgencyLevel() == null || (user.getRoleId()==null||user.getRoleId().intValue()<=0)) {
                OutputUtil.output(1004, LanguageUtil.getString("no_auth"), req, resp, false);
                return;
            }
            SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_1");
            int total_fee = NumberUtils.toInt(req.getParameter("money"), 0);
            Integer agencyId=NumberUtils.toInt(req.getParameter("agencyId"), 0);
            Long playerId=NumberUtils.toLong(req.getParameter("playerId"), 0);
            UserInfo usInfo = null;
            int s = 10;
            if(playerId > 0){
            	usInfo = new UserInfo();
            	usInfo.setUserId(playerId);
            	usInfo= commonManager.findOne(dataBase,usInfo);
                if(null==usInfo){
                    OutputUtil.output(1003, "此玩家不存在", req, resp, false);
                    return;
                }
            }
            
            if(agencyId > 0){
            	RoomCard roomCard = new RoomCard();
                roomCard.setAgencyId(agencyId);
                roomCard= commonManager.findOne(dataBase,roomCard);
                if(null==roomCard){
                    OutputUtil.output(1003, "此代理不存在", req, resp, false);
                    return;
                }
                s= 100;
            }
            if (total_fee <= 0) {
                OutputUtil.output(1003, "金额必须大于零", req, resp, false);
                return;
            }
            total_fee = total_fee * s;
            final int orderAmount = total_fee;
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setCreateTime(new Date());
            orderInfo.setOrderAmount(String.valueOf(total_fee));
            String out_trade_no = CommonUtil.getSerializableDigit();
            orderInfo.setOrderId(out_trade_no);
            orderInfo.setIsSent(2);
            orderInfo.setPayType("补录");
            orderInfo.setSellTime(new Date());
            orderInfo.setFlatId("");
            orderInfo.setPayMoney(new BigDecimal(req.getParameter("money")));
            if(agencyId > 0){
            	orderInfo.setServerId(agencyId+"Z");
            }else{
            	orderInfo.setServerId(usInfo.getPayBindId()+"");
            	orderInfo.setUserId(playerId);
            }
            orderInfo.setItemId(orderAmount);
            int ret = commonManager.save(orderInfo);
            OutputUtil.output(MessageBuilder.newInstance()
                            .builderCodeMessage(1000, "OK").builder("out_trade_no", orderInfo.getOrderId()).builder("total_fee", orderInfo.getOrderAmount())
                    , req, resp, null, false);
        } catch (Exception e) {
            e.printStackTrace();
            OutputUtil.output(1003, "系统异常，稍后再试", req, resp, false);
            return;
        }
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/income/pay"})
    public void payIncome(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
    	Integer id = Integer.parseInt(params.get("id"));
    	CashLog log = new CashLog();
    	log.setId(id);
    	log = commonManager.findOne(log);
    	if(log == null){
    		OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "记录不存在"), request, response, null, false);
   		    return;
    	}
    	AgencyIncome  agencyIncome = new AgencyIncome();
    	agencyIncome.setKeyId(log.getCashId());
    	agencyIncome = commonManager.findOne(agencyIncome);
    	if(agencyIncome == null){
    		OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "记录不存在"), request, response, null, false);
   		    return;
    	}
    	String str  = log.getCashReport();
    	String result= PayUtil.post("1367927102",PayUtil.PAY_URL,str);
    	logger.info("提现重发:"+id+",结果报文："+result);
		Map<String, String> retMap = XmlUtils.toMap(result.getBytes("UTF-8"), "UTF-8");
		if ("SUCCESS".equals(retMap.get("return_code")) && "SUCCESS".equals(retMap.get("result_code"))) {
            OutputUtil.output(1000,LanguageUtil.getString("operate_success"),request,response,false);
            log.setState(1);
            commonManager.update(log, new String[]{"id"}, new Object[]{log.getId()});
            agencyIncome.setCurrentState("1");
            commonManager.update(agencyIncome, new String[]{"keyId"}, new Object[]{agencyIncome.getKeyId()});
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "重发成功"), request, response, null, false);
   		    return;
        }else if ("SYSTEMERROR".equals(retMap.get("err_code"))){
        	OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "重发失败"), request, response, null, false);
   		    return;
        }else{
            OutputUtil.output(1007,retMap.get("err_code_des"),request,response,false);
        }
    }

    //调起代理自助购钻H5微信支付
    public void payZyf(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        Map<String, String> reqParams = UrlParamUtil.getParameters(request);
        String type = reqParams.get("type");
        if (StringUtils.isBlank(type)){
            type="1";
        }
        logger.info("params:{}", reqParams);

        final int orderAmount = NumberUtils.toInt(reqParams.get("total_fee"), 0);

        String itemsStr = PropUtil.getString("goods_items", "", Constant.H5PAY_FILE);
        String[] items = itemsStr.split(";");
        String[] currentItem = null;
        for (String item : items) {
            if (item.length() > 0) {
                String[] temps = item.split(",");
                if (Integer.parseInt(temps[0]) == orderAmount) {
                    currentItem = temps;
                    break;
                }
            }
        }

        if (currentItem == null) {
//            request.setAttribute("result", "品项不存在");
            OutputUtil.output(1001, "品项不存在", request, response, false);
            return;//"h5Pay";
        }

        RoomCard roomCard = loadRoomCard(request);
        RoomCard roomCard1=null;
        if (reqParams.containsKey("token")){
            CacheEntity<RoomCard> cacheEntity = CacheEntityUtil.getCache(reqParams.get("token"));
            if (cacheEntity != null){
                roomCard1 = cacheEntity.getValue();
            }

            if (roomCard1 == null){
                OutputUtil.output(1001,LanguageUtil.getString("param_error"),request,response,false);
                return;
            }
        }

        logger.info("good item:userId={},agencyId={},item={},delegate={}", roomCard.getUserId(), roomCard.getAgencyId(), currentItem,roomCard1==null?"":roomCard1.getAgencyId());
        String out_trade_no = CommonUtil.getSerializableDigit();
        JSONObject item = new JSONObject();
        item.put("partner_id", PropUtil.getString("partner_id", "", Constant.H5PAY_FILE));
        item.put("app_id", PropUtil.getString("app_id", "", Constant.H5PAY_FILE));
        item.put("wap_type", type);
        item.put("money", String.valueOf(orderAmount));
        item.put("out_trade_no", out_trade_no);
        item.put("subject", CoderUtil.encode("代理购钻"));
        item.put("imei", roomCard.getUserId());
        item.put("client_ip", IpUtil.getIpAddr(request));
        //item.put("qn", PropUtil.getString("qn", "123", Constant.H5PAY_FILE));
        String signString = Util.getUrlData(item);
        item.put("sign", com.sy.util.MD5Util.md5Encode(signString+"&key="+PropUtil.getString("zyf_key", "", Constant.H5PAY_FILE)).toUpperCase());
        String kystr = Util.getUrlData(item);
        String url = PropUtil.getString("zyf_payUrl", "", Constant.H5PAY_FILE)+kystr;
        logger.info("请求的url:"+url);
        String res = null;
        try {
            URL urls =new URL(url);
            HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            Integer code = conn.getResponseCode();
            logger.info("支付返回的代码值："+code);
            logger.info("请求返回的内容："+conn.getHeaderFields());
            if(code != null && (code==302 || code==200)){
                Map<String, List<String>> map = conn.getHeaderFields();
                String str;
                if(code == 302){
                    str = map.get("Location").get(0).replace("[", "").replace("]", "");
                }else{
                    BufferedInputStream bis = null;
                    bis = new BufferedInputStream(conn.getInputStream(), 1024);
                    int length = -1;
                    StringBuilder results = new StringBuilder();
                    byte[] buf = new byte[1024];
                    while ((length = bis.read(buf)) != -1) {
                        results.append(new String(buf, 0, length, "utf-8"));
                    }
                    str = results.toString();
                    bis.close();
                }
                String result = str;
                /*"https://pay.swiftpass.cn/pay/qrcode?uuid="+URLEncoder.encode(*///);
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setCreateTime(new Date());
                orderInfo.setOrderAmount(String.valueOf(orderAmount));
                orderInfo.setOrderId(out_trade_no);
                orderInfo.setPlatform(reqParams.get("type"));
                orderInfo.setIsSent(0);
                String pf = "weixindtz_zyf";
                orderInfo.setPlatform(pf);
                orderInfo.setSellTime(new Date());
                orderInfo.setFlatId("");
                orderInfo.setItemId(orderAmount);

                orderInfo.setPayMoney(new BigDecimal(orderAmount*1.0/100).setScale(2, BigDecimal.ROUND_HALF_UP));

                if (roomCard1==null){
                    orderInfo.setUserId(roomCard.getUserId().longValue());
                    orderInfo.setServerId(roomCard.getAgencyId()+"Z");

                }else{
                    orderInfo.setUserId(roomCard1.getUserId().longValue());
                    orderInfo.setServerId(roomCard1.getAgencyId()+"Z");
                    orderInfo.setDelegateAgency(roomCard.getAgencyId()+"Z");
                }

                orderInfo.setItemNum(Integer.parseInt(currentItem[1]));

                orderInfo.setFlatId(request.getSession().getId());

                commonManager.save( orderInfo);

                OutputUtil.output(MessageBuilder.newInstance()
                                .builderCodeMessage(1000, "OK").builder("body","代理购钻").builder("pay_info",result).builder("type",(StringUtils.isBlank(type)||type.equals("1"))?"微信支付":"支付宝支付")
                                .builder("out_trade_no", orderInfo.getOrderId()).builder("total_fee", orderInfo.getOrderAmount())
                        , request, response, null, false);

                setSessionValue(request,"order_confirm", "confirm"+out_trade_no);
                return;//"h5Pay";
            }else {
                BufferedInputStream bis = null;
                if (code == 200) {
                    bis = new BufferedInputStream(conn.getInputStream(), 1024);
                } else {
                    bis = new BufferedInputStream(conn.getErrorStream(), 1024);
                }

                int length = -1;
                StringBuilder result = new StringBuilder();
                byte[] buf = new byte[1024];
                while ((length = bis.read(buf)) != -1) {
                    result.append(new String(buf, 0, length, "utf-8"));
                }
                logger.info("微信支付返回的结果:"+result);
                bis.close();

                res = "操作失败";
            }
        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
            res = "系统异常";
        } finally {
            logger.info("request url={},param={}", url, item, res);
        }
        OutputUtil.output(1002, res, request, response, false);
        return;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pay", "/create"})
    public void payUnion(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Constant.requestMap.put(loadRoomCard(request).getAgencyId().toString(),request);
        String payType = PropUtil.getString("pay_type","wft");
        if ("wft".equals(payType)){
            payWft(request,response);
        }else if ("unpay".equals(payType)){
            payUf(request,response);
        }else if ("zyf".equals(payType)){
            payZyf(request,response);
        }
    }

    public void payWft(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        Map<String, String> reqParams = UrlParamUtil.getParameters(request);
        String type = reqParams.get("type");
        logger.info("params:{}", reqParams);

        final Integer orderAmount = NumberUtils.toInt(reqParams.get("total_fee"), 0);
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
        Date d = sim.parse("2018-02-18");
        String itemsStr = PropUtil.getString("goods_items", "", Constant.H5PAY_FILE);
        if(new Date().getTime()-d.getTime() < 0){
            itemsStr = PropUtil.getString("goods_items1", "", Constant.H5PAY_FILE);
        }
        String[] items = itemsStr.split(";");
        String[] currentItem = null;
        for (String item : items) {
            if (item.length() > 0) {
                String[] temps = item.split(",");
                if (Integer.parseInt(temps[0]) == orderAmount) {
                    currentItem = temps;
                    break;
                }
            }
        }

        if (currentItem == null) {
//            request.setAttribute("result", "品项不存在");
            OutputUtil.output(1001, "品项不存在", request, response, false);
            return;//"h5Pay";
        }

        RoomCard roomCard = loadRoomCard(request);
        RoomCard roomCard1=null;
        if (reqParams.containsKey("token")){
            CacheEntity<RoomCard> cacheEntity = CacheEntityUtil.getCache(reqParams.get("token"));
            if (cacheEntity != null){
                roomCard1 = cacheEntity.getValue();
            }

            if (roomCard1 == null){
                OutputUtil.output(1001,LanguageUtil.getString("param_error"),request,response,false);
                return;
            }
        }
        //String url = "https://pay.swiftpass.cn/pay/gateway";
        logger.info("good item:userId={},agencyId={},item={},delegate={}", roomCard.getUserId(), roomCard.getAgencyId(), currentItem,roomCard1==null?"":roomCard1.getAgencyId());
        String out_trade_no = CommonUtil.getSerializableDigit();
        Map<String, Object> payMap = new HashMap<>();
        payMap.put("service", "pay.weixin.wappay");
        payMap.put("mch_id", PropUtil.getString("merId", "", Constant.H5PAY_FILE));
        payMap.put("out_trade_no", out_trade_no);
        payMap.put("body", "代理购钻");
        payMap.put("total_fee", orderAmount.toString());
        payMap.put("mch_create_ip", request.getRemoteAddr());
        String notifyUrl = PropUtil.getString("notifyurl", "", Constant.H5PAY_FILE);
        if (notifyUrl.endsWith("/")){
            notifyUrl+="wft";
        }else{
            notifyUrl+="/wft";
        }
        payMap.put("notify_url", notifyUrl);
        payMap.put("nonce_str", out_trade_no);
        payMap.put("attach","附加信息");
        payMap.put("device_info","AND_WAP");
        payMap.put("mch_app_name","代理购钻");
        payMap.put("mch_app_id","com.tencent.tmgp.sgame");
        payMap.put("version","2.0");
        payMap.put("is_raw", "1");
        payMap.put("sign_type","MD5");
        payMap.put("sign", sign(payMap));
        URL url = new URL("https://pay.swiftpass.cn/pay/gateway");
        String res = null;
        try {
            String xmlStr = XmlUtil.fromMap(payMap, "xml");
            logger.info("支付XML格式数据："+xmlStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            byte[] xmlbyte = xmlStr.toString().getBytes("UTF-8");
            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(xmlbyte);
            outStream.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            StringBuffer sb2 = new StringBuffer();
            String lines;
            while(null !=(lines=in.readLine())){
                sb2.append(lines);
            }
            in.close();
            String result =  sb2.toString();;
            logger.info("create order url:"+url+",result="+result);
            Map<String, Object> resMap = XmlUtil.toMap(result);
            String status =resMap.get("status").toString();
            if (status.equals("0")) {
                String result_code = resMap.get("result_code").toString();
                if (result_code.equals("0")) {
                    OrderInfo orderInfo = new OrderInfo();
                    orderInfo.setCreateTime(new Date());
                    orderInfo.setOrderAmount(String.valueOf(orderAmount));
                    orderInfo.setOrderId(out_trade_no);
                    orderInfo.setPlatform(reqParams.get("type"));
                    orderInfo.setIsSent(0);
                    String pf = "weixindtz_wft";
                    orderInfo.setPlatform(pf);
                    orderInfo.setSellTime(new Date());
                    orderInfo.setFlatId("");
                    orderInfo.setItemId(orderAmount);

                    orderInfo.setPayMoney(new BigDecimal(orderAmount*1.0/100).setScale(2, BigDecimal.ROUND_HALF_UP));

                    if (roomCard1==null){
                        orderInfo.setUserId(roomCard.getUserId().longValue());
                        orderInfo.setServerId(roomCard.getAgencyId()+"Z");

                    }else{
                        orderInfo.setUserId(roomCard1.getUserId().longValue());
                        orderInfo.setServerId(roomCard1.getAgencyId()+"Z");
                        orderInfo.setDelegateAgency(roomCard.getAgencyId()+"Z");
                    }

                    orderInfo.setItemNum(Integer.parseInt(currentItem[1]));

                    orderInfo.setFlatId(request.getSession().getId());
                    String pay_info = resMap.get("pay_info").toString();
                    commonManager.save( orderInfo);
                    OutputUtil.output(MessageBuilder.newInstance()
                                    .builderCodeMessage(1000, "OK").builder("body","代理购钻").builder("pay_info",pay_info).builder("type","微信支付")
                                    .builder("out_trade_no", orderInfo.getOrderId()).builder("total_fee", orderInfo.getOrderAmount())
                            , request, response, null, false);
                    return;
                }
            }

        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
            res = "系统异常";
        } finally {
            logger.info("request url={},param={}", url, payMap, res);
        }
        OutputUtil.output(1002, res, request, response, false);
        return;
    }

    //20180412改为优赋支付
    public void payUf(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        Map<String, String> reqParams = UrlParamUtil.getParameters(request);
        String type ="unpay" ;//reqParams.get("type");
        logger.info("params:{}", reqParams);

        final int orderAmount = NumberUtils.toInt(reqParams.get("total_fee"), 0);
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
        Date d = sim.parse("2018-02-18");
        String itemsStr = PropUtil.getString("goods_items", "", Constant.H5PAY_FILE);
        if(new Date().getTime()-d.getTime() < 0){
            itemsStr = PropUtil.getString("goods_items1", "", Constant.H5PAY_FILE);
        }
        String[] items = itemsStr.split(";");
        String[] currentItem = null;
        for (String item : items) {
            if (item.length() > 0) {
                String[] temps = item.split(",");
                if (Integer.parseInt(temps[0]) == orderAmount) {
                    currentItem = temps;
                    break;
                }
            }
        }

        if (currentItem == null) {
//            request.setAttribute("result", "品项不存在");
            OutputUtil.output(1001, "品项不存在", request, response, false);
            return;//"h5Pay";
        }

        RoomCard roomCard = loadRoomCard(request);
        RoomCard roomCard1=null;
        if (reqParams.containsKey("token")){
            CacheEntity<RoomCard> cacheEntity = CacheEntityUtil.getCache(reqParams.get("token"));
            if (cacheEntity != null){
                roomCard1 = cacheEntity.getValue();
            }

            if (roomCard1 == null){
                OutputUtil.output(1001,LanguageUtil.getString("param_error"),request,response,false);
                return;
            }
        }
        logger.info("good item:userId={},agencyId={},item={},delegate={}", roomCard.getUserId(), roomCard.getAgencyId(), currentItem,roomCard1==null?"":roomCard1.getAgencyId());
        String out_trade_no = CommonUtil.getSerializableDigit();
        Map<String, Object> payMap = new LinkedHashMap<>();
        String merId=PropUtil.getString("unpay_merId", "300494", Constant.H5PAY_FILE);
        payMap.put("customerid",merId );
        payMap.put("sdcustomno", out_trade_no);
        payMap.put("orderAmount", orderAmount);
        payMap.put("cardno", "41");
        String notifyUrl = PropUtil.getString("notifyurl", "", Constant.H5PAY_FILE);
        if (notifyUrl.endsWith("/")){
            notifyUrl+="unpay";
        }else{
            notifyUrl+="/unpay";
        }
        payMap.put("noticeurl", notifyUrl);
        payMap.put("backurl", PropUtil.getString("notifyurl", "", Constant.H5PAY_FILE).replace("pay/notify",""));

        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> kv : payMap.entrySet()) {
            stringBuilder.append("&").append(kv.getKey()).append("=").append(kv.getValue());
        }
        stringBuilder.append(PropUtil.getString("unpay_merId_key_"+merId, "CA1D3153A1CF0ED998D4879FBB50D9AB", Constant.H5PAY_FILE));

        payMap.put("sign", com.sy.mainland.util.MD5Util.getMD5String(stringBuilder.substring(1)));
        payMap.put("mark", "agency_wznn_unpay");
        payMap.put("remarks", CoderUtil.encode("代理购钻"));
        payMap.put("zftype", "2");

        stringBuilder.setLength(0);
        for (Map.Entry<String, Object> kv : payMap.entrySet()) {
            stringBuilder.append("&").append(kv.getKey()).append("=").append(kv.getValue());
        }
        String url="http://api.unpay.com/PayMegerHandler.ashx?"+stringBuilder.substring(1);
        String result = HttpUtil.getUrlReturnValue(url);
        try {
            logger.info("create order url:"+url+",result="+result);
            Element elements = new SAXReader().read(new StringReader(result)).getDocument().getRootElement();

            Iterator<Element> it=((Element)elements.elements().iterator().next()).elements().iterator();
            Map<String,String> resMap=new HashMap<>();
            while (it.hasNext()){
                Element e=it.next();
                if ("item".equals(e.getName())){
                    resMap.put(e.attributeValue("name"),e.attributeValue("value"));
                }
            }
            String status =resMap.get("errcode");
            if ("1111".equals(status)) {
                String pay_info = resMap.get("url");
                if (StringUtils.isNotBlank(pay_info)) {
                    OrderInfo orderInfo = new OrderInfo();
                    orderInfo.setCreateTime(new Date());
                    orderInfo.setOrderAmount(String.valueOf(orderAmount));
                    orderInfo.setOrderId(out_trade_no);
                    orderInfo.setPlatform(type);
                    orderInfo.setIsSent(0);
                    String pf = "weixindtz_unpay";
                    orderInfo.setPlatform(pf);
                    orderInfo.setSellTime(new Date());
                    orderInfo.setFlatId("");
                    orderInfo.setItemId(orderAmount);

                    orderInfo.setPayMoney(new BigDecimal(orderAmount*1.0/100).setScale(2, BigDecimal.ROUND_HALF_UP));

                    if (roomCard1==null){
                        orderInfo.setUserId(roomCard.getUserId().longValue());
                        orderInfo.setServerId(roomCard.getAgencyId()+"Z");
                    }else{
                        orderInfo.setUserId(roomCard1.getUserId().longValue());
                        orderInfo.setServerId(roomCard1.getAgencyId()+"Z");
                        orderInfo.setDelegateAgency(roomCard.getAgencyId()+"Z");
                    }

                    orderInfo.setItemNum(Integer.parseInt(currentItem[1]));

                    orderInfo.setFlatId(request.getSession().getId());
                    commonManager.save( orderInfo);
                    OutputUtil.output(MessageBuilder.newInstance()
                                    .builderCodeMessage(1000, "OK").builder("body","代理购钻").builder("pay_info",pay_info).builder("type","微信支付")
                                    .builder("out_trade_no", orderInfo.getOrderId()).builder("total_fee", orderInfo.getOrderAmount())
                            , request, response, null, false);
                    return;
                }
            }

        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
        }
        OutputUtil.output(1002, "购钻失败，请稍后再试", request, response, false);
        return;
    }
    //处理代理自助购钻H5微信支付结果通知
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/notify","/notify/zyf"})
    public void payNotifyForZyf(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String respString = "fail";
        Map<String, String> params = UrlParamUtil.getParameters(req);
        try {
            logger.info("notify zyf params:" + params);
            req.setCharacterEncoding("utf-8");
            resp.setCharacterEncoding("utf-8");
            resp.setHeader("Content-type", "text/html;charset=UTF-8");
            String retSign = params.remove("sign");
            String status = params.get("code");
            if ("0".equals(status)) {
//                String transaction_id = params.get("invoice_no");
                String out_trade_no = params.get("out_trade_no");
                String total_fee = params.get("money");
                logger.info("订单号:：" + out_trade_no);
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setOrderId(out_trade_no);
                orderInfo = commonManager.findOne(orderInfo);
                if ((orderInfo.getIsSent() == null || orderInfo.getIsSent().intValue() <= 0) && orderInfo.getOrderAmount().equals(total_fee)) {
                    respString = "0";
//                            String sign0 = loadSign(params, config);
//
//                            if (sign0.equalsIgnoreCase(retSign)) {

                    int ret = commonManager.saveOrUpdate(SqlHelperUtil.getString("update_pay_result", Constant.ORDER_INFO_FILE)
                            , new Object[]{out_trade_no});
                    if (ret > 0) {

                        ret = commonManager.saveOrUpdate(SqlHelperUtil.getString("add_cards_agency", Constant.ROOMCARD_FILE)
                                , new Object[]{orderInfo.getItemNum().intValue(), orderInfo.getUserId()});
                        logger.info("pay success:result={},order={}", ret, JSON.toJSONString(orderInfo));

                        commonManager.saveOrUpdate(SqlHelperUtil.getString("update_pay_sell_time", Constant.ORDER_INFO_FILE)
                                , new Object[]{CommonUtil.dateTimeToString(), out_trade_no});

                        RoomCard roomCard = new RoomCard();
                        roomCard.setUserId(orderInfo.getUserId().intValue());
                        roomCard = commonManager.findOne(roomCard);
                        HttpServletRequest r = Constant.requestMap.get(roomCard.getAgencyId().toString());
                        setSessionValue(r, "roomCard", roomCard);
                        if (roomCard != null && (roomCard.getAgencyLevel() == null || roomCard.getAgencyLevel().intValue() == 0)) {
                            int agencyCost = NumberUtils.toInt(PropUtil.getString("agency_cost"), 500);

                            if (orderInfo.getPayMoney().doubleValue() >= agencyCost || (orderInfo.getPayMoney().doubleValue() + commonManager.count(SqlHelperUtil.getString("agency_cards", Constant.ROOMCARD_ORDER_FILE)
                                    , new Object[]{roomCard.getAgencyId() + "Z"})) >= agencyCost) {
                                ret = commonManager.saveOrUpdate(SqlHelperUtil.getString("update_agency_level", Constant.ROOMCARD_FILE), new Object[]{CommonUtil.dateTimeToString(), "1", roomCard.getUserId()});

                                logger.info("update agency level:from {} to {},result={}", roomCard.getAgencyLevel(), 1, ret);
                            }
                        }

                        CacheEntityUtil.setCache("refresh" + orderInfo.getFlatId(), new CacheEntity<>(System.currentTimeMillis(), 10 * 60));
                    }
                }else if (orderInfo.getIsSent()!=null&&orderInfo.getIsSent().intValue()==1){
                    respString="0";
                }
            }
        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
        } finally {
            logger.info("zyf orderId={}, result={}",params.get("out_trade_no"),respString);
            resp.getWriter().write(respString);
            resp.getWriter().flush();
            resp.getWriter().close();
        }
    }


    //处理代理自助购钻H5微信支付结果通知
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/notify/wft"})
    public void payNotifyForWft(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String respString = "fail";
        try {
            req.setCharacterEncoding("utf-8");
            resp.setCharacterEncoding("utf-8");
            resp.setHeader("Content-type", "text/html;charset=UTF-8");
            String resString = XmlUtils.parseRequst(req);
            logger.info("通知内容：" + resString);
            if (resString != null && !"".equals(resString)) {
                Map<String, String> map = XmlUtils.toMap(resString.getBytes(), "utf-8");
                String res = XmlUtils.toXml(map);
                logger.info("通知内容：" + res);
                if (map.get("status").equals("0")) {
                    if (map.get("result_code").equals("0")){
                        String out_trade_no = map.get("out_trade_no");
                        logger.info("订单号:：" + out_trade_no);
                        OrderInfo orderInfo = new OrderInfo();
                        orderInfo.setOrderId(out_trade_no);
                        orderInfo = commonManager.findOne(orderInfo);
                        if ((orderInfo.getIsSent() == null || orderInfo.getIsSent().intValue() <= 0) && orderInfo.getOrderAmount().equals(map.get("total_fee"))) {
                            respString = "success";
                            int ret = commonManager.saveOrUpdate( SqlHelperUtil.getString("update_pay_result", Constant.ORDER_INFO_FILE)
                                    , new Object[]{out_trade_no});
                            if (ret > 0) {

                                ret = commonManager.saveOrUpdate( SqlHelperUtil.getString("add_cards_agency", Constant.ROOMCARD_FILE)
                                        , new Object[]{orderInfo.getItemNum().intValue(), orderInfo.getUserId()});
                                logger.info("pay success:result={},order={}", ret, JSON.toJSONString(orderInfo));

                                commonManager.saveOrUpdate( SqlHelperUtil.getString("update_pay_sell_time", Constant.ORDER_INFO_FILE)
                                        , new Object[]{CommonUtil.dateTimeToString(),out_trade_no});

                                RoomCard roomCard=new RoomCard();
                                roomCard.setUserId(orderInfo.getUserId().intValue());
                                roomCard=commonManager.findOne(roomCard);
                                HttpServletRequest r = Constant.requestMap.get(roomCard.getAgencyId().toString());
                                setSessionValue(r, "roomCard",roomCard);
                                if (roomCard!=null&&(roomCard.getAgencyLevel() == null || roomCard.getAgencyLevel().intValue() == 0)) {
                                    int agencyCost = NumberUtils.toInt(PropUtil.getString("agency_cost"), 500);

                                    if (orderInfo.getPayMoney().doubleValue() >= agencyCost || (orderInfo.getPayMoney().doubleValue() + commonManager.count(SqlHelperUtil.getString("agency_cards", Constant.ROOMCARD_ORDER_FILE)
                                            , new Object[]{roomCard.getAgencyId()+"Z"})) >= agencyCost) {
                                        ret=commonManager.saveOrUpdate(SqlHelperUtil.getString("update_agency_level", Constant.ROOMCARD_FILE),new Object[]{CommonUtil.dateTimeToString(),"1",roomCard.getUserId()});

                                        logger.info("update agency level:from {} to {},result={}",roomCard.getAgencyLevel(),1,ret);
                                    }
                                }

                                CacheEntityUtil.setCache("refresh"+orderInfo.getFlatId(),new CacheEntity<>(System.currentTimeMillis(),10*60));
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
        } finally {
            resp.getWriter().write(respString);
            resp.getWriter().flush();
        }
    }

    //处理代理自助购钻H5微信支付结果通知
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/notify/unpay"})
    public void payNotifyForUnpay(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String respString = "fail";
        try {
            req.setCharacterEncoding("utf-8");
            resp.setCharacterEncoding("utf-8");
            resp.setHeader("Content-type", "text/html;charset=UTF-8");
            Map<String,String> map=UrlParamUtil.getParameters(req);
            logger.info("通知内容：" + map);
            String state=map.get("state");

            String customerid=map.get("customerid");
            String sd51no=map.get("sd51no");
            String sdcustomno=map.get("sdcustomno");
            String mark=map.get("mark");
            String ordermoney=map.get("ordermoney");
            String key=PropUtil.getString("unpay_merId_key_"+customerid, "CA1D3153A1CF0ED998D4879FBB50D9AB", Constant.H5PAY_FILE);
            String sign = com.sy.mainland.util.MD5Util.getMD5String("customerid="+customerid+"&sd51no="+sd51no+"&sdcustomno="+sdcustomno+"&mark="+mark+"&key="+key);

            if (sign.equals(map.get("sign"))){
                sign= com.sy.mainland.util.MD5Util.getMD5String("sign="+sign+"&customerid="+customerid+"&ordermoney="+ordermoney+"&sd51no="+sd51no+"&state="+state+"&key="+key);
                if (sign.equals(map.get("resign"))){
                    respString = "<result>1</result>";
                    if ("1".equals(state)) {
                        OrderInfo orderInfo = new OrderInfo();
                        orderInfo.setOrderId(sdcustomno);
                        orderInfo = commonManager.findOne(orderInfo);
                        int total_fee = Math.round(Float.parseFloat(ordermoney)*100);
                        if (orderInfo!=null&&(orderInfo.getIsSent() == null || orderInfo.getIsSent().intValue() <= 0) && total_fee == Integer.parseInt(orderInfo.getOrderAmount())) {
                            int ret = commonManager.saveOrUpdate( SqlHelperUtil.getString("update_pay_result", Constant.ORDER_INFO_FILE)
                                    , new Object[]{sdcustomno});
                            if (ret > 0) {

                                ret = commonManager.saveOrUpdate( SqlHelperUtil.getString("add_cards_agency", Constant.ROOMCARD_FILE)
                                        , new Object[]{orderInfo.getItemNum().intValue(), orderInfo.getUserId()});
                                logger.info("pay success:result={},order={}", ret, JSON.toJSONString(orderInfo));

                                commonManager.saveOrUpdate( SqlHelperUtil.getString("update_pay_sell_time", Constant.ORDER_INFO_FILE)
                                        , new Object[]{CommonUtil.dateTimeToString(),sdcustomno});

                                RoomCard roomCard=new RoomCard();
                                roomCard.setUserId(orderInfo.getUserId().intValue());
                                roomCard=commonManager.findOne(roomCard);
                                HttpServletRequest r = Constant.requestMap.get(roomCard.getAgencyId().toString());
                                setSessionValue(r, "roomCard",roomCard);
                                if (roomCard!=null&&(roomCard.getAgencyLevel() == null || roomCard.getAgencyLevel().intValue() == 0)) {
                                    int agencyCost = NumberUtils.toInt(PropUtil.getString("agency_cost"), 500);

                                    if (orderInfo.getPayMoney().doubleValue() >= agencyCost || (orderInfo.getPayMoney().doubleValue() + commonManager.count(SqlHelperUtil.getString("agency_cards", Constant.ROOMCARD_ORDER_FILE)
                                            , new Object[]{roomCard.getAgencyId()+"Z"})) >= agencyCost) {
                                        ret=commonManager.saveOrUpdate(SqlHelperUtil.getString("update_agency_level", Constant.ROOMCARD_FILE),new Object[]{CommonUtil.dateTimeToString(),"1",roomCard.getUserId()});

                                        logger.info("update agency level:from {} to {},result={}",roomCard.getAgencyLevel(),1,ret);
                                    }
                                }

                                CacheEntityUtil.setCache("refresh"+orderInfo.getFlatId(),new CacheEntity<>(System.currentTimeMillis(),10*60));
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
        } finally {
            resp.getWriter().write(respString);
            resp.getWriter().flush();
            resp.getWriter().close();
        }
    }

}
