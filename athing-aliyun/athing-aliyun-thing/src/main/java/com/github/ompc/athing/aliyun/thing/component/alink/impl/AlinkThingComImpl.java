package com.github.ompc.athing.aliyun.thing.component.alink.impl;

import com.github.ompc.athing.aliyun.thing.component.alink.Alink;
import com.github.ompc.athing.aliyun.thing.component.alink.AlinkThingCom;
import com.github.ompc.athing.aliyun.thing.component.alink.Reply;
import com.github.ompc.athing.aliyun.thing.util.StringUtils;

/**
 * alink组件
 */
public class AlinkThingComImpl implements AlinkThingCom {


    @Override
    public Alink getAlink() {
        return new Alink() {

            @Override
            public String generateToken() {
                return StringUtils.generateToken();
            }

            @Override
            public Reply<Void> successReply(String token) {
                return new Reply<>(token, Reply.REPLY_OK, "success", null);
            }

            @Override
            public Reply<Void> failureReply(String token, int code, String message) {
                return new Reply<>(token, code, message, null);
            }

            @Override
            public <T> Reply<T> successReply(String token, T data) {
                return new Reply<>(token, Reply.REPLY_OK, "success", data);
            }

        };
    }
}
