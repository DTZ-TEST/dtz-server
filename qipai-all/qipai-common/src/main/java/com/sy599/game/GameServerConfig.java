package com.sy599.game;

import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.SystemCommonInfo;
import com.sy599.game.db.dao.SystemCommonInfoDao;
import com.sy599.game.db.dao.TableCheckDao;
import com.sy599.game.db.enums.DbEnum;
import com.sy599.game.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

/**
 * 
 * 游戏配置信息
 * 
 * */
public class GameServerConfig {
	public static final String log4j_config_dir = "WEB-INF/config/log4j.properties";
	public static final String jdbc_config_dir = "WEB-INF/config/jdbc.properties";
	public static final String server_config_dir = "WEB-INF/config/server.properties";
	public static final String ibatis_config_dir = "serverconfig/SqlMapConfig.xml";
	public static String keyWord_dir = "WEB-INF/csv/keywords.txt";

	public static volatile int SERVER_ID = 1;
	/** 服务器所在平台 **/
	public static String SERVER_PF = "";

	public static String SERVER_IP = "127.0.0.1";
	public static int SERVER_PORT = 8080;
	public static String LOGSERVER_IP = "127.0.0.1";
	public static int LOGSERVER_PORT = 7111;
	public static String LOGIN_SERVER_ADDRESS = "";
	public static String csv_path = "";

	public static String gameName = "";

	public static Properties game_server_properties = new Properties();

	private static boolean dataStatistics = false;

	/** 数据统计表t_data_statistics是否存在 **/
	public static boolean checkDataStatistics(){
		return dataStatistics;
	}

	/**
	 * 加载服务器配置参数
	 * @param properties
	 */
	public static void load(Properties properties) {
		game_server_properties = properties;
		SERVER_ID = Integer.parseInt(properties.getProperty("server.id"));
		SERVER_PF = properties.getProperty("server.pf","");
		SERVER_IP = properties.getProperty("gameserver.ip");
		SERVER_PORT = Integer.parseInt(properties.getProperty("gameserver.port"));
		LOGSERVER_IP = properties.getProperty("logserver.ip");
		csv_path = properties.getProperty("game.csvpath");
		LOGSERVER_PORT = Integer.parseInt(properties.getProperty("logserver.port"));

		if (StringUtils.isBlank(csv_path)) {
			csv_path = "csv";
		}
		keyWord_dir = "WEB-INF/" + GameServerConfig.csv_path + "/keywords.txt";
		LOGIN_SERVER_ADDRESS = properties.getProperty("loginserver.address");

		SharedConstants.loadTimeOut();
	}

	public static void loadSystemCommonInfo() {
		SystemCommonInfo gameNameInfo = SystemCommonInfoDao.getInstance().selectLogin("gameName");
		if (gameNameInfo != null) {
			if (!StringUtils.isBlank(gameNameInfo.getContent())) {
				gameName = gameNameInfo.getContent();
			}
		}
		try {
			dataStatistics = TableCheckDao.getInstance().checkTableExists(DbEnum.LOGIN, "t_data_statistics");
		}catch (Exception e){
			dataStatistics = false;
		}
	}

	public static boolean isTest() {
		return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("test"));
	}

	public static boolean isDebug() {
		return SERVER_PF.equals("debug");
	}

	public static boolean isDeveloper() {
		return SERVER_IP.startsWith("192.168.1");
	}

	public static boolean isAbroad() { return ResourcesConfigsUtil.loadIntegerValue("ServerConfig","gold_server_abroad",0) == 1;}
}
