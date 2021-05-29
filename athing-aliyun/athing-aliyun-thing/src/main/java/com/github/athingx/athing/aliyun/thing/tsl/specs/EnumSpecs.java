package com.github.athingx.athing.aliyun.thing.tsl.specs;

import com.github.athingx.athing.aliyun.thing.tsl.schema.TslDataType;

import java.util.LinkedHashMap;

public class EnumSpecs extends LinkedHashMap<Integer, String> implements TslSpecs {
    @Override
    public TslDataType.Type getType() {
        return TslDataType.Type.ENUM;
    }
}
