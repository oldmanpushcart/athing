package com.github.athingx.athing.aliyun.thing.tsl.schema;

import com.google.gson.annotations.SerializedName;

public class TslFunctionBlock {

    @SerializedName("functionBlockId")
    private final String componentId;

    @SerializedName("functionBlockName")
    private final String name;

    @SerializedName("description")
    private final String desc;

    @SerializedName("productKey")
    private final String productId;

    public TslFunctionBlock(String componentId, String name, String desc, String productId) {
        this.componentId = componentId;
        this.name = name;
        this.desc = desc;
        this.productId = productId;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getProductId() {
        return productId;
    }
}
