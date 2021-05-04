package com.github.ompc.athing.aliyun.thing.runtime.messenger;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;

public class JsonSerializerImpl implements JsonSerializer {

    public static final JsonSerializer serializer = new JsonSerializerImpl();

    @Override
    public String toJson(Object object) {
        return GsonFactory.getGson().toJson(object);
    }


}
