package com.github.athingx.athing.aliyun.qatest.message;

import com.github.athingx.athing.standard.platform.message.ThingMessage;
import com.github.athingx.athing.standard.platform.message.ThingMessageListener;
import com.github.athingx.athing.standard.platform.message.ThingModularUpgradeMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class QaThingModularUpgradeMessageListener implements ThingMessageListener {

    private final ConcurrentHashMap<String, Waiter> moduleWaiterMap = new ConcurrentHashMap<>();

    @Override
    public void onMessage(ThingMessage message) {

        if (!(message instanceof ThingModularUpgradeMessage)) {
            return;
        }
        final ThingModularUpgradeMessage upgradeMsg = (ThingModularUpgradeMessage) message;
        final Waiter existed, current = new Waiter(upgradeMsg);
        if ((existed = moduleWaiterMap.putIfAbsent(upgradeMsg.getModuleId(), current)) != null) {
            existed.message = upgradeMsg;
            existed.latch.countDown();
        }

    }

    public ThingModularUpgradeMessage waitingForThingModularUpgradeMessageByModuleId(String moduleId) throws InterruptedException {
        final Waiter existed, current = new Waiter();
        final Waiter waiter = (existed = moduleWaiterMap.putIfAbsent(moduleId, current)) != null
                ? existed
                : current;
        waiter.latch.await();
        return waiter.message;
    }

    private static class Waiter {

        private final CountDownLatch latch = new CountDownLatch(1);
        private ThingModularUpgradeMessage message;

        public Waiter() {
        }

        public Waiter(ThingModularUpgradeMessage message) {
            this.message = message;
            this.latch.countDown();
        }
    }

}
