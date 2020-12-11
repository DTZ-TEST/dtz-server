package com.sy.sanguo.common.util;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class StringUtil {
	public static String filterEmoji(String source) {
		if (StringUtils.isNotBlank(source)) {
			return source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "");
		} else {
			return source;
		}
	}

	// public static String filterEmoji(String source) {
	// if (source != null) {
	// Pattern emoji =
	// Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
	// Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
	// Matcher emojiMatcher = emoji.matcher(source);
	// if (emojiMatcher.find()) {
	// source = emojiMatcher.replaceAll("");
	// return source;
	// }
	// return source;
	// }
	// return source;
	// }

	public static void loadFromFile(Properties properties, String dir) throws Exception {
		try {
			FileInputStream fis = new FileInputStream(dir);
			properties.load(fis);
			fis.close();
		} catch (Exception e) {
			throw e;
		}
	}

	public static boolean isFindEmoji(String source) {
		if (source != null) {
			Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
			Matcher emojiMatcher = emoji.matcher(source);
			if (emojiMatcher.find()) {
				// source = emojiMatcher.replaceAll("*");
				return true;
			}
			return false;
		}
		return false;
	}

	public static String emojiConvert1(String str) throws UnsupportedEncodingException {
		String patternString = "([\\x{10000}-\\x{10ffff}\ud800-\udfff])";

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			try {
				matcher.appendReplacement(sb, "[[" + URLEncoder.encode(matcher.group(1), "UTF-8") + "]]");
			} catch (UnsupportedEncodingException e) {
				throw e;
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * @Description 还原utf8数据库中保存的含转换后emoji表情的字符串
	 * @param str
	 *            转换后的字符串
	 * @return 转换前的字符串
	 * @throws UnsupportedEncodingException
	 *             exception
	 */
	public static String emojiRecovery2(String str) throws UnsupportedEncodingException {
		String patternString = "\\[\\[(.*?)\\]\\]";

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(str);

		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			try {
				matcher.appendReplacement(sb, URLDecoder.decode(matcher.group(1), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw e;
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public static String contentToEmoji(String content) {
		return StringEscapeUtils.unescapeJava(content.replace("\\\\u", "\\u"));
	}

	public static String emojiToContent(String emoji) {
		return StringEscapeUtils.escapeJava(emoji);
	}

	/**
	 * 小写字符串
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomLowerString(int length) {
		String str = "abcdefghijklmnopqrstuvwxyz";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < length; ++i) {
			int number = random.nextInt(str.length());// [0,62)
			sb.append(str.charAt(number));
		}
		return sb.toString().toLowerCase();
	}

	/**
	 * 随机字符串
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length) {
		Random random = new Random();

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < length; ++i) {
			int number = random.nextInt(3);
			long result = 0;

			switch (number) {
			case 0:
				result = Math.round(Math.random() * 25 + 65);
				sb.append(String.valueOf((char) result));
				break;
			case 1:
				result = Math.round(Math.random() * 25 + 97);
				sb.append(String.valueOf((char) result));
				break;
			case 2:
				sb.append(String.valueOf(new Random().nextInt(10)));
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * 随机获得一个UUID，这个值是唯一的
	 * 
	 * @return String
	 * @throws
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString().replace("_", "");
	}

	/**
	 * 将字符串分隔后，转换成double[]
	 * 
	 * @param str
	 * @param delimiter
	 * @return double[]
	 */
	public static double[] explodeToDoubleArray(String str, String delimiter) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(delimiter);
		double[] result = new double[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Double.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * 将字符串分隔后，转换成double[]
	 * 
	 * @param str
	 * @param delimiter
	 * @return double[]
	 */
	public static double[] explodeToDoubleArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		double[] result = new double[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Double.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * 将字符串分隔后，转换成long[]
	 * 
	 * @param str
	 * @return long[]
	 */
	public static long[] explodeToLongArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		long[] result = new long[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Long.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * 将字符串分隔后，转换成int[]
	 * 
	 * @param str
	 * @param delimiter
	 * @return int[]
	 */
	public static int[] explodeToIntArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		int[] result = new int[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Integer.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * 将字符串分隔后，转换成int[]
	 * 
	 * @param str
	 * @param delimiter
	 * @return int[]
	 */
	public static String[] explodeToStringArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		String[] result = new String[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = val;
			i++;
		}
		return result;
	}

	public static String[] explodeToStringArray(String str, String delimiter) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(delimiter);
		String[] result = new String[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = val;
			i++;
		}
		return result;
	}

	/**
	 * 将字符串分隔后，转换成int[]
	 * 
	 * @param str
	 * @param delimiter
	 * @return int[]
	 */
	public static int[] explodeToIntArray(String str, String delimiter) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(delimiter);
		int[] result = new int[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Integer.valueOf(val);
			i++;
		}
		return result;
	}

	public static List<Integer> explodeToIntList(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		List<Integer> result = new ArrayList<Integer>(strArray.length);

		for (String val : strArray) {
			result.add(Integer.valueOf(val));
		}
		return result;
	}

	public static List<List<Long>> explodeToLongLists(String str) {
		if (StringUtils.isBlank(str))
			return null;
		List<List<Long>> lists = new ArrayList<List<Long>>();
		String strArray[] = str.split(";");
		for (String listValues : strArray) {
			List<Long> list = explodeToLongList(listValues);
			if (list != null) {
				lists.add(list);
			}

		}
		return lists;
	}

	public static List<Long> explodeToLongList(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		List<Long> result = new ArrayList<Long>(strArray.length);

		for (String val : strArray) {
			result.add(Long.valueOf(val));
		}
		return result;
	}

	/**
	 * 将array组合成用delimiter分隔的字符串
	 * 
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static String implode(int[] array, String delimiter) {
		StringBuilder sb = new StringBuilder("");
		for (int i : array) {
			sb.append(i);
			sb.append(delimiter);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	/**
	 * 将array组合成用,分隔的字符串
	 * 
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static String implode(int[] array) {
		StringBuilder sb = new StringBuilder("");
		for (int i : array) {
			sb.append(i);
			sb.append(",");
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	public static String implode(List<?> array){
		return implode(array,",");
	}
	/**
	 * 将array组合成用,分隔的字符串
	 * 
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static String implode(List<?> array,String delimiter) {
		StringBuilder sb = new StringBuilder("");
		for (Object i : array) {
			sb.append(i);
			sb.append(delimiter);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	public static String implode(long[] array) {
		StringBuilder sb = new StringBuilder("");
		for (long i : array) {
			sb.append(i);
			sb.append(",");
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	public static String implode(String[] array) {
		return StringUtils.join(array, ",");
	}

	public static String implode(String[] array, String delimiter) {
		return StringUtils.join(array, delimiter);
	}

	/**
	 * getValue读取csv
	 * 
	 * @param values
	 * @param index
	 * @return
	 */
	public static String getValue(String[] values, int index) {
		if (index >= values.length) {
			return "";
		}
		return values[index];
	}

	/**
	 * getValue读取csv
	 * 
	 * @param values
	 * @param index
	 * @return
	 */
	public static String getValue(String[] values, int index, String def) {
		if (index >= values.length) {
			return def;
		}
		return values[index];
	}

	public static long getLongValue(String[] values, int index) {
		String value = getValue(values, index);
		if (StringUtils.isBlank(value)) {
			return 0;
		}
		return Long.parseLong(value);
	}

	public static int getIntValue(String[] values, int index) {
		String value = getValue(values, index);
		if (StringUtils.isBlank(value)) {
			return 0;
		}
		return Integer.parseInt(value);
	}
	public static float getFloatValue(String[] values, int index) {
		String value = getValue(values, index);
		if (StringUtils.isBlank(value)) {
			return 0;
		}
		return Float.parseFloat(value);
	}
	public static void main(String[] arg) {
		try {
			String text = "This is a smiley \uD83C\uDFA6 face\uD860\uDD5D \uD860\uDE07 \uD860\uDEE2 \uD863\uDCCA \uD863\uDCCD \uD863\uDCD2 \uD867\uDD98 ";
			System.out.println(text);
			System.out.println(text.length());
			System.out.println(text.replaceAll("[\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]", "*"));
			System.out.println(filterEmoji(text));
			System.out.println(filterEmoji("123"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
