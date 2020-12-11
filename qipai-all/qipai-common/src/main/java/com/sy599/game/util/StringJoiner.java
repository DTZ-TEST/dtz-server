package com.sy599.game.util;

import java.util.Objects;

/**
 * 字符串添加工具类
 *
 * @author Gavin 2019/04/15
 */
public final class StringJoiner {
    private final java.util.StringJoiner joiner;

    public StringJoiner(String separator) {
        this(separator, "", "");
    }

    public StringJoiner(String delimiter,
                        String prefix,
                        String suffix) {
        this.joiner = new java.util.StringJoiner(delimiter, prefix, suffix);
    }

    /**
     * 追加数据
     *
     * @param value
     * @return
     */
    public StringJoiner add(Object value) {
        Objects.requireNonNull(value);
        joiner.add(String.valueOf(value));
        return this;
    }

    @Override
    public String toString() {
        return joiner.toString();
    }

    public static void main(String[] args) {
        StringJoiner append = new StringJoiner(",");
        append.add("hello").add("world").add("java").add("python").add("c++");
        System.out.println("args = [" + append.toString() + "]");
    }
}
