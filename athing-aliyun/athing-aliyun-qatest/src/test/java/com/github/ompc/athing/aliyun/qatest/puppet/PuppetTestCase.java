package com.github.ompc.athing.aliyun.qatest.puppet;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.qatest.puppet.component.EchoThingCom;
import com.github.ompc.athing.aliyun.qatest.puppet.component.LightThingCom;
import com.github.ompc.athing.component.dmgr.api.DmgrThingCom;
import com.github.ompc.athing.component.dmgr.api.domain.info.CpuInfo;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.component.ThingEvent;
import com.github.ompc.athing.standard.platform.ThingPlatformException;
import com.github.ompc.athing.standard.platform.ThingTemplate;
import com.github.ompc.athing.standard.platform.domain.SortOrder;
import com.github.ompc.athing.standard.platform.domain.ThingPropertySnapshot;
import com.github.ompc.athing.standard.platform.message.ThingPostEventMessage;
import com.github.ompc.athing.standard.platform.message.ThingPostPropertyMessage;
import com.github.ompc.athing.standard.platform.message.ThingReplyPropertySetMessage;
import com.github.ompc.athing.standard.platform.message.ThingReplyServiceReturnMessage;
import com.github.ompc.athing.standard.platform.util.TpEmptyReturn;
import com.github.ompc.athing.standard.platform.util.TpReturnHelper;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingReplyFuture;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PuppetTestCase extends PuppetSupport {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test$thing_post_properties$success() throws InterruptedException {

        final Identifier cpuInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "cpu_info");
        final Identifier memoryInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "memory_info");
        final Identifier networksInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "network_info");
        final Identifier powersInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "power_info");
        final Identifier storesInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "store_info");

        final ThingReplyFuture<Void> future = tPuppet.getThingOp().postProperties(
                new Identifier[]{
                        cpuInfoId,
                        memoryInfoId,
                        networksInfoId,
                        powersInfoId,
                        storesInfoId
                }
        );

        future.await();
        if (future.isException()) {
            future.getException().printStackTrace();
        }

        Assert.assertTrue(future.isSuccess());

        final ThingPostPropertyMessage message = waitingForPostMessageByReqId(future.getToken());
        Assert.assertNotNull(message.getPropertySnapshot(cpuInfoId));
        Assert.assertNotNull(message.getPropertySnapshot(memoryInfoId));
        Assert.assertNotNull(message.getPropertySnapshot(networksInfoId));
        Assert.assertNotNull(message.getPropertySnapshot(powersInfoId));
        Assert.assertNotNull(message.getPropertySnapshot(storesInfoId));

    }

    @Test
    public void test$thing_post_properties$failure$property_not_provide() throws InterruptedException {
        final ThingReplyFuture<Void> future = tPuppet.getThingOp().postProperties(
                new Identifier[]{
                        Identifier.toIdentifier("not_exist_component", "not_exist_property")
                }
        );
        future.await();
        Assert.assertTrue(future.isException());
        Assert.assertTrue(future.getException() instanceof ThingException);
    }

    @Test
    public void test$thing_post_event$success() throws InterruptedException {
        final ThingReplyFuture<Void> future = tPuppet.getThingOp().postEvent(new ThingEvent<>(
                        Identifier.toIdentifier(EchoThingCom.THING_COM_ID, "echo_event"),
                        new EchoThingCom.Echo("HELLO!")
                )
        );

        future.await();
        Assert.assertTrue(future.isSuccess());

        final ThingPostEventMessage message = waitingForPostMessageByReqId(future.getToken());
        final EchoThingCom.Echo echo = message.getData();
        Assert.assertEquals("HELLO!", echo.getWords());
    }

    @Test
    public void test$platform_invoke_sync_service$success() {
        final EchoThingCom.Echo echo = tpPuppet.getThingTemplate(PRODUCT_ID, THING_ID)
                .getThingComponent(EchoThingCom.class)
                .echoBySync("HELLO!");
        Assert.assertEquals("HELLO!", echo.getWords());
    }

    @Test
    public void test$platform_invoke_async_service$success() throws Exception {
        final TpEmptyReturn tpReturn = TpReturnHelper.getTpEmptyReturn(() ->
                tpPuppet.getThingTemplate(PRODUCT_ID, THING_ID)
                        .getThingComponent(EchoThingCom.class)
                        .echoByAsync(new EchoThingCom.Echo("HELLO!")));
        final ThingReplyServiceReturnMessage message = waitingForReplyMessageByReqId(tpReturn.getReqId());
        final EchoThingCom.Echo echo = message.getData();
        Assert.assertEquals("HELLO!", echo.getWords());
    }

    @Test
    public void test$platform_batch_set_properties$success() throws Exception {

        final Identifier brightId = Identifier.toIdentifier(LightThingCom.THING_COM_ID, "bright");
        final Identifier stateId = Identifier.toIdentifier(LightThingCom.THING_COM_ID, "state");
        final Map<Identifier, Object> propertyValueMap = new HashMap<>();
        propertyValueMap.put(brightId, 100);
        propertyValueMap.put(stateId, LightThingCom.State.ON);

        // reset
        final LightThingCom lightCom = tPuppet.getThingCom(LightThingCom.class, true);
        lightCom.setBright(0);
        lightCom.setState(LightThingCom.State.OFF);

        // changed
        final TpEmptyReturn tpReturn = TpReturnHelper.getTpEmptyReturn(() ->
                tpPuppet.getThingTemplate(PRODUCT_ID, THING_ID)
                        .batchSetProperties(propertyValueMap)
        );

        // check
        final ThingReplyPropertySetMessage message = waitingForReplyMessageByReqId(tpReturn.getReqId());
        Assert.assertEquals(200, message.getCode());
        Assert.assertEquals(100, lightCom.getBright());
        Assert.assertEquals(LightThingCom.State.ON, lightCom.getState());
    }

    @Test
    public void test$platform_get_property$success() {
        final CpuInfo cpuInfo = tpPuppet.getThingTemplate(PRODUCT_ID, THING_ID)
                .getThingComponent(DmgrThingCom.class)
                .getCpuInfo();
        Assert.assertNotNull(cpuInfo);
    }

    @Test
    public void test$platform_batch_get_properties$success() throws ThingPlatformException, InterruptedException {

        final Identifier cpuInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "cpu_info");
        final Identifier memoryInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "memory_info");
        final Identifier networksInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "network_info");
        final Identifier powersInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "power_info");
        final Identifier storesInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "store_info");

        final ThingReplyFuture<Void> future = tPuppet.getThingOp().postProperties(new Identifier[]{
                cpuInfoId,
                memoryInfoId,
                networksInfoId,
                powersInfoId,
                storesInfoId
        });

        future.await();
        Assert.assertTrue(future.isSuccess());
        Assert.assertTrue(future.getSuccess().isOk());


        final Set<Identifier> identifiers = new HashSet<Identifier>() {{
            add(cpuInfoId);
            add(memoryInfoId);
            add(networksInfoId);
            add(powersInfoId);
            add(storesInfoId);
        }};

        final ThingTemplate template = tpPuppet.getThingTemplate(PRODUCT_ID, THING_ID);
        final Map<Identifier, ThingPropertySnapshot> propertySnapshotMap = template.batchGetProperties(identifiers);
        Assert.assertNotNull(propertySnapshotMap.get(cpuInfoId));
        Assert.assertNotNull(propertySnapshotMap.get(memoryInfoId));
        Assert.assertNotNull(propertySnapshotMap.get(networksInfoId));
        Assert.assertNotNull(propertySnapshotMap.get(powersInfoId));
        Assert.assertNotNull(propertySnapshotMap.get(storesInfoId));

    }

    @Test
    public void test$platform_iterator_get_property$success() throws ThingPlatformException {
        final Identifier cpuInfoId = Identifier.toIdentifier(DmgrThingCom.THING_COM_ID, "cpu_info");
        final ThingTemplate template = tpPuppet.getThingTemplate(PRODUCT_ID, THING_ID);
        final Iterator<ThingPropertySnapshot> propertySnapshotIt = template.iteratorForPropertySnapshot(cpuInfoId, 10, SortOrder.DESCENDING);
        while (propertySnapshotIt.hasNext()) {
            final ThingPropertySnapshot propertySnapshot = propertySnapshotIt.next();
            logger.info("property:{}", GsonFactory.getGson().toJson(propertySnapshot));
            Assert.assertNotNull(propertySnapshot);
        }
    }

}
