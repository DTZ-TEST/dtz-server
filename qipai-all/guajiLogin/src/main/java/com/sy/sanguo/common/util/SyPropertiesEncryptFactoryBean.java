package com.sy.sanguo.common.util;

import com.sy.mainland.util.PropertiesEncryptFactoryBean;
import org.apache.log4j.Logger;

public class SyPropertiesEncryptFactoryBean extends PropertiesEncryptFactoryBean {
    private static final Logger logger = Logger.getLogger(SyPropertiesEncryptFactoryBean.class);
    @Override
    public String decrypt(String str) {
        try {
            logger.info("jdbc|password|2|"+new net.sy599.common.security.SecuritConstantImpl().decrypt(str));
            return new net.sy599.common.security.SecuritConstantImpl().decrypt(str);
        }catch (Exception e){
            return str;
        }
    }
}
