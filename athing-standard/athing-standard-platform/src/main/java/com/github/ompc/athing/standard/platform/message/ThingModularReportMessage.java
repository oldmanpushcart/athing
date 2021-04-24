package com.github.ompc.athing.standard.platform.message;

public class ThingModularReportMessage extends ThingMessage {

    private final String moduleId;
    private final String version;

    /**
     * 设备消息
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param timestamp 消息时间戳
     * @param moduleId  模块ID
     * @param version   模块版本
     */
    public ThingModularReportMessage(String productId, String thingId, long timestamp,
                                        String moduleId, String version) {
        super(Type.THING_MODULAR_REPORT, productId, thingId, timestamp);
        this.moduleId = moduleId;
        this.version = version;
    }

    /**
     * 获取模块ID
     *
     * @return 模块ID
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * 获取模块版本
     *
     * @return 模块版本
     */
    public String getVersion() {
        return version;
    }
}
