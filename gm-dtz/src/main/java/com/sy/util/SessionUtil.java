package com.sy.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by pc on 2017/4/14.
 */
public final class SessionUtil {

    private static final int time_out = 8 * 60 * 60;

    public final static boolean clearSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session == null) {
            return false;
        } else {
            session.invalidate();
            return true;
        }
    }

    public final static boolean removeSessionAttribute(HttpServletRequest request, String sessionName) {
        HttpSession session = request.getSession();
        if (session == null) {
            return false;
        } else {
            session.removeAttribute(sessionName);
            return true;
        }
    }

    public final static <T> T getSessionValue(HttpServletRequest request, String sessionName) {
        HttpSession session = request.getSession();
        return session == null ? null : (T) session.getAttribute(sessionName);
    }

    public final static HttpSession setSessionValue(HttpServletRequest request, String sessionName, Object sessionValue) {
        HttpSession session = request.getSession();
        if (session == null) {
            return null;
        } else {
            String timeoutStr = PropUtil.getString("session_timeout");
            int currentTimeout;
            if (StringUtils.isNotBlank(timeoutStr)) {
                currentTimeout = NumberUtils.toInt(timeoutStr, time_out);
            } else {
                currentTimeout = time_out;
            }

            session.setAttribute(sessionName, sessionValue);

            session.setMaxInactiveInterval(currentTimeout);//以秒为单位
            return session;
        }
    }

    public final static HttpSession setSessionValue(HttpServletRequest request, Map<String, Object> sessionMap) {
        HttpSession session = request.getSession();
        if (session == null) {
            return null;
        } else {
            String timeoutStr = PropUtil.getString("session_timeout");
            int currentTimeout;
            if (StringUtils.isNotBlank(timeoutStr)) {
                currentTimeout = NumberUtils.toInt(timeoutStr, time_out);
            } else {
                currentTimeout = time_out;
            }

            for (Map.Entry<String, Object> kv : sessionMap.entrySet()) {
                session.setAttribute(kv.getKey(), kv.getValue());
            }

            session.setMaxInactiveInterval(currentTimeout);//以秒为单位
            return session;
        }
    }
}
