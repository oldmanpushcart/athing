package com.github.athingx.athing.standard.platform.domain;

import com.github.athingx.athing.standard.component.Identifier;

/**
 * 设备属性快照
 */
public class ThingPropertySnapshot {

    private final Identifier identifier;
    private final Object value;
    private final long timestamp;

    /**
     * 设备属性快照
     *
     * @param identifier 标识
     * @param value      属性值
     * @param timestamp  快照时间戳
     */
    public ThingPropertySnapshot(Identifier identifier, Object value, long timestamp) {
        this.identifier = identifier;
        this.value = value;
        this.timestamp = timestamp;
    }

    /**
     * 获取标识
     *
     * @return 标识
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * 获取属性值
     *
     * @return 属性值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 获取快照时间戳
     *
     * @return 快照时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
}
