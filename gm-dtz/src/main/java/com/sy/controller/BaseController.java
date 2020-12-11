package com.sy.controller;

import com.alibaba.fastjson.JSON;
import com.sy.entity.pojo.RoomCard;
import com.sy.entity.pojo.SystemUser;
import com.sy.mainland.util.IpUtil;
import com.sy.mainland.util.cache.CacheEntity;
import com.sy.mainland.util.cache.CacheEntityUtil;
import com.sy.mainland.util.core.Identifiable;
import com.sy.mainland.util.db.annotation.SelectDataBase;
import com.sy.manager.CommonManager;
import com.sy.util.PropUtil;
import com.sy.util.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * BaseController实现了Identifiable(权限验证)接口
 *
 * @author Administrator
 */
public class BaseController implements Identifiable, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    protected CommonManager commonManager;

    protected static ApplicationContext applicationContext;

    /**
     * 权限验证
     */
    public String verificate() {
        return this.getClass().getName();
    }

    public final static String getCallBack(HttpServletRequest request, String result) {
        String callback = request.getParameter("jsoncallback");
        return new StringBuilder(100).append(callback).append("([").append(result).append("])").toString();
    }

    public final static String getIpAddr(HttpServletRequest request) {
        return IpUtil.getIpAddrByRequest(request);
    }

    public final static <T> T getSessionValue(HttpServletRequest request, String sessionName) {
        return SessionUtil.getSessionValue(request, sessionName);
    }

    public final static HttpSession setSessionValue(HttpServletRequest request, String sessionName, Object sessionValue) {
        return SessionUtil.setSessionValue(request, sessionName, sessionValue);
    }

    public final static HttpSession setSessionValue(HttpServletRequest request, Map<String, Object> sessionMap) {
        return SessionUtil.setSessionValue(request, sessionMap);
    }

    public final static boolean removeSessionAttribute(HttpServletRequest request, String sessionName) {
        return SessionUtil.removeSessionAttribute(request, sessionName);
    }

    public final static SystemUser loadSystemUser(HttpServletRequest request) {
        return getSessionValue(request, "user");
    }

    public final static RoomCard loadRoomCard(HttpServletRequest request) {
        RoomCard roomCard = getSessionValue(request, "roomCard");
        CacheEntity ce1 = CacheEntityUtil.deleteCache("refresh" + request.getSession().getId());
        CacheEntity ce2 = CacheEntityUtil.deleteCache("refresh agency" + roomCard.getAgencyId());
        if (roomCard != null && (ce1 != null || ce2 != null)) {
            RoomCard roomCard0 = new RoomCard();
            roomCard0.setAgencyId(roomCard.getAgencyId());
            try {

                roomCard0 = applicationContext.getBean(CommonManager.class).findOne(roomCard0);
                logger.info("refresh roomCard:before={},after={}", JSON.toJSONString(roomCard), JSON.toJSONString(roomCard0));
                roomCard = roomCard0;

                setSessionValue(request, "roomCard", roomCard);
            } catch (Exception e) {
                logger.error("Exception:" + e.getMessage(), e);
            }
            setSessionValue(request, "notice_pay", "1");
        }
        return roomCard;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BaseController.applicationContext = applicationContext;
    }
}
