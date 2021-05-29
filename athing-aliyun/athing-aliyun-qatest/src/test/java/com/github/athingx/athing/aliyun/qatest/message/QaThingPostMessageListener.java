package com.github.athingx.athing.aliyun.qatest.message;

import com.github.athingx.athing.standard.platform.message.ThingMessage;
import com.github.athingx.athing.standard.platform.message.ThingMessageListener;
import com.github.athingx.athing.standard.platform.message.ThingPostMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class QaThingPostMessageListener implements ThingMessageListener {

    private final ConcurrentHashMap<String, Waiter> tokenWaiterMap = new ConcurrentHashMap<>();

    @Override
    public void onMessage(ThingMessage message) {

        if (!(message instanceof ThingPostMessage)) {
            return;
        }
        final ThingPostMessage postMsg = (ThingPostMessage) message;
        final Waiter existed, current = new Waiter(postMsg);
        if ((existed = tokenWaiterMap.putIfAbsent(postMsg.getToken(), current)) != null) {
            existed.message = postMsg;
            existed.latch.countDown();
        }

    }

    @SuppressWarnings("unchecked")
    public <T extends ThingPostMessage> T waitingForPostMessageByToken(String token) throws InterruptedException {
        final Waiter existed, current = new Waiter();
        final Waiter waiter = (existed = tokenWaiterMap.putIfAbsent(token, current)) != null
                ? existed
                : current;
        waiter.latch.await();
        return (T) waiter.message;
    }

    private static class Waiter {

        private final CountDownLatch latch = new CountDownLatch(1);
        private ThingPostMessage message;

        public Waiter() {
        }

        public Waiter(ThingPostMessage message) {
            this.message = message;
            this.latch.countDown();
        }
    }

}
