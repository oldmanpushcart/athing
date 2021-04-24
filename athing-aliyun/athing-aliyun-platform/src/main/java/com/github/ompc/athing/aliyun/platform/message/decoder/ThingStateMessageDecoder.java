package com.github.ompc.athing.aliyun.platform.message.decoder;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.platform.message.ThingMessageDecoder;
import com.github.ompc.athing.aliyun.platform.util.EnumUtils;
import com.github.ompc.athing.standard.platform.message.ThingMessage;
import com.github.ompc.athing.standard.platform.message.ThingStateMessage;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 设备状态消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-2ll-4j3-1wx">设备上下线状态</a>
 */
public class ThingStateMessageDecoder implements ThingMessageDecoder {

    private final Gson gson = GsonFactory.getGson();

    @Override
    public ThingMessage[] decode(String jmsTopic, String jmsMessageId, String jmsMessage) throws Exception {

        if (!jmsTopic.matches("/as/mqtt/status/[^/]+/[^/]+")) {
            return null;
        }

        final Data data = gson.fromJson(jmsMessage, Data.class);
        final SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            final long utcOccurTimestamp = utcDateFormat.parse(data.utcTime).getTime();
            final long utcLastTimestamp = utcDateFormat.parse(data.utcLastTime).getTime();
            return new ThingMessage[]{
                    new ThingStateMessage(
                            data.productId,
                            data.thingId,
                            utcOccurTimestamp,
                            EnumUtils.valueOf(data.status.toUpperCase(), ThingStateMessage.State.class),
                            utcLastTimestamp,
                            data.clientIp
                    )
            };
        } catch (ParseException cause) {
            throw new DecodeException(String.format("illegal utc format, occur=%s;last=%s;", data.utcTime, data.utcLastTime), cause);
        }
    }


    /**
     * 数据
     */
    private static class Data {

        @SerializedName("status")
        String status;

        @SerializedName("productKey")
        String productId;

        @SerializedName("deviceName")
        String thingId;

        @SerializedName("time")
        String time;

        @SerializedName("utcTime")
        String utcTime;

        @SerializedName("lastTime")
        String lastTime;

        @SerializedName("utcLastTime")
        String utcLastTime;

        @SerializedName("clientIp")
        String clientIp;

    }

}
