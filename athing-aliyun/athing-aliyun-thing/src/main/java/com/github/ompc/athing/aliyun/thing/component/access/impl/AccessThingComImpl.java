package com.github.ompc.athing.aliyun.thing.component.access.impl;

import com.github.ompc.athing.aliyun.thing.ThingAccess;
import com.github.ompc.athing.aliyun.thing.component.access.Access;
import com.github.ompc.athing.aliyun.thing.component.access.AccessThingCom;

/**
 * 设备接入组件实现
 */
public class AccessThingComImpl implements AccessThingCom {

    private final ThingAccess access;

    public AccessThingComImpl(ThingAccess access) {
        this.access = access;
    }

    @Override
    public Access getAccess() {
        return new Access() {
            @Override
            public String getProductId() {
                return access.getProductId();
            }

            @Override
            public String getThingId() {
                return access.getThingId();
            }

            @Override
            public String getSecret() {
                return access.getSecret();
            }
        };
    }

}
