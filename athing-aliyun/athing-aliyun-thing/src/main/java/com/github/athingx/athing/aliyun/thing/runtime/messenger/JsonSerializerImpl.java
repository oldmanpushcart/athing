package com.github.athingx.athing.aliyun.thing.runtime.messenger;

import com.github.athingx.athing.aliyun.framework.util.GsonFactory;

public class JsonSerializerImpl implements JsonSerializer {

    public static final JsonSerializer serializer = new JsonSerializerImpl();

    @Override
    public String toJson(Object object) {
        return GsonFactory.getGson().toJson(object);
    }


}
