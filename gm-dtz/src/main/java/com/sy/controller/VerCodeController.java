package com.sy.controller;

import com.alibaba.fastjson.JSON;
import com.sy.entity.message.CodeImage;
import com.sy.entity.pojo.SystemUser;
import com.sy.mainland.util.OutputUtil;
import com.sy.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Controller
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/vercode/*"})
public class VerCodeController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(VerCodeController.class);

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/telcode"})
    public void telCode(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Long pre = getSessionValue(request, "telCode_current");

        if (pre != null && NumberUtils.toLong(PropUtil.getString("sms_time_interval"), 60000) >= (System.currentTimeMillis() - pre)) {
            OutputUtil.output(1003, LanguageUtil.getString("frequent_operation"), request, response, false);
            return;
        }

        String tel = request.getParameter("tel");
        SystemUser user = loadSystemUser(request);
        if ((StringUtils.isBlank(tel) || (!StringUtil.isPhoneNumber(tel = tel.trim()))) && user == null) {
            OutputUtil.output(1001, LanguageUtil.getString("tel_error"), request, response, false);
        } else {
            if (user == null) {
                SystemUser systemUser = new SystemUser();
                systemUser.setUserTel(tel);

                if("1".equals(request.getParameter("check"))){
                    if (commonManager.findOne(systemUser) == null) {
                        OutputUtil.output(1002, LanguageUtil.getString("user_not_exists"), request, response, false);
                        return;
                    }
                }else{
                    if (commonManager.findOne(systemUser) != null) {
                        OutputUtil.output(1002, LanguageUtil.getString("tel_exists"), request, response, false);
                        return;
                    }
                }

            } else {
                tel = user.getUserTel();
            }

            String code = StringUtil.getRandomString(5, StringUtil.CharacterType.DIGIT);
            setSessionValue(request, "telCode_current", System.currentTimeMillis());
            setSessionValue(request, "telCode", tel + "," + code);
            setSessionValue(request, "telCode_expire", System.currentTimeMillis() + NumberUtils.toLong(PropUtil.getString("telCode_expire"), 5 * 60 * 1000));

            String result = SMSUtil.sendSMS(PropUtil.getString("sms_appId")
                    , PropUtil.getString("sms_appKey")
                    , PropUtil.getString("sms_msg").replace("{}", code)
                    , tel);
            Object message = "ERROR";
            if (StringUtils.isNotBlank(result)) {
                message = JSON.parseObject(result).get("errmsg");
            }
            OutputUtil.output(1000, message, request, response, false);
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/code"})
    public void code(HttpServletRequest request, HttpServletResponse response) {
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        // 禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            CodeImage codeImage = ValidateCodeUtil.createCode();
            if (codeImage != null) {
                AccountUtil.putVerCode(request, codeImage.code);
                ValidateCodeUtil.write(codeImage.image, outputStream);
            }
        } catch (Exception e) {
            logger.error("error message:" + e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    logger.error("error message:" + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public String verificate() {
        return null;
    }
}
