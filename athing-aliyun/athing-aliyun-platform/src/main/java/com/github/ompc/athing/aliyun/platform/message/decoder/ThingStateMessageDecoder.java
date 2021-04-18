package com.github.ompc.athing.aliyun.platform.message.decoder;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.platform.component.message.decoder.ThingMessageDecoder;
import com.github.ompc.athing.standard.platform.message.ThingMessage;
import com.github.ompc.athing.standard.platform.message.ThingStateChangedMessage;
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
    public ThingMessage decode(String jmsTopic, String jmsMessageId, String jmsMessage) throws Exception {

        if (!jmsTopic.matches("/as/mqtt/status/[^/]+/[^/]+")) {
            return null;
        }

        final ThingStateChanged thingStateChanged = gson.fromJson(jmsMessage, ThingStateChanged.class);
        final SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            final long utcOccurTimestamp = utcDateFormat.parse(thingStateChanged.utcTime).getTime();
            final long utcLastTimestamp = utcDateFormat.parse(thingStateChanged.utcLastTime).getTime();
            return new ThingStateChangedMessage(
                    thingStateChanged.productId,
                    thingStateChanged.thingId,
                    utcOccurTimestamp,
                    parseThingStateChangedEventState(thingStateChanged),
                    utcLastTimestamp,
                    thingStateChanged.clientIp
            );
        } catch (ParseException cause) {
            throw new DecodeException(String.format("illegal utc format, occur=%s;last=%s;", thingStateChanged.utcTime, thingStateChanged.utcLastTime), cause);
        }
    }

    private ThingStateChangedMessage.State parseThingStateChangedEventState(ThingStateChanged changed) {
        switch (changed.status.toUpperCase()) {
            case "ONLINE":
                return ThingStateChangedMessage.State.ONLINE;
            case "OFFLINE":
                return ThingStateChangedMessage.State.OFFLINE;
            default:
                return ThingStateChangedMessage.State.UN_KNOW;
        }
    }


    private static class ThingStateChanged {

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
