package com.sy.util;

import com.sy.mainland.util.IpUtil;
import com.sy.mainland.util.PropertiesCacheUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取配置文件值的工具类
 * @author lz
 */
public final class PropUtil {

	/**
	 * 项目的主配置文件名称
	 */
	public final static String BASEPROP = Constant.CORE_FILE;
	
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
		return getString(key, defaultValue, BASEPROP);
	}
		
	/**
	 * 根据key获取value的方法(不含缓存处理)
	 * @param key
	 * @param defaultValue 获取不到值时的默认返回值
	 * @param baseFile  要获取值的文件地址
	 * @return value
	 */
	public static String getString(String key,String defaultValue,String baseFile){
		String cacheValue=PropertiesCacheUtil.getValue(key, baseFile);
		if(StringUtils.isNotEmpty(cacheValue)){
			return cacheValue;
		}
		return defaultValue;
	}
	
	/**
	 * 获取基础vpn.ip
	 * @return 字符串格式 ip
	 */
	public static String getBaseVpnIp(){
		return getString("base.vpn.ip");
	}

	/**
	 * 判断当前ip是否在系统允许的ip白名单当中
	 * @param ip
	 * @return
	 */
	public static boolean isAllowIp(String ip){
		if(IpUtil.isIntranet(ip)){
			return true;
		}
		String allowIps=getBaseVpnIp();
		return allowIps!=null&&allowIps.contains(ip);
	}

	/**
	 * @see #isAllowIp(String)
	 * @param ip
	 * @return
	 */
	public static boolean isNotAllowIp(String ip){
		return !isAllowIp(ip);
	}

	/**
	 * 判断当前ip是否在系统允许的ip白名单当中
	 * @param request
	 * @return
	 */
	public static boolean isAllowIp(HttpServletRequest request){
		if(IpUtil.isIntranet(request)){
			return true;
		}
		String allowIps=getBaseVpnIp();
		return allowIps!=null&&allowIps.contains(IpUtil.getIpAddr(request));
	}

	/**
	 * @see #isAllowIp(HttpServletRequest)
	 * @param request
	 * @return
	 */
	public static boolean isNotAllowIp(HttpServletRequest request){
		return !isAllowIp(request);
	}

}
