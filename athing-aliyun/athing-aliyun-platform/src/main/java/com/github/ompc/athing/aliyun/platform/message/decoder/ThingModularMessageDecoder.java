package com.github.ompc.athing.aliyun.platform.message.decoder;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.platform.message.ThingMessageDecoder;
import com.github.ompc.athing.aliyun.platform.util.EnumUtils;
import com.github.ompc.athing.standard.platform.message.ThingMessage;
import com.github.ompc.athing.standard.platform.message.ThingModularReportMessage;
import com.github.ompc.athing.standard.platform.message.ThingModularUpgradeMessage;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * 设备模块消息
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-me3-ouz-xg8">OTA升级状态通知</a>
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-mlz-0yp-7i7">OTA模块版本号上报</a>
 */
public class ThingModularMessageDecoder implements ThingMessageDecoder {

    private final Gson gson = GsonFactory.getGson();

    @Override
    public ThingMessage[] decode(String jmsTopic, String jmsMessageId, String jmsMessage) throws Exception {

        if (!jmsMessage.matches("^/[^/]+/[^/]+/ota/")) {
            return null;
        }

        // 解码设备模块升级消息
        if (jmsMessage.endsWith("/ota/upgrade")) {
            return new ThingMessage[]{decodeUpgradeMessage(jmsMessage)};
        }

        // 解码设备模块报告消息
        else if (jmsMessage.endsWith("/ota/version/post")) {
            return new ThingMessage[]{decodeReportMessage(jmsMessage)};
        }

        // 不属于本次解码范围
        return null;
    }

    /**
     * 解码设备模块升级消息
     *
     * @param jmsMessage JSON消息
     * @return 设备模块升级消息
     */
    private ThingModularUpgradeMessage decodeUpgradeMessage(String jmsMessage) {
        final Upgrade upgrade = gson.fromJson(jmsMessage, Upgrade.class);
        return new ThingModularUpgradeMessage(
                upgrade.productId,
                upgrade.thingId,
                upgrade.timestamp.getTime(),
                upgrade.moduleId,
                upgrade.srcVersion,
                upgrade.destVersion,
                EnumUtils.valueOf(upgrade.status.toUpperCase(), ThingModularUpgradeMessage.Result.class),
                upgrade.desc
        );
    }

    /**
     * 解码设备模块报告消息
     *
     * @param jmsMessage JSON消息
     * @return 设备模块报告消息
     */
    private ThingModularReportMessage decodeReportMessage(String jmsMessage) {
        final Report report = gson.fromJson(jmsMessage, Report.class);
        return new ThingModularReportMessage(
                report.productId,
                report.thingId,
                report.timestamp.getTime(),
                report.moduleId,
                report.version
        );
    }

    /**
     * 升级
     */
    private static class Upgrade {

        @SerializedName("productKey")
        String productId;

        @SerializedName("deviceName")
        String thingId;

        @SerializedName("status")
        String status;

        @SerializedName("messageCreateTime")
        Date timestamp;

        @SerializedName("srcVersion")
        String srcVersion;

        @SerializedName("destVersion")
        String destVersion;

        @SerializedName("desc")
        String desc;

        @SerializedName("moduleName")
        String moduleId;
    }

    /**
     * 报告
     */
    private static class Report {

        @SerializedName("productKey")
        String productId;

        @SerializedName("deviceName")
        String thingId;

        @SerializedName("messageCreateTime")
        Date timestamp;

        @SerializedName("moduleName")
        String moduleId;

        @SerializedName("moduleVersion")
        String version;

    }

}
