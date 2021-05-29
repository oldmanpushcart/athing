package com.github.athingx.athing.aliyun.thing.tsl.specs;

import com.github.athingx.athing.aliyun.thing.tsl.schema.TslDataType;
import com.google.gson.annotations.SerializedName;

public class BoolSpecs implements TslSpecs {

    @SerializedName("1")
    private final String descT;

    @SerializedName("0")
    private final String descF;

    public BoolSpecs(String descT, String descF) {
        this.descT = descT;
        this.descF = descF;
    }

    public BoolSpecs() {
        this("true", "false");
    }

    public String getDescT() {
        return descT;
    }

    public String getDescF() {
        return descF;
    }

    @Override
    public TslDataType.Type getType() {
        return TslDataType.Type.BOOL;
    }
}
