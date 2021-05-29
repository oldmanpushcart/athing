package com.github.athingx.athing.aliyun.thing.tsl.schema;

import com.github.athingx.athing.aliyun.framework.component.meta.ThComMeta;

import java.util.ArrayList;
import java.util.List;

public class TslMainSchema extends TslSchema {

    private final List<TslFunctionBlock> functionBlocks = new ArrayList<>();

    public TslMainSchema(String productId) {
        super(new TslProfile(productId, TslProfile.MAIN_TSL_PROFILE_VERSION));
    }

    public List<TslFunctionBlock> getFunctionBlocks() {
        return functionBlocks;
    }

    public TslComSchema newComSchema(ThComMeta meta) {
        final TslComSchema schema = new TslComSchema(new TslFunctionBlock(
                meta.getThingComId(),
                meta.getThingComName(),
                meta.getThingComDesc(),
                getProfile().getProductId()
        ));
        functionBlocks.add(schema.getFunctionBlock());
        return schema;
    }

}
