package com.github.athingx.athing.aliyun.platform.message.decoder;

import com.github.athingx.athing.aliyun.framework.util.GsonFactory;
import com.github.athingx.athing.aliyun.platform.message.ThingMessageDecoder;
import com.github.athingx.athing.aliyun.platform.util.EnumUtils;
import com.github.athingx.athing.standard.platform.message.ThingLifeCycleMessage;
import com.github.athingx.athing.standard.platform.message.ThingMessage;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * 设备生命周期消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-0im-t30-d4l">设备生命周期变更</a>
 */
public class ThingLifeCycleMessageDecoder implements ThingMessageDecoder {

    private final Gson gson = GsonFactory.getGson();

    @Override
    public ThingMessage[] decode(String jmsTopic, String jmsMessageId, String jmsMessage) throws Exception {

        if (!jmsTopic.matches("^/[^/]+/[^/]+/thing/lifecycle")) {
            return null;
        }

        final Data data = gson.fromJson(jmsMessage, Data.class);
        return new ThingMessage[]{
                new ThingLifeCycleMessage(
                        data.productId,
                        data.thingId,
                        data.timestamp.getTime(),
                        EnumUtils.valueOf(data.action.toUpperCase(), ThingLifeCycleMessage.LifeCycle.class)
                )
        };
    }

    /**
     * 数据
     */
    private static class Data {

        @SerializedName("productKey")
        String productId;

        @SerializedName("deviceName")
        String thingId;

        @SerializedName("messageCreateTime")
        Date timestamp;

        @SerializedName("action")
        String action;

    }

}
