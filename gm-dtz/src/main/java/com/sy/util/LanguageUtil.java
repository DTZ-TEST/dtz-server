package com.sy.util;

import com.sy.mainland.util.PropertiesCacheUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 获取配置文件值的工具类
 * @author lz
 */
public final class LanguageUtil {
	
	/**
	 * 根据key获取value的方法
	 * @param key 
	 * @return value
	 */
	public static String getString(String key){
		return getString(key, null);
	}
	
	/**
	 * 根据key获取value的方法(含缓存处理)
	 * @param key
	 * @param defaultValue 获取不到值时的默认返回值
	 * @return value
	 */
	public static String getString(String key,String defaultValue){
		return getString(key, defaultValue, null);
	}
		
	/**
	 * 根据key获取value的方法(不含缓存处理)
	 * @param key
	 * @param defaultValue 获取不到值时的默认返回值
	 * @param region  要获取值的文件地址
	 * @return value
	 */
	public static String getString(String key,String defaultValue,String region){
		if (StringUtils.isBlank(region)){
			region=PropUtil.getString("region");
			if (StringUtils.isBlank(region)){
				region="default";
			}
		}
		String cacheValue=PropertiesCacheUtil.getValue(key, new StringBuilder(Constant.LANGUAGE_DIRECTORY).append(region).append(".properties").toString());
		if(StringUtils.isNotEmpty(cacheValue)){
			return cacheValue;
		}
		return defaultValue;
	}
}
