package com.sy.sanguo.game.pdkuai.manager;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.LoginCacheContext;
import com.sy.sanguo.game.pdkuai.db.dao.TableLogDao;

public class TableLogManager {
	private static TableLogManager _inst = new TableLogManager();

	public static TableLogManager getInstance() {
		return _inst;
	}

	public void checkDelLog() {
		int hour = 4*24;
		if (LoginCacheContext.isTestServer()) {
			hour = 7*24;
		}
		int delCount = TableLogDao.getInstance().delLogByHour(hour);
		GameBackLogger.SYS_LOG.info("打牌日志清理数123:" + delCount);
	}
}
