package com.github.athingx.athing.aliyun.thing.tsl.schema;

import com.google.gson.annotations.SerializedName;

public class TslComSchema extends TslSchema {

    @SerializedName("functionBlockId")
    private final String componentId;

    @SerializedName("functionBlockName")
    private final String componentName;

    private transient final TslFunctionBlock functionBlock;

    public TslComSchema(TslFunctionBlock block) {
        super(new TslProfile(block.getProductId(), TslProfile.SUB_TSL_PROFILE_VERSION));
        this.componentId = block.getComponentId();
        this.componentName = block.getName();
        this.functionBlock = block;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public TslFunctionBlock getFunctionBlock() {
        return functionBlock;
    }

}
