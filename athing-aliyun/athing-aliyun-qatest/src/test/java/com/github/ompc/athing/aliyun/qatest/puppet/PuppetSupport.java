package com.github.ompc.athing.aliyun.qatest.puppet;

import com.github.ompc.athing.aliyun.platform.ThingPlatformAccess;
import com.github.ompc.athing.aliyun.platform.ThingPlatformBuilder;
import com.github.ompc.athing.aliyun.qatest.QaThingConfigListener;
import com.github.ompc.athing.aliyun.qatest.message.QaThingMessageGroupListener;
import com.github.ompc.athing.aliyun.qatest.message.QaThingPostMessageListener;
import com.github.ompc.athing.aliyun.qatest.message.QaThingReplyMessageListener;
import com.github.ompc.athing.aliyun.qatest.puppet.component.EchoThingCom;
import com.github.ompc.athing.aliyun.qatest.puppet.component.LightThingCom;
import com.github.ompc.athing.aliyun.qatest.puppet.component.impl.QaThingComImpl;
import com.github.ompc.athing.aliyun.qatest.puppet.component.impl.ResourceThingComImpl;
import com.github.ompc.athing.aliyun.thing.ThingAccess;
import com.github.ompc.athing.aliyun.thing.ThingConnectOption;
import com.github.ompc.athing.aliyun.thing.ThingConnector;
import com.github.ompc.athing.component.dmgr.api.DmgrThingCom;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.platform.ThingPlatform;
import com.github.ompc.athing.standard.platform.ThingPlatformException;
import com.github.ompc.athing.standard.platform.message.ThingMessageListener;
import com.github.ompc.athing.standard.platform.message.ThingPostMessage;
import com.github.ompc.athing.standard.platform.message.ThingReplyMessage;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.config.ThingConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static com.github.ompc.athing.aliyun.framework.Constants.DEFAULT_REGION_ID;

/**
 * Puppet设备支撑
 */
public class PuppetSupport {

    // 基础常量
    protected static final Properties properties = getProperties(new Properties());
    protected static final String PRODUCT_ID =
            properties.getProperty("athing.product.id");
    protected static final String THING_ID =
            properties.getProperty("athing.thing.id");
    protected static final String THING_SERVER_URL =
            properties.getProperty("athing.thing.server-url");
    protected static final ThingAccess THING_ACCESS = new ThingAccess(
            PRODUCT_ID,
            THING_ID,
            properties.getProperty("athing.thing.secret")
    );
    protected static final ThingPlatformAccess PLATFORM_ACCESS = new ThingPlatformAccess(
            properties.getProperty("athing-platform.access.id"),
            properties.getProperty("athing-platform.access.secret")
    );
    protected static final String PLATFORM_JMS_CONSUMER_GROUP =
            properties.getProperty("athing-platform.jms.group");
    protected static final String PLATFORM_JMS_CONNECTION_URL =
            properties.getProperty("athing-platform.jms.connection-url");
    protected static final QaThingReplyMessageListener qaThingReplyMessageListener = new QaThingReplyMessageListener();
    protected static final QaThingPostMessageListener qaThingPostMessageListener = new QaThingPostMessageListener();
    protected static final QaThingConfigListener qaThingConfigListener = new QaThingConfigListener();
    private static final Logger logger = LoggerFactory.getLogger(PuppetSupport.class);
    // 基础变量
    protected static Thing tPuppet;
    protected static ThingPlatform tpPuppet;

    /**
     * 初始化配置文件
     *
     * @param properties 配置信息
     * @return 配置信息
     */
    private static Properties getProperties(Properties properties) {
        // 读取配置文件
        final String propertiesFilePath = System.getProperties().getProperty("athing-qatest.properties.file");
        final File propertiesFile = new File(propertiesFilePath);
        if (!propertiesFile.exists() || !propertiesFile.canRead()) {
            throw new RuntimeException(String.format("loading properties error: file not existed: %s", propertiesFilePath));
        }
        try (final InputStream is = new FileInputStream(propertiesFile)) {
            properties.load(is);
            return properties;
        } catch (Exception cause) {
            throw new RuntimeException("loading properties error!", cause);
        }
    }

    @BeforeClass
    public static void initialization() throws Exception {
        tPuppet = initPuppetThing();
        tpPuppet = initPuppetThingPlatform();
    }

    @AfterClass
    public static void destroy() throws Exception {
        if (null != tPuppet) {
            tPuppet.destroy();
        }
        if (null != tpPuppet) {
            tpPuppet.destroy();
        }
    }


    // ------------------------------------- THING ------------------------------------

    private static Thing initPuppetThing() throws Exception {
        return new ThingConnector()
                .connecting(THING_SERVER_URL, THING_ACCESS)
                .load(new File("./src/test/resources/lib/athing-component-dmgr-core-1.0.0-SNAPSHOT-jar-with-dependencies-for-qatest.jar"))
                .load(
                        new QaThingComImpl(),
                        new ResourceThingComImpl(),
                        new ThingCom() {
                        }
                )
                .setThingConfigListener(qaThingConfigListener)
                .setThingOpHook(thing -> logger.info("{} require reboot", thing))
                .connect(new ThingConnectOption())
                .sync()
                .getSuccess();
    }

    private static ThingPlatform initPuppetThingPlatform() throws ThingPlatformException {
        return new ThingPlatformBuilder()
                .building(DEFAULT_REGION_ID, PLATFORM_ACCESS)
                .product(PRODUCT_ID, DmgrThingCom.class, LightThingCom.class, EchoThingCom.class)
                .consumer(
                        DEFAULT_REGION_ID,
                        PLATFORM_ACCESS,
                        PLATFORM_JMS_CONNECTION_URL,
                        PLATFORM_JMS_CONSUMER_GROUP,
                        new QaThingMessageGroupListener(new ThingMessageListener[]{
                                qaThingReplyMessageListener,
                                qaThingPostMessageListener
                        })
                )
                .build();
    }

    public <T extends ThingReplyMessage> T waitingForReplyMessageByReqId(String reqId) throws InterruptedException {
        return qaThingReplyMessageListener.waitingForReplyMessageByReqId(reqId);
    }

    public <T extends ThingPostMessage> T waitingForPostMessageByReqId(String reqId) throws InterruptedException {
        return qaThingPostMessageListener.waitingForPostMessageByReqId(reqId);
    }

    public ThingConfig waitingForReceiveThingConfig() throws InterruptedException {
        return qaThingConfigListener.waitingForReceiveThingConfig();
    }

}
