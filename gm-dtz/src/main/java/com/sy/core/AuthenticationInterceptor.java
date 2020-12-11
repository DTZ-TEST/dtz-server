package com.sy.core;

import com.sy.mainland.util.core.CoreHandlerInterceptor;
import com.sy.mainland.util.core.Identifiable;
import com.sy.util.SessionUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationInterceptor extends CoreHandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    private String[] allowContains;

    public void setAllowContains(String[] allowContains) {
        this.allowContains = allowContains;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String uri = request.getRequestURI();
        String context = request.getContextPath();

        if (StringUtils.isNotEmpty(context)) {
            uri = uri.substring(context.length());
        }

        System.out.println("curreent uri:"+uri);

        if (allowContains!=null&&allowContains.length>0){
            for(int i=0;i<allowContains.length;i++){
                if (uri.contains(allowContains[i])){
                    return true;
                }
            }
        }

        int mark;

        if ((mark = uri.startsWith("/page/index") ? 1 : (uri.startsWith("/user/login") ? 2 :
                (uri.startsWith("/page/register") ? 3 : (uri.startsWith("/user/register") ? 4 :
                        (uri.startsWith("/vercode/telcode") ? 5 : (uri.startsWith("/page/forgot") ? 6 :
                                (uri.startsWith("/user/forgot") ? 7:(uri.startsWith("/pay/notify") ? 8 : -1)))))))) > 0) {

            if (mark == 5) {
                String referer = request.getHeader("Referer");
                if (referer == null || !referer.startsWith(new StringBuilder(80).append(request.getScheme()).append("://").append(request.getServerName()).toString())) {
//                    OutputUtil.output(2001, LanguageUtil.getString("no_auth", "Not support!"), request, response, false);
//                    return false;
                    LOGGER.info("referer:{}", referer);
                }
            }


            if (SessionUtil.getSessionValue(request, "user") != null) {
                if (mark == 5) {
                    return true;
                } else {
                    response.setHeader("REQUIRES_AUTH_URL",context+"/page/home");
                    request.getRequestDispatcher("/page/home").forward(request, response);
                    return false;
                }

            }
            return true;
        }

        if (allowUrls != null && (mark = allowUrls.length) > 0) {
            for (int i = 0; i < mark; i++) {
                if (uri.equals(allowUrls[i])) {
                    return true;
                }
            }
        }

        // 权限验证
        Object controller = ((HandlerMethod) handler).getBean();
        if (controller instanceof Identifiable) {
            Identifiable identifiable = (Identifiable) controller;
            if (identifiable.verificate() == null) {
                return true;
            } else {
                if (SessionUtil.getSessionValue(request, "userSession") == null) {
                    response.setHeader("REQUIRES_AUTH_URL",context+"/page/index");
                    request.getRequestDispatcher("/page/index").forward(request, response);
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            response.setHeader("REQUIRES_AUTH_URL",context+"/page/index");
            request.getRequestDispatcher("/page/index").forward(request, response);
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }
}
