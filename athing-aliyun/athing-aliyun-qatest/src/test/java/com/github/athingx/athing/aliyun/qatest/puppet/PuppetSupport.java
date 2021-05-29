package com.github.athingx.athing.aliyun.qatest.puppet;

import com.github.athingx.athing.aliyun.component.monitor.api.MonitorThingCom;
import com.github.athingx.athing.aliyun.platform.ThingMessageConsumerBuilder;
import com.github.athingx.athing.aliyun.platform.ThingPlatformAccess;
import com.github.athingx.athing.aliyun.platform.ThingPlatformBuilder;
import com.github.athingx.athing.aliyun.qatest.message.QaThingMessageGroupListener;
import com.github.athingx.athing.aliyun.qatest.message.QaThingModularUpgradeMessageListener;
import com.github.athingx.athing.aliyun.qatest.message.QaThingPostMessageListener;
import com.github.athingx.athing.aliyun.qatest.message.QaThingReplyMessageListener;
import com.github.athingx.athing.aliyun.qatest.puppet.component.EchoThingCom;
import com.github.athingx.athing.aliyun.qatest.puppet.component.LightThingCom;
import com.github.athingx.athing.aliyun.qatest.puppet.component.impl.QaThingComImpl;
import com.github.athingx.athing.aliyun.thing.ThingBoot;
import com.github.athingx.athing.aliyun.thing.runtime.access.ThingAccess;
import com.github.athingx.athing.aliyun.thing.runtime.access.ThingAccessImpl;
import com.github.athingx.athing.standard.platform.ThingPlatform;
import com.github.athingx.athing.standard.platform.ThingPlatformException;
import com.github.athingx.athing.standard.platform.message.ThingMessageListener;
import com.github.athingx.athing.standard.platform.message.ThingModularUpgradeMessage;
import com.github.athingx.athing.standard.platform.message.ThingPostMessage;
import com.github.athingx.athing.standard.platform.message.ThingReplyMessage;
import com.github.athingx.athing.standard.thing.Thing;
import com.github.athingx.athing.standard.thing.boot.BootArguments;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import static java.lang.String.format;

/**
 * Puppet设备支撑
 */
public class PuppetSupport {

    // 基础常量
    protected static final Properties properties = loadingProperties(new Properties());
    protected static final String PRODUCT_ID = $("athing.product.id");
    protected static final String THING_ID = $("athing.thing.id");

    private static final ThingAccess THING_ACCESS = new ThingAccessImpl(
            $("athing.product.id"),
            $("athing.thing.id"),
            $("athing.thing.secret")
    );

    private static final ThingPlatformAccess THING_PLATFORM_ACCESS = new ThingPlatformAccess(
            $("athing-platform.access.id"),
            $("athing-platform.access.secret")
    );

    protected static final QaThingReplyMessageListener qaThingReplyMessageListener = new QaThingReplyMessageListener();
    protected static final QaThingPostMessageListener qaThingPostMessageListener = new QaThingPostMessageListener();
    protected static final QaThingModularUpgradeMessageListener qaThingModularUpgradeMessageListener = new QaThingModularUpgradeMessageListener();

    // 基础变量
    protected static Thing tPuppet;
    protected static ThingPlatform tpPuppet;

    /**
     * 初始化配置文件
     *
     * @param properties 配置信息
     * @return 配置信息
     */
    private static Properties loadingProperties(Properties properties) {

        // 读取配置文件
        final File file = new File(System.getProperties().getProperty("athing-qatest.properties.file"));

        // 检查文件是否存在
        if (!file.exists()) {
            throw new RuntimeException(format("properties file: %s not existed!", file.getAbsolutePath()));
        }

        // 检查文件是否可读
        if (!file.canRead()) {
            throw new RuntimeException(format("properties file: %s can not read!", file.getAbsolutePath()));
        }

        // 加载配置文件
        try (final InputStream is = new FileInputStream(file)) {
            properties.load(is);
            return properties;
        } catch (Exception cause) {
            throw new RuntimeException(format("properties file: %s load error!", file.getAbsoluteFile()), cause);
        }
    }

    private static String $(String name) {
        return properties.getProperty(name);
    }

    @BeforeClass
    public static void initialization() throws Exception {
        tPuppet = initPuppetThing();
        tpPuppet = initPuppetThingPlatform();
    }

    @AfterClass
    public static void destroy() throws Exception {
        tPuppet.destroy();
        tpPuppet.destroy();
    }


    // ------------------------------------- THING ------------------------------------

    private static void reconnect(Thing thing) {
        if (!thing.isDestroyed()) {
            thing.getThingOp().connect()
                    .onFailure(connF -> reconnect(thing))
                    .onSuccess(connF -> connF.getSuccess().getDisconnectFuture().onDone(disconnectF -> reconnect(thing)));
        }
    }

    private static Thing initPuppetThing() throws Exception {
        final Thing thing = new ThingBoot(new URI($("athing.thing.server-url")), THING_ACCESS)
                .load(new File("./src/test/resources/lib/monitor-boot-1.0.0-SNAPSHOT-jar-with-dependencies.jar"))
                .load(new File("./src/test/resources/lib/config-boot-1.0.0-SNAPSHOT-jar-with-dependencies.jar"))
                .load(new File("./src/test/resources/lib/modular-boot-1.0.0-SNAPSHOT-jar-with-dependencies.jar"))
                .load(new File("./src/test/resources/lib/tunnel-boot-1.0.0-SNAPSHOT-jar-with-dependencies.jar"),
                        (productId, thingId, boot) ->
                                boot.bootUp(
                                        productId,
                                        thingId,
                                        BootArguments.parse("service=ssh_localhost&ssh_localhost_type=SSH&ssh_localhost_ip=127.0.0.1&ssh_localhost_port=22")
                                )
                )
                .load(new QaThingComImpl())
                .boot();
        reconnect(thing);
        return thing;
    }

    private static ThingPlatform initPuppetThingPlatform() throws ThingPlatformException {
        return new ThingPlatformBuilder()
                .building("cn-shanghai", THING_PLATFORM_ACCESS)
                .product(PRODUCT_ID, MonitorThingCom.class, LightThingCom.class, EchoThingCom.class)
                .consumer(new ThingMessageConsumerBuilder()
                        .access(THING_PLATFORM_ACCESS)
                        .connection($("athing-platform.jms.connection-url"))
                        .group($("athing-platform.jms.group"))
                        .listener(new QaThingMessageGroupListener(new ThingMessageListener[]{
                                qaThingReplyMessageListener,
                                qaThingPostMessageListener,
                                qaThingModularUpgradeMessageListener
                        }))
                )
                .build();
    }

    public <T extends ThingReplyMessage> T waitingForReplyMessageByToken(String token) throws InterruptedException {
        return qaThingReplyMessageListener.waitingForReplyMessageByToken(token);
    }

    public <T extends ThingPostMessage> T waitingForPostMessageByToken(String token) throws InterruptedException {
        return qaThingPostMessageListener.waitingForPostMessageByToken(token);
    }

    public ThingModularUpgradeMessage waitingForThingModularUpgradeMessageByModuleId(String moduleId) throws InterruptedException {
        return qaThingModularUpgradeMessageListener.waitingForThingModularUpgradeMessageByModuleId(moduleId);
    }

}
