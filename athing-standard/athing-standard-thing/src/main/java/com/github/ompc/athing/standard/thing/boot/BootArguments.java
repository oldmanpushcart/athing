package com.github.ompc.athing.standard.thing.boot;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 引导参数
 */
public class BootArguments {

    private final Set<Entry> entries = new LinkedHashSet<>();

    /**
     * 解析查询字符串为引导参数
     *
     * @param string 查询字符串
     * @return 引导参数
     */
    public static BootArguments parse(String string) {
        final BootArguments arguments = new BootArguments();
        if (null != string) {
            for (final String segment : string.split("&")) {
                if (null == segment) {
                    continue;
                }
                final String[] pairs = segment.split("=");
                if (pairs.length != 2) {
                    continue;
                }
                try {
                    arguments.entries.add(new Entry(
                            URLDecoder.decode(pairs[0], "UTF-8"),
                            URLDecoder.decode(pairs[1], "UTF-8")
                    ));
                } catch (UnsupportedEncodingException cause) {
                    throw new RuntimeException(cause);
                }
            }
        }
        return arguments;
    }

    /**
     * 添加同名多值参数
     * <pre>
     *     如果已经存在同名参数，则自动追加到同名参数名下
     * </pre>
     *
     * @param name      参数名
     * @param converter 转换器
     * @param values    参数值数组
     * @param <T>       参数值类型
     * @return this
     */
    @SafeVarargs
    public final <T> BootArguments putArguments(String name, Converter<T> converter, T... values) {
        if (null != values) {
            for (T value : values) {
                if (null != value) {
                    entries.add(new Entry(name, converter.toString(value)));
                }
            }
        }
        return this;
    }

    /**
     * 获取单名单值
     * <pre>
     *     如果参数为同名多值，只返回第一个值
     * </pre>
     *
     * @param name      参数名
     * @param converter 转换器
     * @param <T>       参数值类型
     * @return 参数值
     */
    public <T> T getArgument(String name, Converter<T> converter) {
        return entries.stream()
                .filter(entry -> Objects.equals(entry.name, name))
                .findFirst()
                .map(entry -> converter.convert(entry.value))
                .orElse(null);
    }

    /**
     * 获取单名单值
     *
     * @param name      参数名
     * @param converter 转换器
     * @param def       默认值
     * @param <T>       参数值类型
     * @return 参数值
     */
    public <T> T getArgument(String name, Converter<T> converter, T def) {
        final T value = getArgument(name, converter);
        return null != value
                ? value
                : def;
    }

    /**
     * 获取同名多值
     *
     * @param name      参数名
     * @param converter 转换器
     * @param <T>       参数值类型
     * @return 参数值
     */
    public <T> List<T> getArguments(String name, Converter<T> converter) {
        return entries.stream()
                .filter(entry -> Objects.equals(entry.name, name))
                .map(entry -> converter.convert(entry.value))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        final StringBuilder toStringSB = new StringBuilder();
        entries.forEach(entry -> {
            try {

                // 如果不是第一个参数，需要加上"&"分隔符
                if (toStringSB.length() > 0) {
                    toStringSB.append("&");
                }

                // 添加KV对
                toStringSB
                        .append(URLEncoder.encode(entry.name, "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.value, "UTF-8"));

            } catch (UnsupportedEncodingException cause) {
                throw new RuntimeException(cause);
            }
        });
        return toStringSB.toString();
    }

    /**
     * 转换器
     *
     * @param <T> 类型
     */
    public interface Converter<T> {

        Converter<Short> cShort = Short::parseShort;
        Converter<Integer> cInt = Integer::parseInt;
        Converter<Long> cLong = Long::parseLong;
        Converter<Double> cDouble = Double::parseDouble;
        Converter<Float> cFloat = Float::parseFloat;
        Converter<Boolean> cBoolean = Boolean::parseBoolean;
        Converter<String> cString = string -> string;

        /**
         * 字面量转换为值
         *
         * @param string 字面量
         * @return 值
         */
        T convert(String string);

        /**
         * 值转换为字面量
         *
         * @param value 值
         * @return 字面量
         */
        default String toString(T value) {
            return value.toString();
        }

    }

    /**
     * KV键值对
     */
    private static class Entry {

        private final String name;
        private final String value;

        private Entry(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Entry)) {
                return false;
            }
            final Entry entry = (Entry) obj;
            return Objects.equals(name, entry.name)
                    && Objects.equals(value, entry.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }

    }

}
