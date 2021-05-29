package com.github.athingx.athing.aliyun.thing.tsl.schema;

import com.google.gson.annotations.SerializedName;

public class TslPropertyElement extends TslElement {

    @SerializedName("accessMode")
    private final String accessMode;

    @SerializedName("dataType")
    private final TslDataType dataType;

    public TslPropertyElement(String identifier, boolean isReadOnly, TslDataType dataType) {
        super(identifier);
        this.accessMode = isReadOnly ? "r" : "rw";
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return "PROPERTY:" + getIdentifier();
    }

    public String getAccessMode() {
        return accessMode;
    }

    public TslDataType getDataType() {
        return dataType;
    }
}
