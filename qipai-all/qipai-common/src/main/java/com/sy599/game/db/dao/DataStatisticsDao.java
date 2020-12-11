package com.sy599.game.db.dao;


import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DataStatisticsDao extends BaseDao {
	private static DataStatisticsDao _inst = new DataStatisticsDao();

	public static DataStatisticsDao getInstance() {
		return _inst;
	}

	/**
	 * 数据统计
	 *
	 * @param dataStatistics
	 * @param type <br/>1：单大局大赢家<br/>2：单大局大负豪<br/>3：总小局数<br/>4：单大局赢最多<br/>5：单大局输最多<br/>
	 */
	public void saveOrUpdateDataStatistics(final DataStatistics dataStatistics,final int type){
		TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
			@Override
			public void run() {
				try {
					getSqlLoginClient().update("dataStatistics.save_or_update"+type,dataStatistics);
				}catch (Exception e){
					LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
				}
			}
		});
	}

	public void saveOrUpdateLoginData(final Map<String,Object> map){
		try {
			getSqlLoginClient().update("dataStatistics.save_or_update_login_data",map);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
	}

	public void updateLoginData(final Map<String,Object> map){
		try {
			getSqlLoginClient().update("dataStatistics.update_login_data",map);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
	}

	public void saveOrUpdateOnlineData(final String currentTime,final String serverId,final int currentCount){
		try {
			Map<String,Object> map = new HashMap<>();
			map.put("currentTime",currentTime);
			map.put("serverId",serverId);
			map.put("currentCount",currentCount);
			getSqlLoginClient().update("dataStatistics.save_or_update_online_data",map);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
	}

	public void saveSystemUserCountlogin(final Map<String,Object> map)
	{
		try {
			getSqlLoginClient().update("dataStatistics.save_system_user_countlogin",map);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
	}


    public void saveOrUpdateDataStatisticsBjd(final DataStatistics dataStatistics){
        TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    getSqlLoginClient().update("dataStatistics.save_or_update_bjd",dataStatistics);
                }catch (Exception e){
                    LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
                }
            }
        });
    }

    public DataStatistics loadMaxWzjsOfUser(long userId){
        try {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR,-30);
            Long startDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(c.getTime()));
            Map<String,Object> map = new HashMap<>();
            map.put("userId",userId);
            map.put("startDate",startDate);
            return (DataStatistics)getSqlLoginClient().queryForObject("dataStatistics.loadMaxWzjsOfUser",map);
        }catch (Exception e){
            LogUtil.errorLog.error("loadMaxWzjsOfUser|error|"+e.getMessage(),e);
        }
        return null;
    }
}
