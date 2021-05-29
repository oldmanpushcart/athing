package com.github.athingx.athing.aliyun.framework.util;

import java.util.HashMap;

/**
 * Map对象
 * <p>
 * 集联操作，便于构造一个对象进行Json序列化
 * </p>
 */
public class MapObject extends HashMap<String, Object> {

    private final transient MapObject parent;

    public MapObject() {
        this(null);
    }

    private MapObject(MapObject parent) {
        this.parent = parent;
    }

    public MapObject putProperty(String name, Object value) {
        put(name, value);
        return this;
    }

    public MapObject enterProperty(String name) {
        final MapObject mapObject = new MapObject(this);
        put(name, mapObject);
        return mapObject;
    }

    public MapObject exitProperty() {
        if (null == parent) {
            throw new IllegalStateException("root");
        }
        return parent;
    }

}
