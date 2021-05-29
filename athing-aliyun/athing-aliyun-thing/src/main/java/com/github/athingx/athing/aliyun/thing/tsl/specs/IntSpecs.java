package com.github.athingx.athing.aliyun.thing.tsl.specs;

import com.github.athingx.athing.aliyun.thing.tsl.schema.TslDataType;

public class IntSpecs implements TslSpecs {

    private final int min;
    private final int max;
    private final int step;

    public IntSpecs(int min, int max, int step) {
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public IntSpecs() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

    @Override
    public TslDataType.Type getType() {
        return TslDataType.Type.INT;
    }
}
