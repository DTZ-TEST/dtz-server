package com.sy.controller;

import com.alibaba.fastjson.JSONObject;
import com.sy.entity.pojo.RoomCard;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.OutputUtil;
import com.sy.mainland.util.UrlParamUtil;
import com.sy.util.PropUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/callback/*"})
public class CallbackController extends BaseController {

    private static final String WX_ACCESS_TOKEN="https://api.weixin.qq.com/sns/oauth2/access_token";

    private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/code"})
    public String code(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
        String state=params.get("state");
        String code=params.get("code");

        String state1=getSessionValue(request,"cash_code_state");
        logger.info("cash_code_state:{},params:{}",state1, params);

        if (StringUtils.isNotBlank(state1)){
            removeSessionAttribute(request,"cash_code_state");
        }

        if (StringUtils.isAnyBlank(state,code)){
            OutputUtil.output(1001,PropUtil.getString("param_error"),request,response,false);
        }else if(!state.equals(state1)){
            OutputUtil.output(1002,PropUtil.getString("param_error"),request,response,false);
        }else{
            RoomCard roomCard=loadRoomCard(request);
            if (StringUtils.isNotBlank(roomCard.getOpenid())){
                OutputUtil.output(1003,PropUtil.getString("wx_auth_repeat"),request,response,false);
                return null;
            }

            Map<String,String> map=new LinkedHashMap<>();

            map.put("appid",PropUtil.getString("appid"));
            map.put("secret",PropUtil.getString("secret"));
            map.put("code",code);
            map.put("grant_type","authorization_code");

            String result = HttpUtil.getUrlReturnValue(WX_ACCESS_TOKEN,HttpUtil.DEFAULT_CHARSET,HttpUtil.POST,map);

            logger.info("load access_token url:{},params:{},result:{}",WX_ACCESS_TOKEN,map,result);

            String openid=null;
            if (StringUtils.isNotBlank(result)){
                JSONObject json=JSONObject.parseObject(result);
                openid=json.getString("openid");
            }

            if (StringUtils.isNotBlank(openid)){
                int ret=commonManager.saveOrUpdate("update roomcard set openid=? where agencyId=?",new Object[]{openid,roomCard.getAgencyId()});
                if (ret>0){
                    roomCard.setOpenid(openid);
                    logger.info("wx auth success:agencyId={},openid={}",roomCard.getAgencyId(),openid);

                    return "income";
                }
            }

            OutputUtil.output(1004,PropUtil.getString("wx_auth_fail"),request,response,false);
        }

        return null;
    }
}
