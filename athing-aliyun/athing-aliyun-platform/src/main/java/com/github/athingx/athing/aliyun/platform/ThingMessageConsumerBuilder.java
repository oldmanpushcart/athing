package com.github.athingx.athing.aliyun.platform;

import com.github.athingx.athing.aliyun.framework.Constants;
import com.github.athingx.athing.aliyun.platform.message.ThingMessageDecoder;
import com.github.athingx.athing.aliyun.platform.product.ThProductMeta;
import com.github.athingx.athing.standard.platform.ThingPlatformException;
import com.github.athingx.athing.standard.platform.message.ThingMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 设备消息消费者构造器
 */
public class ThingMessageConsumerBuilder {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ThingPlatformAccess access;
    private String connection;
    private String group;
    private final Set<ThingMessageDecoder> decoders = new LinkedHashSet<>();
    private ThingMessageListener listener;

    /**
     * 设置消息服务接入
     *
     * @param access 接入
     * @return this
     */
    public ThingMessageConsumerBuilder access(ThingPlatformAccess access) {
        this.access = access;
        return this;
    }

    /**
     * 设置消息服务连接
     *
     * @param connection 服务连接
     * @return this
     */
    public ThingMessageConsumerBuilder connection(String connection) {
        this.connection = connection;
        return this;
    }

    /**
     * 设置消息服务分组
     *
     * @param group 分组
     * @return this
     */
    public ThingMessageConsumerBuilder group(String group) {
        this.group = group;
        return this;
    }

    /**
     * 设置消息解码器
     *
     * @param decoders 解码器
     * @return this
     */
    public ThingMessageConsumerBuilder decoders(ThingMessageDecoder... decoders) {
        this.decoders.addAll(Arrays.asList(decoders));
        return this;
    }

    /**
     * 设置消息监听器
     *
     * @param listener 监听器
     * @return this
     */
    public ThingMessageConsumerBuilder listener(ThingMessageListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 构造消息消费者
     *
     * @param productMetaMap 设备产品元数据集合
     * @return 消息消费者
     * @throws ThingPlatformException 构造失败
     */
    ThingMessageConsumer build(Map<String, ThProductMeta> productMetaMap) throws ThingPlatformException {
        final ThingMessageConsumer consumer = ThingMessageConsumer.createThingMessageConsumer(
                Objects.requireNonNull(access, "access"),
                Objects.requireNonNull(connection, "connection"),
                Objects.requireNonNull(group, "group"),
                Objects.requireNonNull(productMetaMap, "product meta map"),
                decoders,
                Objects.requireNonNull(listener, "listener")
        );
        logger.info("thing-platform:/{}/consumer is connected, connection={}", Constants.THING_PLATFORM_CODE, connection);
        return consumer;
    }

}
