package com.sy.core;

import com.sy.mainland.util.CommonUtil;
import com.sy.util.PropUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Created by pc on 2017/5/5.
 */
public class AutoContextLoaderListener extends ContextLoaderListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoContextLoaderListener.class);

    @Override
    protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac, ServletContext sc) {

        String configLocationParam;
        if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
            configLocationParam = sc.getInitParameter("contextId");
            if (configLocationParam != null) {
                wac.setId(configLocationParam);
            } else {
                wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + ObjectUtils.getDisplayString(sc.getContextPath()));
            }
        }

        wac.setServletContext(sc);
//        configLocationParam = sc.getInitParameter("contextConfigLocation");
        configLocationParam = new StringBuilder("classpath*:").append(CommonUtil.objectToString(PropUtil.getString("version")).replace("/", "")).append("applicationContext.xml").toString();

        System.out.println("contextConfigLocation:" + configLocationParam);
        LOGGER.info("contextConfigLocation:{}", configLocationParam);

        if (configLocationParam != null) {
            wac.setConfigLocation(configLocationParam);
        }

        ConfigurableEnvironment env = wac.getEnvironment();
        if (env instanceof ConfigurableWebEnvironment) {
            ((ConfigurableWebEnvironment) env).initPropertySources(sc,  null);
        }

        this.customizeContext(sc, wac);
        wac.refresh();
    }
}
