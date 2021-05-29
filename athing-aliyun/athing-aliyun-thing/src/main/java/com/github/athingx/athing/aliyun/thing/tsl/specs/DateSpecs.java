package com.github.athingx.athing.aliyun.thing.tsl.specs;

import com.github.athingx.athing.aliyun.thing.tsl.schema.TslDataType;

public class DateSpecs implements TslSpecs {


    @Override
    public TslDataType.Type getType() {
        return TslDataType.Type.DATE;
    }
}
