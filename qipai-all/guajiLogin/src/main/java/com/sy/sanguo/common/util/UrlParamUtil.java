package com.sy.sanguo.common.util;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class UrlParamUtil {
	public static final String DEFAULT_CHARSET = "UTF-8";

	/**
	 * 获取url参数(UTF-8)
	 * 
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final Map<String, String> getParameters(HttpServletRequest request)
			throws UnsupportedEncodingException {
		return getParameters(request, DEFAULT_CHARSET);
	}

	/**
	 * 获取url参数
	 * 
	 * @param request
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final Map<String, String> getParameters(HttpServletRequest request,
			String charset) throws UnsupportedEncodingException {
		Map<String, String> paramMap = new HashMap<String, String>();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String paramValue = request.getParameter(paramName);
			if (paramValue == null) {
				paramValue = "";
			}
			paramMap.put(CoderUtil.decode(paramName),
					CoderUtil.decode(paramValue, charset));
		}
		return paramMap;
	}
}
