package com.sy.util;

import com.sy.mainland.util.PropertiesCacheUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 获取配置文件值的工具类
 * @author lz
 */
public final class SqlHelperUtil {

	/**
	 * 根据key获取value的方法
	 * @param key
	 * @return value
	 */
	public static String getString(String key ,String file){
		return getString(key, null ,file);
	}
		
	/**
	 * 根据key获取value的方法(不含缓存处理)
	 * @param key
	 * @param defaultValue 获取不到值时的默认返回值
	 * @param file  要获取值的文件地址
	 * @return value
	 */
	public static String getString(String key,String defaultValue,String file){
		String cacheValue=PropertiesCacheUtil.getValue(key, new StringBuilder(Constant.SQL_DIRECTORY).append(file).toString());
		if(StringUtils.isNotEmpty(cacheValue)){
			return cacheValue;
		}
		return defaultValue;
	}
}
