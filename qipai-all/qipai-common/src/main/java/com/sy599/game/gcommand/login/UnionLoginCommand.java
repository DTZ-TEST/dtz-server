package com.sy599.game.gcommand.login;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.MD5Util;
import com.sy.mainland.util.MessageBuilder;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.login.util.LoginUtil;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.NettyUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UnionLoginCommand extends BaseCommand{

    @Override
    public void setMsgTypeMap() {

    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

    }

    public int login(MessageUnit message, MyWebSocket socket) throws Exception {
        ComMsg.ComReq comReq = ComMsg.ComReq.parseFrom(message.getContent());
        List<Integer> paramIntsList = comReq.getParamsList();
        List<String> paramsList = comReq.getStrParamsList();

        String paramStr = (paramsList==null||paramsList.size()==0)?"":paramsList.get(0);
        Integer playType = (paramIntsList==null||paramIntsList.size()==0)?0:paramIntsList.get(0);

        if (StringUtils.isBlank(paramStr)){
            socket.send(WebSocketMsgType.union_login_fail, MessageBuilder.newInstance().builder("code",-1).builder("msg","登陆错误，请稍后再试").toString());
            return -1;
        }
        JSONObject jsonObjectLogin = JSONObject.parseObject(paramStr);

        LogUtil.msgLog.info("UnionLoginCommand|login|start|params={}", jsonObjectLogin);

        String sign = String.valueOf(jsonObjectLogin.remove("sign"));
        if ("null".equals(sign)||sign.length()==0){
            socket.send(WebSocketMsgType.union_login_fail, MessageBuilder.newInstance().builder("code",-1).builder("msg","签名信息错误，请稍后再试").toString());
            return -1;
        }
        List<String> paramNames =new ArrayList<>(jsonObjectLogin.keySet());
        Collections.sort(paramNames);

        StringBuilder stringBuilder = new StringBuilder(1024);
        for (String str : paramNames){
            stringBuilder.append("&").append(str).append("=").append(jsonObjectLogin.getString(str));
        }

        stringBuilder.append("&key=");
        String signKey;

        if (playType == null || playType.intValue() <= 0){
            signKey = LoginUtil.DEFAULT_KEY;
        }else{
            BaseTable table = TableManager.getInstance().getInstanceTable(playType);
            if (table == null){
                signKey = LoginUtil.DEFAULT_KEY;
            }else{
                signKey = table.loadSignKey();
            }
        }

        stringBuilder.append(signKey);
        if (!MD5Util.getMD5String(stringBuilder.toString()).equalsIgnoreCase(sign)){
            socket.send(WebSocketMsgType.union_login_fail, MessageBuilder.newInstance().builder("code",-1).builder("msg","签名错误，请稍后再试").toString());
            return -1;
        }

        long startTime = System.currentTimeMillis();
        Map<String,Object> retMap = LoginUtil.login(socket.getCtx(),jsonObjectLogin);

        String retStr;
        if (retMap == null){
            retStr = null;
        }else{
            retMap.put("currentServer",GameServerConfig.SERVER_ID);
            retStr = JSONObject.toJSONString(retMap);
        }

        String ip = NettyUtil.userIpMap.get(socket.getCtx().channel());
        if (StringUtils.isBlank(ip)){
            ip = NettyUtil.getRemoteAddr(socket.getCtx());
        }

        LogUtil.msgLog.info("UnionLoginCommand|login|end|params={},ip={},login result={},time(ms)={}", jsonObjectLogin, ip, retStr, System.currentTimeMillis() - startTime);

        if (retStr == null){
            socket.send(WebSocketMsgType.union_login_fail, MessageBuilder.newInstance().builder("code",-1).builder("msg","登陆异常，请稍后再试").toString());
            return -1;
        }
        Object code = retMap.get("code");

        if ((code instanceof Number)&&((Number)code).intValue()==0){
            socket.send(WebSocketMsgType.union_login_success, retStr);
            return 1;
        }else{
            socket.send(WebSocketMsgType.union_login_fail, retStr);
            return 0;
        }
    }

    public static void main(String[] args) {
        String jsonStr = "{\"code\":\"071MkWKt0DgQOi18gdLt0ST2Lt0MkWK1\",\"c\":\"null\",\"os\":\"Android\",\"syvc\":118,\"sign\":\"efa1d5776d0242422e45a0dc883ad1d2\",\"deviceCode\":\"A0000087599BD7\",\"vc\":118,\"mac\":\"02:00:00:00:00:00\",\"roomId\":\"\",\"p\":\"weixindtz\",\"gamevc\":\"v2.3.30\",\"bind_access_token\":\"\",\"bind_fresh_token\":\"\",\"bind_openid\":\"\"}";
//        check(jsonStr);

        jsonStr = "{\"os\":\"iOS\",\"syvc\":\"33\",\"openid\":\"oW_YGv-G1anHYEElePXaY0USa5m8\",\"sign\":\"019e37ada08bce7729f1a989fe3a76f6\",\"vc\":\"33\",\"roomId\":\"\",\"access_token\":\"23_X9tOynKNRXSRM8U0lJDD1MHFLPv-Z4PuQE9Z0P_v0xCPEdi71fSSQfffpjEckHJ42yFXyW9OY36p_6Ja-Sy9egQtdSS5awEu_dBom2woln8\",\"p\":\"weixindtz\",\"refresh_token\":\"23_X9tOynKNRXSRM8U0lJDD1DbqKLRgJxzS_1LuJU-5XXHH5whN3bDDPY8On5sbc8gREni7aOf5l_N8xxhzrj0ESsfYexGGFc5ENONcYkKDBh4\",\"gamevc\":\"v2.3.30\",\"bind_access_token\":\"\",\"bind_fresh_token\":\"\",\"bind_pf\":\"xianliaodtz\",\"bind_openid\":\"\"}";
//        check(jsonStr);

        jsonStr = "{\"os\":\"iOS\",\"syvc\":\"32\",\"openid\":\"oW_YGvzffDjO9h10KJnNPr5qgUDc\",\"sign\":\"80fad177d2b3cf5ca12b5fe991d59974\",\"vc\":\"32\",\"roomId\":\"\",\"access_token\":\"23_hvBaQjBfKqPNYE_cj9h1ss3CPth8K6UNy1Lk5Fyl5eivqrBpZZjD5jiCDXwUUAahS5UnkmcUSmtPLG6TOF7QQEBVYJT_fszvmsva1QJ7ivo\",\"p\":\"weixindtz\",\"refresh_token\":\"23_hvBaQjBfKqPNYE_cj9h1snnPDQyW4qTKtH7Qf0poY1DSrL72E_lJTedmLwIj3AYM98OyJSZFtu61bdsCQGhYQoBN5cTnqQ9HuVuKN-xjvYc\",\"gamevc\":\"v2.3.30\",\"bind_access_token\":\"ae4d12ec5e5ceddb785b2cc3317bc058\",\"bind_fresh_token\":\"55bd961cb9583128b6c3b9de65a1252d\",\"bind_pf\":\"xianliaodtz\",\"bind_openid\":\"VpqAoHcnmNE3sB30UETxSQ==\"}";
//        check(jsonStr);

        jsonStr = "{\"code\":\"061utpvF1OQc370CCXtF1kuvvF1utpvC\",\"c\":\"null\",\"os\":\"Android\",\"syvc\":120,\"sign\":\"f64fe2e21b4e68968b2b7a136be9cd9f\",\"deviceCode\":\"008796761203911\",\"vc\":120,\"mac\":\"02:00:00:00:00:00\",\"roomId\":\"\",\"p\":\"weixindtz\",\"gamevc\":\"v2.3.31\",\"bind_access_token\":\"\",\"bind_fresh_token\":\"\",\"bind_pf\":\"xianliaodtz\",\"bind_openid\":\"\"}";
//        check(jsonStr);

        jsonStr = "{\"code\":\"011KmU0l0uPjns1XxP1l0BGd1l0KmU0F\",\"c\":\"null\",\"os\":\"Android\",\"syvc\":118,\"sign\":\"f3135b620988a11d7e3411765e47a3bb\",\"deviceCode\":\"A000009A0FF0B0\",\"vc\":118,\"mac\":\"02:00:00:00:00:00\",\"roomId\":\"\",\"p\":\"weixindtz\",\"gamevc\":\"v2.3.30\",\"bind_access_token\":\"\",\"bind_fresh_token\":\"\",\"bind_openid\":\"\"}";
        check(jsonStr);


        jsonStr = "{\"code\":\"071XySuG0tM8Mc2wR1vG0ECHuG0XySux\",\"c\":\"null\",\"os\":\"Android\",\"syvc\":118,\"deviceCode\":\"868738034110359\",\"vc\":118,\"mac\":\"02:00:00:00:00:00\",\"roomId\":\"\",\"p\":\"weixindtz\",\"gamevc\":\"v2.3.30\",\"bind_access_token\":\"\",\"bind_fresh_token\":\"\",\"bind_openid\":\"\"}";
        check(jsonStr);

    }

    public static void check(String jsonStr) {
        JSONObject jsonObjectLogin = JSONObject.parseObject(jsonStr);
        String sign = String.valueOf(jsonObjectLogin.remove("sign"));

        List<String> paramNames = new ArrayList<>(jsonObjectLogin.keySet());
        Collections.sort(paramNames);
        StringBuilder sb = new StringBuilder(1024);
        for (String str : paramNames) {
            sb.append("&").append(str).append("=").append(jsonObjectLogin.getString(str));
        }

        sb.append("&key=");
        String signKey;

        signKey = LoginUtil.DEFAULT_KEY;

        sb.append(signKey);
        System.out.println(sb.toString());
        System.out.println(MD5Util.getMD5String(sb.toString()));
        System.out.println(sign);
    }
}
