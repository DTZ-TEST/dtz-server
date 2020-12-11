package com.sy.sanguo.common.executor.task;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by pc on 2017/5/19.
 */
public class TenMinuteFixedRateTask extends TimerTask {


    public static final Date loadFirstExecuteDate() {
        Calendar calendar = Calendar.getInstance();
        int m = calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.MINUTE, 10 - (m % 10));
        return calendar.getTime();
    }

    @Override
    public void run() {
        try {
            //加载数据库资源配置
            ResourcesConfigsUtil.initResourcesConfigs();
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }
}
