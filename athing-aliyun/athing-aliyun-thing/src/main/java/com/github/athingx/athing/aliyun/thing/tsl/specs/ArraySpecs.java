package com.github.athingx.athing.aliyun.thing.tsl.specs;

import com.github.athingx.athing.aliyun.thing.tsl.schema.TslDataType;

public class ArraySpecs implements TslSpecs {

    private final int size;
    private final TslDataType item;

    public ArraySpecs(int size, TslDataType item) {
        this.size = size;
        this.item = item;
    }

    public ArraySpecs(TslDataType item) {
        this(128, item);
    }

    public int getSize() {
        return size;
    }

    public TslDataType getItem() {
        return item;
    }

    @Override
    public TslDataType.Type getType() {
        return TslDataType.Type.ARRAY;
    }
}
