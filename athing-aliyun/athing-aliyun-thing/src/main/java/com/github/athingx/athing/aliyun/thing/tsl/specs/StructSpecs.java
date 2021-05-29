package com.github.athingx.athing.aliyun.thing.tsl.specs;

import com.github.athingx.athing.aliyun.thing.tsl.schema.TslData;
import com.github.athingx.athing.aliyun.thing.tsl.schema.TslDataType;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class StructSpecs extends LinkedList<TslData> implements TslSpecs {

    public StructSpecs(TslData... dataArray) {
        super(Arrays.asList(dataArray));
    }

    public StructSpecs(Collection<TslData> data) {
        super(data);
    }


    @Override
    public TslDataType.Type getType() {
        return TslDataType.Type.STRUCT;
    }
}
