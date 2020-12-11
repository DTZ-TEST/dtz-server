package com.sy599.game.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 字符串解析数据工具类
 *
 * @author Gavin 2019/04/15
 */
public class StringSeparator {
    /**
     * 原始字符串数据
     */
    private final String data;
    /**
     * 切割后的数据存放点
     */
    private final List<String> values;
    /**
     * 当前索引,默认从0开始
     */
    private int index;
    /**
     * 最大的数据索引
     */
    private int maxIndex;
    /**
     * 数字匹配正则
     */
    private final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]*");

    /**
     * 构造一个默认的带字符串数据和分隔符的构造函数
     *
     * @param data      默认切割的字符串数据
     * @param separator 分隔符
     */
    public StringSeparator(String data, String separator) {
        this(data, separator, 0);
    }

    /**
     * 构造一个默认的带字符串数据和分隔符的构造函数
     *
     * @param data       默认切割的字符串数据
     * @param separator  分隔符
     * @param startIndex 索引起始位置
     */
    public StringSeparator(String data, String separator, int startIndex) {
        this.data = data;
        String[] valueStrs = data.split(separator);
        this.values = Stream.of(valueStrs).collect(Collectors.toList());
        this.index = startIndex;
        this.maxIndex = values.size();
    }

    /**
     * 根据当前索引所在位置获取对应的字符串数据
     *
     * @return
     */
    public String getStr() {
        return getStr(false);
    }

    /**
     * 采用是否自增的方式获取所在索引位置的字符串数据
     *
     * @param inc 是否自增获取下一个
     * @return
     */
    public String getStr(boolean inc) {
        if (inc) {
            index++;
        }
        checkIndex();
        return values.get(index);
    }

    /**
     * 检查索引是否越界
     */
    public void checkIndex() {
        if (index >= maxIndex) {
            throw new IndexOutOfBoundsException();
        }
    }

    public Integer getInt() {
        return getInt(true);
    }

    public Integer getInt(boolean inc) {
        return getInt(inc, 0);
    }

    public Integer getInt(boolean inc, Integer defValue) {
        String value = getStr(inc);
        return toInt(value, defValue);
    }

    public Long getLong() {
        return getLong(true);
    }

    public Long getLong(boolean inc) {
        return getLong(inc, 0L);
    }

    public Long getLong(boolean inc, Long defValue) {
        String value = getStr(inc);
        return toLong(value, defValue);
    }

    public Boolean getBool() {
        return getBool(true);
    }

    public Boolean getBool(boolean inc) {
        String value = getStr(inc);
        return toInt(value) == 1;
    }

    private Integer toInt(String value, Integer defValue) {
        if (value != null && !value.isEmpty() && isNumeric(value)) {
            return Integer.parseInt(value);
        }
        return defValue;
    }

    private Long toLong(String value) {
        return toLong(value, 0L);
    }

    private Long toLong(String value, Long defValue) {
        if (value != null && !value.isEmpty() && isNumeric(value)) {
            return Long.parseLong(value);
        }
        return defValue;
    }

    private Integer toInt(String value) {
        return toInt(value, 0);
    }

    /**
     * 利用正则表达式判断字符串是否是数字
     *
     * @param str
     * @return
     */
    public boolean isNumeric(String str) {
        Matcher isNum = NUMBER_PATTERN.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 获取最大的索引位置
     *
     * @return
     */
    public int getMaxIndex() {
        return maxIndex;
    }

    /**
     * 获取当前的索引位置
     *
     * @return
     */
    public int getIndex() {
        return index;
    }

    public void resetIndex() {
        resetIndex(0);
    }

    public void resetIndex(int index) {
        this.index = index;
    }
}
