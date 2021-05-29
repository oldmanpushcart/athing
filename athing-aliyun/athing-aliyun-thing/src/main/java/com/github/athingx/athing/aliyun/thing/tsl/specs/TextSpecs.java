package com.github.athingx.athing.aliyun.thing.tsl.specs;

import com.github.athingx.athing.aliyun.thing.tsl.schema.TslDataType;

public class TextSpecs implements TslSpecs {

    private final int length;

    public TextSpecs(int length) {
        this.length = length;
    }

    public TextSpecs() {
        this(2048);
    }

    public int getLength() {
        return length;
    }

    @Override
    public TslDataType.Type getType() {
        return TslDataType.Type.TEXT;
    }
}
