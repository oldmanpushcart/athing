package com.github.athingx.athing.standard.thing;

/**
 * 设备异常
 */
public class ThingException extends Exception {

    private final String productId;
    private final String thingId;

    /**
     * 设备异常
     *
     * @param thing   设备
     * @param message 错误信息
     * @param cause   错误异常
     */
    public ThingException(Thing thing, String message, Throwable cause) {
        this(thing.getProductId(), thing.getThingId(), message, cause);
    }

    /**
     * 设备异常
     *
     * @param thing   设备
     * @param message 错误信息
     */
    public ThingException(Thing thing, String message) {
        this(thing.getProductId(), thing.getThingId(), message);
    }

    /**
     * 设备异常
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param message   错误信息
     * @param cause     错误异常
     */
    public ThingException(String productId, String thingId, String message, Throwable cause) {
        super(message, cause);
        this.productId = productId;
        this.thingId = thingId;
    }

    /**
     * 设备异常
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param message   错误信息
     */
    public ThingException(String productId, String thingId, String message) {
        super(message);
        this.productId = productId;
        this.thingId = thingId;
    }

    /**
     * 获取产品ID
     *
     * @return 产品ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    public String getThingId() {
        return thingId;
    }


    @Override
    public String getLocalizedMessage() {
        return String.format("thing:/%s/%s occur error: %s", getProductId(), getThingId(), getMessage());
    }

}
