package com.github.ompc.athing.aliyun.qatest.message;

import com.github.ompc.athing.standard.platform.message.ThingMessage;
import com.github.ompc.athing.standard.platform.message.ThingMessageListener;
import com.github.ompc.athing.standard.platform.message.ThingReplyMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 测试用设备消息监听器
 */
public class QaThingReplyMessageListener implements ThingMessageListener {

    private final ConcurrentHashMap<String, Waiter> tokenWaiterMap = new ConcurrentHashMap<>();

    @Override

    public void onMessage(ThingMessage message) {
        if (!(message instanceof ThingReplyMessage)) {
            return;
        }
        final ThingReplyMessage replyMsg = (ThingReplyMessage) message;
        final Waiter existed, current = new Waiter(replyMsg);
        if ((existed = tokenWaiterMap.putIfAbsent(replyMsg.getToken(), current)) != null) {
            existed.message = replyMsg;
            existed.latch.countDown();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ThingReplyMessage> T waitingForReplyMessageByToken(String token) throws InterruptedException {
        final Waiter existed, current = new Waiter();
        final Waiter waiter = (existed = tokenWaiterMap.putIfAbsent(token, current)) != null
                ? existed
                : current;
        waiter.latch.await();
        return (T) waiter.message;
    }

    private static class Waiter {

        private final CountDownLatch latch = new CountDownLatch(1);
        private ThingReplyMessage message;

        public Waiter() {
        }

        public Waiter(ThingReplyMessage message) {
            this.message = message;
            this.latch.countDown();
        }
    }

}
