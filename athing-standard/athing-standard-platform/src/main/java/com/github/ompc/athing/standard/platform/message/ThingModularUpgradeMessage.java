package com.github.ompc.athing.standard.platform.message;

import static com.github.ompc.athing.standard.platform.message.ThingMessage.Type.THING_MODULAR_UPGRADE;

/**
 * 设备模块组件升级结果消息
 */
public class ThingModularUpgradeMessage extends ThingMessage {

    private final String moduleId;
    private final String srcVersion;
    private final String dstVersion;
    private final Result result;
    private final String desc;

    /**
     * 设备模块升级结果消息
     *
     * @param productId  产品ID
     * @param thingId    设备ID
     * @param timestamp  消息时间戳
     * @param moduleId   模块ID
     * @param srcVersion 源版本
     * @param dstVersion 目标版本
     * @param result     升级结果
     * @param desc       结果描述
     */
    public ThingModularUpgradeMessage(
            String productId, String thingId, long timestamp,
            String moduleId, String srcVersion, String dstVersion, Result result, String desc
    ) {
        super(THING_MODULAR_UPGRADE, productId, thingId, timestamp);
        this.moduleId = moduleId;
        this.srcVersion = srcVersion;
        this.dstVersion = dstVersion;
        this.result = result;
        this.desc = desc;
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
     * 获取源版本
     *
     * @return 源版本
     */
    public String getSrcVersion() {
        return srcVersion;
    }

    /**
     * 获取目标版本
     *
     * @return 目标版本
     */
    public String getDstVersion() {
        return dstVersion;
    }

    /**
     * 获取升级结果
     *
     * @return 升级结果
     */
    public Result getResult() {
        return result;
    }

    /**
     * 获取结果描述
     *
     * @return 结果描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 设备模块升级状态
     */
    public enum Result {

        /**
         * 升级成功
         */
        SUCCESS,

        /**
         * 升级失败
         */
        FAILURE,

        /**
         * 升级取消
         */
        CANCELED

    }
}
