package com.github.athingx.athing.aliyun.platform.util;

import java.util.Objects;

/**
 * 枚举工具类
 */
public class EnumUtils {

    /**
     * 枚举字面量转换为枚举常量，如无匹配，返回null
     *
     * @param value 枚举字面量
     * @param type  枚举类型
     * @param <T>   枚举类型
     * @return 枚举常量
     */
    public static <T extends Enum<?>> T valueOf(String value, Class<T> type) {
        for (T e : type.getEnumConstants()) {
            if (Objects.equals(e.name(), value)) {
                return e;
            }
        }
        return null;
    }

}
