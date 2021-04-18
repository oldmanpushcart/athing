package com.github.ompc.athing.aliyun.platform;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.github.ompc.athing.aliyun.framework.Constants;
import com.github.ompc.athing.aliyun.framework.component.meta.ThComMeta;
import com.github.ompc.athing.aliyun.framework.component.meta.ThComMetaFactory;
import com.github.ompc.athing.aliyun.framework.util.IOUtils;
import com.github.ompc.athing.aliyun.platform.product.ThProductMeta;
import com.github.ompc.athing.aliyun.platform.product.ThProductStub;
import com.github.ompc.athing.standard.platform.ThingPlatform;
import com.github.ompc.athing.standard.platform.ThingPlatformException;
import com.github.ompc.athing.standard.platform.message.ThingMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.aliyuncs.profile.DefaultProfile.getProfile;

/**
 * 设备平台构造器
 */
public class ThingPlatformBuilder {

    private final static Logger logger = LoggerFactory.getLogger(ThingPlatformBuilder.class);

    static {
        logger.info(IOUtils.getLogo("athing-platform-logo.txt"));
    }

    /**
     * 构建设备平台:构建产品
     *
     * @param regionId 区域ID
     * @param access   设备平台访问密钥
     * @return Building.this
     */
    public Building building(String regionId, ThingPlatformAccess access) {

        final IAcsClient client = new DefaultAcsClient(getProfile(
                regionId,
                access.getIdentity(),
                access.getSecret())
        );

        return new Building() {

            private final Building _this = this;
            private final Map<String, ThProductStub> thProductStubMap = new HashMap<>();
            private ThingMessageConsumer consumer;

            @Override
            public Building product(String productId, Class<?>... thingComInterfaces) {
                final ThProductStub thProductStub = new ThProductStub(client, productId,
                        new ThProductMeta(
                                Stream.of(thingComInterfaces)
                                        .map(ThComMetaFactory::make)
                                        .toArray(ThComMeta[]::new)
                        ));
                thProductStubMap.put(productId, thProductStub);
                logger.info("thing-platform:/{} building product:/{} has components={}",
                        Constants.THING_PLATFORM_CODE,
                        productId,
                        thProductStub.getThProductMeta().getThComMetaMap().keySet()
                );
                return this;
            }

            @Override
            public Build consumer(ThingMessageConsumerBuilder builder) {
                return () -> {
                    consumer = builder.build(thProductStubMap.values().stream().collect(Collectors.toMap(
                            ThProductStub::getProductId,
                            ThProductStub::getThProductMeta,
                            (a, b) -> a
                    )));
                    return _this.build();
                };
            }

            @Override
            public ThingPlatform build() {
                final ThingPlatform thingPlatform = new ThingPlatformImpl(thProductStubMap, client, consumer);
                logger.info("{} build completed, products={};consumer={};",
                        thingPlatform,
                        thProductStubMap.keySet(),
                        consumer
                );
                return thingPlatform;
            }

        };
    }

    public interface Building extends Build {

        /**
         * 设备平台构建：构建设备产品
         *
         * @param productId          产品ID
         * @param thingComInterfaces 产品组件接口集合
         * @return BuildingForThingProduct.this
         */
        Building product(String productId, Class<?>... thingComInterfaces);

        /**
         * 设备平台构建：构建设备消息消费
         *
         * @return IBuildingForThingPlatform.this
         */
        Build consumer(ThingMessageConsumerBuilder builder);

    }

    /**
     * 设备平台构建
     */
    public interface Build {

        /**
         * 构建设备平台
         *
         * @return 设备平台
         * @throws ThingPlatformException 构建失败
         */
        ThingPlatform build() throws ThingPlatformException;

    }

}
