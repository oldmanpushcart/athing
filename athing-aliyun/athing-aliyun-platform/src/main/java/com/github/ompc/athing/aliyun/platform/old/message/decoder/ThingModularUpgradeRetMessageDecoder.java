package com.github.ompc.athing.aliyun.platform.old.message.decoder;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.platform.product.ThProductMeta;
import com.github.ompc.athing.aliyun.platform.message.decoder.DecodeException;
import com.github.ompc.athing.standard.platform.message.ThingMessage;
import com.github.ompc.athing.standard.platform.message.ThingModularUpgradeRetMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Map;

/**
 * 设备升级消息编解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-me3-ouz-xg8">固件升级状态通知</a>
 */
public class ThingModularUpgradeRetMessageDecoder extends MessageDecoder<JmsMessageDecoder.Header, JmsMessageDecoder.Header> {

    private final Gson gson = GsonFactory.getGson();
    private final Map<String, ThProductMeta> productMetaMap;

    public ThingModularUpgradeRetMessageDecoder(Map<String, ThProductMeta> productMetaMap) {
        this.productMetaMap = productMetaMap;
    }

    @Override
    protected boolean matches(JmsMessageDecoder.Header preHeader) {
        return preHeader.getMessageTopic().matches("/[^/]+/[^/]+/ota/upgrade");
    }

    @Override
    protected JmsMessageDecoder.Header decodeHeader(JmsMessageDecoder.Header preHeader, JsonObject payloadJsonObject) {
        return preHeader;
    }

    @Override
    protected ThingMessage decodePayload(JmsMessageDecoder.Header header, JsonObject payloadJsonObject) throws DecodeException {
        final ThingModuleUpgradeStatusData data = gson.fromJson(payloadJsonObject, ThingModuleUpgradeStatusData.class);
        if (!productMetaMap.containsKey(data.productKey)) {
            throw new DecodeException(String.format("product not defined, product=%s", data.productKey));
        }

        return new ThingModularUpgradeRetMessage(
                data.productKey,
                data.deviceName,
                data.messageCreateTime.getTime(),
                data.moduleName,
                data.srcVersion,
                data.destVersion,
                parseThingModuleUpgradeResult(data),
                data.desc
        );

    }

    private ThingModularUpgradeRetMessage.Result parseThingModuleUpgradeResult(ThingModuleUpgradeStatusData data) {
        switch (data.status.toUpperCase()) {
            case "SUCCEEDED":
                return ThingModularUpgradeRetMessage.Result.SUCCESS;
            case "FAILED":
                return ThingModularUpgradeRetMessage.Result.FAILURE;
            default:
                return ThingModularUpgradeRetMessage.Result.UN_KNOW;
        }
    }

    private static class ThingModuleUpgradeStatusData {

        @SerializedName("iotId")
        String iotId;

        @SerializedName("productKey")
        String productKey;

        @SerializedName("deviceName")
        String deviceName;

        @SerializedName("status")
        String status;

        @SerializedName("messageCreateTime")
        Date messageCreateTime;

        @SerializedName("srcVersion")
        String srcVersion;

        @SerializedName("destVersion")
        String destVersion;

        @SerializedName("desc")
        String desc;

        @SerializedName("jobId")
        String jobId;

        @SerializedName("taskId")
        String taskId;

        @SerializedName("moduleName")
        String moduleName;
    }

}
