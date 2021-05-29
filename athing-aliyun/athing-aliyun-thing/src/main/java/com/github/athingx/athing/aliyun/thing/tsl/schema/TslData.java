package com.github.athingx.athing.aliyun.thing.tsl.schema;

import com.google.gson.annotations.SerializedName;

public class TslData extends TslIdentifier {

    @SerializedName("dataType")
    private final TslDataType dataType;

    public TslData(String identifier, TslDataType dataType) {
        super(identifier);
        this.dataType = dataType;
    }

    public TslDataType getDataType() {
        return dataType;
    }

    @Override
    public String toString() {
        return getDataType().getType() + ":" + getIdentifier();
    }
}
