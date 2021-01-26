package com.sy.sanguo.common.util;

import com.sy.mainland.util.PropertiesEncryptFactoryBean;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

public class SyPropertiesEncryptFactoryBean extends PropertiesEncryptFactoryBean {
    @Override
    public String decrypt(String str) {
        try {
            LogUtil.i("jdbc|password|2|"+new net.sy599.common.security.SecuritConstantImpl().decrypt(str));
            return new net.sy599.common.security.SecuritConstantImpl().decrypt(str);
        }catch (Exception e){
            return str;
        }
    }
}
