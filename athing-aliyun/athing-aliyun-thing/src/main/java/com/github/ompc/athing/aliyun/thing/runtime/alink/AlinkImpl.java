package com.github.ompc.athing.aliyun.thing.runtime.alink;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.thing.util.StringUtils;
import com.github.ompc.athing.standard.thing.ThingReply;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class AlinkImpl implements Alink {

    private final Gson gson = GsonFactory.getGson();

    @Override
    public String generateToken() {
        return StringUtils.generateToken();
    }

    @Override
    public <T> T deserialize(String json, Type typeOf) {
        return gson.fromJson(json, typeOf);
    }

    @Override
    public <T> T deserialize(String json, Class<T> classOf) {
        return gson.fromJson(json, classOf);
    }

    @Override
    public <T> ThingReply<T> deserializeReply(String json) {
        return gson.fromJson(json, new TypeToken<ThingReplyImpl<T>>() {
        }.getType());
    }

    @Override
    public <T> T deserialize(String json) {
        return gson.fromJson(json, new TypeToken<T>() {
        }.getType());
    }

    @Override
    public String serialize(Object target) {
        return gson.toJson(target);
    }

    @Override
    public <T> ThingReply<T> successReply(String token) {
        return new ThingReplyImpl<>(token, ALINK_REPLY_OK, "success", null);
    }

    @Override
    public <T> ThingReply<T> successReply(String token, T data) {
        return new ThingReplyImpl<>(token, ALINK_REPLY_OK, "success", data);
    }

    @Override
    public <T> ThingReply<T> failureReply(String token, int code, String message) {
        return new ThingReplyImpl<>(token, code, message, null);
    }
}
