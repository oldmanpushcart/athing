package com.github.athingx.athing.aliyun.thing.tsl.specs;

import com.github.athingx.athing.aliyun.thing.tsl.schema.TslDataType;

public class DoubleSpecs implements TslSpecs {

    private final double min;
    private final double max;
    private final double step;

    public DoubleSpecs() {
        this(-1 * Double.MIN_VALUE, Double.MAX_VALUE, 0.01d);
    }

    public DoubleSpecs(double min, double max, double step) {
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    @Override
    public TslDataType.Type getType() {
        return TslDataType.Type.DOUBLE;
    }
}
