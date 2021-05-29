package com.github.athingx.athing.aliyun.framework.component.meta;

import com.github.athingx.athing.standard.component.annotation.ThParam;

/**
 * 服务参数元数据
 */
public class ThParamMeta {

    private final ThParam anThParam;
    private final Class<?> paramType;
    private final int paramIndex;

    ThParamMeta(ThParam anThParam, Class<?> paramType, int paramIndex) {
        this.anThParam = anThParam;
        this.paramType = paramType;
        this.paramIndex = paramIndex;
    }

    /**
     * 获取参数命名
     *
     * @return 参数命名
     */
    public String getName() {
        return anThParam.value();
    }

    /**
     * 获取参数类型
     *
     * @return 参数类型
     */
    public Class<?> getType() {
        return paramType;
    }

    /**
     * 获取参数下标
     *
     * @return 参数下标
     */
    public int getIndex() {
        return paramIndex;
    }
}
